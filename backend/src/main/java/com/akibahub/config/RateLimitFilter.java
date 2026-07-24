package com.akibahub.config;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitConfig rateLimitConfig;
    private final Set<String> trustedProxies;

    public RateLimitFilter(RateLimitConfig rateLimitConfig,
                            @Value("${security.trusted-proxies:127.0.0.1,::1}") String trustedProxiesCsv) {
        this.rateLimitConfig = rateLimitConfig;
        this.trustedProxies = Arrays.stream(trustedProxiesCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        boolean authLimited = isAuthLimitedPath(uri);
        boolean paymentLimited = isPaymentInitiatePath(uri, request.getMethod());

        if (!authLimited && !paymentLimited) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIP(request);
        Bucket bucket = authLimited
                ? rateLimitConfig.resolveAuthBucket(clientIp)
                : rateLimitConfig.resolvePaymentBucket(clientIp);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        } else {
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
            response.setHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            // Must match the { success, message, data } shape every other
            // endpoint returns via ApiResponse (see GlobalExceptionHandler).
            // This filter runs before any @RestControllerAdvice, so it was
            // previously hand-writing a different shape ({"error": "..."}).
            // The frontend's apiFetch only ever reads data.message, so a
            // 429 silently fell back to the generic "Request failed" text
            // instead of telling the user they were rate-limited.
            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Too many requests. Please wait a moment and try again.\"}");
        }
    }

    // Only endpoints where a request carries an arbitrary, guessable
    // credential (a password, or a refresh token someone might be
    // brute-forcing) belong in the auth bucket. This used to be
    // uri.startsWith("/api/auth/"), which also caught GET /api/auth/me -
    // an authenticated, read-only "who am I" check that every page calls
    // at least twice (the sidebar profile chip, plus that page's own data
    // load). With a 10-requests-per-60s bucket SHARED across all of
    // /api/auth/* per IP, a few page navigations - or a few people testing
    // from the same network - burned through the budget on /me traffic
    // alone, and it started 429ing. Since fetchCurrentUser() failures are
    // handled gracefully (silently degrade rather than error), the visible
    // symptom was confusing: the settings page profile section quietly
    // staying blank, and the dashboard greeting quietly not personalizing
    // with the user's name - no error shown either way, just missing data.
    // /logout is excluded for the same reason: it requires a valid access
    // token already, so it isn't a credential-guessing surface either.
    private static boolean isAuthLimitedPath(String uri) {
        return "/api/auth/login".equals(uri)
                || "/api/auth/register".equals(uri)
                || "/api/auth/refresh".equals(uri);
    }

    private static boolean isPaymentInitiatePath(String uri, String method) {
        if (!"POST".equalsIgnoreCase(method)) {
            return false;
        }
        return "/api/wallets/me/personal/deposit".equals(uri)
                || "/api/payments/demo/complete".equals(uri);
    }

    private String getClientIP(HttpServletRequest request) {
        String directPeer = request.getRemoteAddr();

        // Only honor X-Forwarded-For if this request's direct TCP connection
        // came from a reverse proxy we control. Otherwise the header is
        // attacker-controlled input - anyone can set
        // "X-Forwarded-For: <anything>" on a request they send directly
        // to us, which previously handed them a brand new rate-limit
        // bucket on every request, defeating the limiter entirely.
        if (trustedProxies.contains(directPeer)) {
            String xfHeader = request.getHeader("X-Forwarded-For");
            if (xfHeader != null && !xfHeader.isBlank()) {
                return xfHeader.split(",")[0].trim();
            }
        }
        return directPeer;
    }
}
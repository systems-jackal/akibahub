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
        boolean authLimited = uri.startsWith("/api/auth/");
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
            response.getWriter().write("{\"error\":\"Too many requests. Try again later.\"}");
        }
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

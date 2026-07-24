package com.akibahub.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final RateLimitFilter rateLimitFilter;

    // Configurable so a local/demo run (frontend opened on a different
    // origin/port than the backend, e.g. via `python -m http.server` or the
    // VS Code Live Server extension) isn't silently blocked by CORS. This
    // used to be hard-coded to only "https://akiba.unitybridge.dev" - any
    // other origin got no Access-Control-Allow-Origin header at all, so the
    // browser threw a CORS error, fetch() rejected with a generic
    // "Failed to fetch"/"NetworkError", and every page's catch block showed
    // that raw, unhelpful message via showAlert(). Defaults below keep prod
    // locked down while covering common local dev origins out of the box.
    @Value("${security.cors.allowed-origins:https://akiba.unitybridge.dev,http://localhost:8080,http://127.0.0.1:8080,http://localhost:5500,http://127.0.0.1:5500}")
    private String allowedOriginsCsv;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter, RateLimitFilter rateLimitFilter) {
        this.jwtFilter = jwtFilter;
        this.rateLimitFilter = rateLimitFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Only these three are public. Previously "/api/auth/**"
                // permitted EVERYTHING under /api/auth, which meant
                // /api/auth/me (and now /logout) were reachable with no
                // token at all - GET /api/auth/me with no Authorization
                // header would reach the controller with a null user and
                // throw an NPE instead of a clean 401.
                .requestMatchers(
                        "/api/auth/register",
                        "/api/auth/login",
                        "/api/auth/refresh",
                        "/api/payments/callback",
                        "/health"
                ).permitAll()
                .anyRequest().authenticated()
            )
            // Order matters, and addFilterBefore(X, Target) inserts X
            // immediately before Target IN THE FILTER LIST - so the filter
            // added in the LAST call ends up closest to Target, and
            // earlier calls end up earlier still. To get the intended
            // chain RateLimitFilter -> JwtAuthenticationFilter ->
            // UsernamePasswordAuthenticationFilter, rateLimitFilter must
            // be added FIRST and jwtFilter SECOND (previously this was
            // reversed, so JwtAuthenticationFilter was silently running
            // BEFORE RateLimitFilter despite the comment claiming
            // otherwise).
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> allowedOrigins = Arrays.stream(allowedOriginsCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowCredentials(true);
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
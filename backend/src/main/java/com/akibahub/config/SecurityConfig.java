package com.akibahub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final RateLimitFilter rateLimitFilter;

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
                .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/refresh", "/health")
                    .permitAll()
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
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("https://akiba.unitybridge.dev"));
        config.setAllowCredentials(true);
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
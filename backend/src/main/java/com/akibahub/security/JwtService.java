package com.akibahub.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret:CHANGE_THIS_TO_A_VERY_LONG_SECRET_KEY_12345678901234567890}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    private Key key;

    @PostConstruct
    public void init() {
        // Ensure secret is long enough for HS256 (at least 32 bytes)
        if (secret.length() < 32) {
            secret = secret + "12345678901234567890123456789012";
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            System.err.println("Error extracting email: " + e.getMessage());
            return null;
        }
    }

    public boolean isValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.err.println("Token expired: " + e.getMessage());
            return false;
        } catch (JwtException e) {
            System.err.println("JWT validation error: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Validation error: " + e.getMessage());
            return false;
        }
    }
}
package com.akibahub.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String fullName;
    private String phoneNumber;

    @Column(unique = true)
    private String memberCode;

    private String provider;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;  // Add this to match database

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (memberCode == null) {
            memberCode = "MBR-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 10000);
        }
        if (provider == null) {
            provider = "LOCAL";
        }
        // active is already defaulted to true via @Builder.Default
    }
}
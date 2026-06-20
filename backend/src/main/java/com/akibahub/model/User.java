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

    // If your database has these columns, uncomment them:
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // ⚠️ ONLY REMOVE this if your database doesn't have it:
    // @Column(name = "updated_at")
    // private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (memberCode == null) {
            memberCode = "MBR-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 10000);
        }
        if (provider == null) {
            provider = "LOCAL";
        }
    }
}
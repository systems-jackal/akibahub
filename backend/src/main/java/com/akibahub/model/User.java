package com.akibahub.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String memberCode;
    private String provider;
    
    // Add this if your database has it
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
    
    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    // REMOVE updated_at - it's causing the error
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = java.time.LocalDateTime.now();
        }
        if (memberCode == null) {
            memberCode = "MBR-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 10000);
        }
        if (provider == null) {
            provider = "LOCAL";
        }
    }
}
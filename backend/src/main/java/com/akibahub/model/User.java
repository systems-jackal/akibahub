package com.akibahub.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = true)
    private String password;

    private String fullName;

    private String phoneNumber;

    @Column(unique = true)
    private String memberCode;

    private String provider; // LOCAL, GOOGLE, etc
}
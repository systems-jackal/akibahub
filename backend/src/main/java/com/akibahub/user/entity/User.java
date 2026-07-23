package com.akibahub.user.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 15)
    private String phoneNumber;

    @Column(unique = true, nullable = false, length = 8)
    private String idNumber;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String fullName;

    // Drives authorization: JwtAuthenticationFilter grants a
    // "ROLE_<role>" authority from this, which @PreAuthorize checks on
    // admin-only endpoints. Defaults to MEMBER for every normal signup -
    // there is deliberately no way to self-register as ADMIN through the
    // API; promoting a user is an out-of-band (database/ops) action.
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.MEMBER;

    // A suspended account is rejected at login (see AuthService) even
    // with a correct password - this is what an admin's "suspend user"
    // action actually does.
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public UserDto toDto() {
        return new UserDto(id, fullName, phoneNumber, idNumber, role, status);
    }

    public record UserDto(Long id, String fullName, String phoneNumber, String idNumber, Role role, AccountStatus status) {}

    public enum Role { MEMBER, ADMIN }
    public enum AccountStatus { ACTIVE, SUSPENDED }
}
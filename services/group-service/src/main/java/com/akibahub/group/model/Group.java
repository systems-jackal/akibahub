package com.akibahub.group.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "groups")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "group_wallet_balance", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal groupWalletBalance = BigDecimal.ZERO;

    @Column(name = "total_contributed", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalContributed = BigDecimal.ZERO;

    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "KES";

    @Column(name = "max_members")
    @Builder.Default
    private int maxMembers = 50;

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

    @Column(name = "contribution_threshold")
    @Builder.Default
    private int contributionThreshold = 51;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void creditWallet(BigDecimal amount) {
        this.groupWalletBalance = this.groupWalletBalance.add(amount);
        this.totalContributed = this.totalContributed.add(amount);
    }

    public void debitWallet(BigDecimal amount) {
        if (this.groupWalletBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient group wallet balance");
        }
        this.groupWalletBalance = this.groupWalletBalance.subtract(amount);
    }
}

package com.akibahub.savings.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "total_deposited", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalDeposited = BigDecimal.ZERO;

    @Column(name = "total_withdrawn", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalWithdrawn = BigDecimal.ZERO;

    @Column(length = 3)
    @Builder.Default
    private String currency = "KES";

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void credit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
        this.totalDeposited = this.totalDeposited.add(amount);
    }

    public void debit(BigDecimal amount) {
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }
        this.balance = this.balance.subtract(amount);
        this.totalWithdrawn = this.totalWithdrawn.add(amount);
    }
}

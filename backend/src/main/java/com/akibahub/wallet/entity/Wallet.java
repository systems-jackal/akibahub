package com.akibahub.wallet.entity;

import com.akibahub.user.entity.User;
import com.akibahub.group.entity.Group;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "wallets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Wallet {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @Column(nullable = false) @Enumerated(EnumType.STRING)
    private WalletType type;

    @Builder.Default
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance = BigDecimal.ZERO;

    // Optimistic locking. Without this, two concurrent requests against
    // the same wallet (double-click deposit, or a contribution racing a
    // proposal payout) can both read the same starting balance and one
    // update silently overwrites the other - a lost-update race that's
    // a real correctness bug for a financial ledger. With @Version,
    // Hibernate includes "AND version = ?" on the UPDATE and throws
    // OptimisticLockException if another transaction beat it to the
    // save, instead of quietly losing money.
    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;

    public enum WalletType { PERSONAL, GROUP }
}
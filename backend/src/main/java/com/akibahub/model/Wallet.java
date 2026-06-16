package com.akibahub.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "wallets")
public class Wallet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "wallet_type", nullable = false)
    private WalletType walletType;

    @Column(name = "current_balance", nullable = false)
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @OneToOne
    @JoinColumn(
            name = "owner_id",
            nullable = false,
            unique = true
    )
    private User owner;

    public Wallet() {}

    public Long getId() {
        return id;
    }

    public WalletType getWalletType() {
        return walletType;
    }

    public void setWalletType(WalletType walletType) {
        this.walletType = walletType;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }
}
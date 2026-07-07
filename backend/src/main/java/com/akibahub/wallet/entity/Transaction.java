package com.akibahub.wallet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Transaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false) @Enumerated(EnumType.STRING)
    private TransactionType type;

    private String reference;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @PrePersist protected void onCreate() { timestamp = LocalDateTime.now(); }

    public enum TransactionType { DEPOSIT, WITHDRAWAL }
}
package com.akibahub.ledger.entity;

import com.akibahub.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "transfers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Transfer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Type type;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Status status;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiated_by")
    private User initiatedBy;

    private String reference;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum Type { DEPOSIT, WITHDRAWAL, CONTRIBUTION, PROPOSAL_PAYOUT }

    // COMPLETED is the only status this migration writes today - FAILED
    // and REVERSED exist so the schema doesn't need to change again once
    // the M-Pesa integration needs to represent a pending/failed
    // transfer, or you need to record a manual reversal.
    public enum Status { COMPLETED, FAILED, REVERSED }
}
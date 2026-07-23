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

    // PENDING exists for future transfer-level pending states; deposit
    // pending rows live in pending_payments. FAILED/REVERSED support
    // failed PSP outcomes and manual reversals without another migration.
    public enum Status { PENDING, COMPLETED, FAILED, REVERSED }
}
package com.akibahub.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "proposals")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Proposal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "proposer_id", nullable = false)
    private Long proposerId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ProposalStatus status = ProposalStatus.PENDING;

    @Column(name = "yes_votes")
    private Integer yesVotes = 0;

    @Column(name = "no_votes")
    private Integer noVotes = 0;

    @Column(name = "abstain_votes")
    private Integer abstainVotes = 0;

    @Column(name = "required_yes_votes", nullable = false)
    private Integer requiredYesVotes;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    public enum ProposalStatus {
        PENDING, APPROVED, REJECTED, EXECUTED
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", insertable = false, updatable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposer_id", insertable = false, updatable = false)
    private User proposer;
}
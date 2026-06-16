package com.akibahub.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "votes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"proposal_id", "voter_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "proposal_id", nullable = false)
    private Long proposalId;

    @Column(name = "voter_id", nullable = false)
    private Long voterId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private VoteDecision decision; // YES, NO, ABSTAIN

    @CreationTimestamp
    @Column(name = "voted_at", updatable = false)
    private LocalDateTime votedAt;

    public enum VoteDecision {
        YES, NO, ABSTAIN
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id", insertable = false, updatable = false)
    private Proposal proposal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voter_id", insertable = false, updatable = false)
    private User voter;
}
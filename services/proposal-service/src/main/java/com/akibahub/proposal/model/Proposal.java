package com.akibahub.proposal.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "proposals")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Proposal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "group_id", nullable = false)
    private String groupId;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "creator_email")
    private String creatorEmail;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(length = 3)
    @Builder.Default
    private String currency = "KES";

    @Column(name = "recipient_phone")
    private String recipientPhone;

    @Column(name = "recipient_description")
    private String recipientDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProposalStatus status = ProposalStatus.ACTIVE;

    @Column(name = "threshold_percent", nullable = false)
    @Builder.Default
    private int thresholdPercent = 51;

    @Column(name = "total_eligible_voters")
    private int totalEligibleVoters;

    @Column(name = "yes_count")
    @Builder.Default
    private int yesCount = 0;

    @Column(name = "no_count")
    @Builder.Default
    private int noCount = 0;

    @Column(name = "abstain_count")
    @Builder.Default
    private int abstainCount = 0;

    @Column(name = "voting_deadline", nullable = false)
    private LocalDateTime votingDeadline;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "execution_reference")
    private String executionReference;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(votingDeadline);
    }

    public boolean hasReachedThreshold() {
        if (totalEligibleVoters == 0) return false;
        double yesPercent = (double) yesCount / totalEligibleVoters * 100;
        return yesPercent >= thresholdPercent;
    }

    public boolean hasBeenRejected() {
        if (totalEligibleVoters == 0) return false;
        // Rejected if remaining possible YES votes can't reach threshold
        int votesLeft = totalEligibleVoters - yesCount - noCount - abstainCount;
        double maxPossibleYes = (double) (yesCount + votesLeft) / totalEligibleVoters * 100;
        return maxPossibleYes < thresholdPercent;
    }
}

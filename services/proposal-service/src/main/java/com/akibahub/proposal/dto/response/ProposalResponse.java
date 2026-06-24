package com.akibahub.proposal.dto.response;

import com.akibahub.proposal.model.Proposal;
import com.akibahub.proposal.model.ProposalStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ProposalResponse {
    private String id;
    private String groupId;
    private String createdBy;
    private String title;
    private String description;
    private BigDecimal amount;
    private String currency;
    private String recipientPhone;
    private String recipientDescription;
    private ProposalStatus status;
    private int thresholdPercent;
    private int totalEligibleVoters;
    private int yesCount;
    private int noCount;
    private int abstainCount;
    private double yesPercent;
    private LocalDateTime votingDeadline;
    private LocalDateTime executedAt;
    private LocalDateTime createdAt;

    public static ProposalResponse from(Proposal p) {
        double yesPercent = p.getTotalEligibleVoters() > 0
                ? (double) p.getYesCount() / p.getTotalEligibleVoters() * 100 : 0;
        return ProposalResponse.builder()
                .id(p.getId())
                .groupId(p.getGroupId())
                .createdBy(p.getCreatedBy())
                .title(p.getTitle())
                .description(p.getDescription())
                .amount(p.getAmount())
                .currency(p.getCurrency())
                .recipientPhone(p.getRecipientPhone())
                .recipientDescription(p.getRecipientDescription())
                .status(p.getStatus())
                .thresholdPercent(p.getThresholdPercent())
                .totalEligibleVoters(p.getTotalEligibleVoters())
                .yesCount(p.getYesCount())
                .noCount(p.getNoCount())
                .abstainCount(p.getAbstainCount())
                .yesPercent(Math.round(yesPercent * 10.0) / 10.0)
                .votingDeadline(p.getVotingDeadline())
                .executedAt(p.getExecutedAt())
                .createdAt(p.getCreatedAt())
                .build();
    }
}

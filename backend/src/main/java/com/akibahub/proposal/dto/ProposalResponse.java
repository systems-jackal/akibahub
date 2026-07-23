package com.akibahub.proposal.dto;

import com.akibahub.proposal.entity.Proposal;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * What the frontend actually receives for a proposal, instead of the raw
 * JPA entity. Two things this fixes at once:
 *
 * 1. Proposal.group and Proposal.createdBy are @JsonIgnore on the entity
 *    (correctly - you don't want to accidentally serialize an entire
 *    Group or User graph). But that also meant the frontend's
 *    `p.group?.name` was ALWAYS undefined - there was no group data
 *    reaching the client at all, hence "Unknown Group" always showing.
 *    This DTO exposes a small, safe GroupSummary instead.
 *
 * 2. Adds live vote tallies (yesVotes / noVotes / totalMembers) so the
 *    DAO-style vote tally bar in the UI can show real numbers instead of
 *    just the final status.
 */
public record ProposalResponse(
        Long id,
        String title,
        String description,
        BigDecimal amount,
        Proposal.ProposalStatus status,
        LocalDateTime createdAt,
        GroupSummary group,
        long yesVotes,
        long noVotes,
        long totalMembers,
        // null when the current user has not voted; "YES" / "NO" otherwise.
        // Lets the UI hide dials instead of offering a vote that the
        // backend will reject with "Already voted".
        String myVote
) {
    public record GroupSummary(Long id, String name) {}
}
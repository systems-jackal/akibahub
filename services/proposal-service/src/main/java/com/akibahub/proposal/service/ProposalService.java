package com.akibahub.proposal.service;

import com.akibahub.proposal.model.*;
import com.akibahub.proposal.repository.ProposalRepository;
import com.akibahub.proposal.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProposalService {

    private final ProposalRepository proposalRepository;
    private final VoteRepository voteRepository;
    private final ConsensusEngine consensusEngine;
    private final GroupServiceClient groupServiceClient;
    private final AuditPublisher auditPublisher;

    @Value("${proposal.voting.default-threshold-percent:51}")
    private int defaultThreshold;

    @Value("${proposal.voting.default-duration-hours:72}")
    private int defaultDurationHours;

    @Transactional
    public Proposal createProposal(String creatorId, String creatorEmail,
                                   String groupId, String title, String description,
                                   BigDecimal amount, String recipientPhone,
                                   String recipientDescription) {
        if (!groupServiceClient.isMember(groupId, creatorId)) {
            throw new SecurityException("Must be a group member to create proposals");
        }

        int memberCount = groupServiceClient.getMemberCount(groupId);

        Proposal proposal = Proposal.builder()
                .groupId(groupId)
                .createdBy(creatorId)
                .creatorEmail(creatorEmail)
                .title(title)
                .description(description)
                .amount(amount)
                .recipientPhone(recipientPhone)
                .recipientDescription(recipientDescription)
                .thresholdPercent(defaultThreshold)
                .totalEligibleVoters(memberCount)
                .votingDeadline(LocalDateTime.now().plusHours(defaultDurationHours))
                .build();

        Proposal saved = proposalRepository.save(proposal);

        auditPublisher.publish("PROPOSAL_CREATED", creatorId, creatorEmail,
                saved.getId(), groupId, amount,
                "title:" + title + " threshold:" + defaultThreshold);

        log.info("Proposal created: {} in group: {}", saved.getId(), groupId);
        return saved;
    }

    @Transactional
    public Vote castVote(String proposalId, String voterId,
                         String voterEmail, VoteValue voteValue) {
        Proposal proposal = proposalRepository.findWithLockById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));

        if (proposal.getStatus() != ProposalStatus.ACTIVE) {
            throw new IllegalStateException("Proposal is no longer active");
        }
        if (proposal.isExpired()) {
            throw new IllegalStateException("Voting period has ended");
        }
        if (!groupServiceClient.isMember(proposal.getGroupId(), voterId)) {
            throw new SecurityException("Must be a group member to vote");
        }
        if (voteRepository.existsByProposalIdAndVoterId(proposalId, voterId)) {
            throw new IllegalStateException("Already voted on this proposal");
        }

        Vote vote = Vote.builder()
                .proposalId(proposalId)
                .voterId(voterId)
                .voterEmail(voterEmail)
                .value(voteValue)
                .build();
        voteRepository.save(vote);

        // Update counts
        switch (voteValue) {
            case YES -> proposal.setYesCount(proposal.getYesCount() + 1);
            case NO -> proposal.setNoCount(proposal.getNoCount() + 1);
            case ABSTAIN -> proposal.setAbstainCount(proposal.getAbstainCount() + 1);
        }
        proposalRepository.save(proposal);

        auditPublisher.publish("VOTE_CAST", voterId, voterEmail,
                proposalId, proposal.getGroupId(), null,
                "vote:" + voteValue);

        // Evaluate consensus after every vote
        consensusEngine.evaluateAfterVote(proposalId);

        return vote;
    }

    @Transactional
    public void cancelProposal(String proposalId, String requestingUserId) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));

        if (proposal.getStatus() != ProposalStatus.ACTIVE) {
            throw new IllegalStateException("Only active proposals can be cancelled");
        }
        if (!proposal.getCreatedBy().equals(requestingUserId)) {
            // Also allow group admin — checked in controller via group-service
            throw new SecurityException("Only the proposal creator can cancel it");
        }

        proposal.setStatus(ProposalStatus.CANCELLED);
        proposalRepository.save(proposal);

        auditPublisher.publish("PROPOSAL_CANCELLED", requestingUserId, null,
                proposalId, proposal.getGroupId(), proposal.getAmount(), null);
    }

    @Transactional(readOnly = true)
    public Proposal getProposal(String proposalId, String requestingUserId) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));
        if (!groupServiceClient.isMember(proposal.getGroupId(), requestingUserId)) {
            throw new SecurityException("Not a member of this group");
        }
        return proposal;
    }

    @Transactional(readOnly = true)
    public Page<Proposal> getGroupProposals(String groupId,
                                            String requestingUserId, Pageable pageable) {
        if (!groupServiceClient.isMember(groupId, requestingUserId)) {
            throw new SecurityException("Not a member of this group");
        }
        return proposalRepository.findByGroupIdOrderByCreatedAtDesc(groupId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Vote> getVotes(String proposalId, String requestingUserId) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));
        if (!groupServiceClient.isMember(proposal.getGroupId(), requestingUserId)) {
            throw new SecurityException("Not a member of this group");
        }
        return voteRepository.findByProposalId(proposalId);
    }
}

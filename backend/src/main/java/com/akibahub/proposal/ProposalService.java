package com.akibahub.proposal;

import com.akibahub.audit.AuditLogService;
import com.akibahub.group.entity.GroupMemberRepository;
import com.akibahub.ledger.LedgerService;
import com.akibahub.ledger.entity.Transfer;
import com.akibahub.proposal.dto.ProposalResponse;
import com.akibahub.proposal.entity.*;
import com.akibahub.shared.AmountValidator;
import com.akibahub.shared.exception.BadRequestException;
import com.akibahub.shared.exception.ConflictException;
import com.akibahub.shared.exception.ForbiddenException;
import com.akibahub.shared.exception.NotFoundException;
import com.akibahub.user.entity.User;
import com.akibahub.wallet.entity.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProposalService {
    private final ProposalRepository proposalRepo;
    private final VoteRepository voteRepo;
    private final GroupMemberRepository memberRepo;
    private final WalletRepository walletRepo;
    private final TransactionRepository transactionRepo;
    private final AuditLogService auditLog;
    private final LedgerService ledgerService;

    public ProposalService(ProposalRepository proposalRepo, VoteRepository voteRepo,
                           GroupMemberRepository memberRepo, WalletRepository walletRepo,
                           TransactionRepository transactionRepo, AuditLogService auditLog,
                           LedgerService ledgerService) {
        this.proposalRepo = proposalRepo;
        this.voteRepo = voteRepo;
        this.memberRepo = memberRepo;
        this.walletRepo = walletRepo;
        this.transactionRepo = transactionRepo;
        this.auditLog = auditLog;
        this.ledgerService = ledgerService;
    }

    @Transactional
    public ProposalResponse createProposal(Long groupId, String title, String description,
                                   BigDecimal amount, User creator) {
        if (title == null || title.isBlank()) {
            throw new BadRequestException("Proposal title is required");
        }
        AmountValidator.requirePositive(amount);
        var membership = memberRepo.findByGroupIdAndUserId(groupId, creator.getId())
                .orElseThrow(() -> new ForbiddenException("Not a group member"));

        Wallet groupWallet = walletRepo.findByGroupIdAndType(groupId, Wallet.WalletType.GROUP)
                .orElseThrow(() -> new NotFoundException("Group wallet not found"));
        if (groupWallet.getBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Proposal amount exceeds group balance. Contribute more first.");
        }

        Proposal proposal = Proposal.builder().group(membership.getGroup()).title(title.trim())
                .description(description == null ? null : description.trim()).amount(amount).createdBy(creator)
                .status(Proposal.ProposalStatus.OPEN).build();
        proposal = proposalRepo.save(proposal);
        auditLog.logEvent("PROPOSAL_CREATED", Map.of(
                "proposalId", proposal.getId(),
                "groupId", groupId,
                "amount", amount,
                "createdBy", creator.getId()
        ));
        return toResponse(proposal, creator);
    }

    @Transactional
    public void vote(Long proposalId, User voter, Vote.VoteValue voteValue) {
        // Pessimistic lock: concurrent tipping YES votes must serialize so
        // only one transaction can transition OPEN → APPROVED and pay out.
        Proposal proposal = proposalRepo.findByIdForUpdate(proposalId)
                .orElseThrow(() -> new NotFoundException("Proposal not found"));
        if (!proposal.getStatus().equals(Proposal.ProposalStatus.OPEN))
            throw new ConflictException("Voting closed");
        memberRepo.findByGroupIdAndUserId(proposal.getGroup().getId(), voter.getId())
                .orElseThrow(() -> new ForbiddenException("Not a group member"));
        if (voteRepo.findByProposalIdAndUserId(proposalId, voter.getId()).isPresent())
            throw new ConflictException("Already voted");

        Vote vote = Vote.builder().proposal(proposal).user(voter).vote(voteValue).build();
        voteRepo.save(vote);
        auditLog.logEvent("VOTE_CAST", Map.of(
                "proposalId", proposalId,
                "userId", voter.getId(),
                "vote", voteValue.name()
        ));

        long totalMembers = memberRepo.countByGroupId(proposal.getGroup().getId());
        long yesVotes = voteRepo.countByProposalIdAndVote(proposalId, Vote.VoteValue.YES);
        long noVotes = voteRepo.countByProposalIdAndVote(proposalId, Vote.VoteValue.NO);

        if (yesVotes > totalMembers / 2) {
            Wallet groupWallet = walletRepo.findByGroupIdAndType(proposal.getGroup().getId(), Wallet.WalletType.GROUP)
                    .orElseThrow(() -> new NotFoundException("Group wallet not found"));
            if (groupWallet.getBalance().compareTo(proposal.getAmount()) < 0) {
                // Majority YES but funds are gone — reject rather than
                // rolling back the deciding vote (which left the proposal
                // stuck OPEN with a majority that could never settle).
                proposal.setStatus(Proposal.ProposalStatus.REJECTED);
                proposalRepo.save(proposal);
                auditLog.logEvent("PROPOSAL_REJECTED_INSUFFICIENT_FUNDS", Map.of(
                        "proposalId", proposalId,
                        "amount", proposal.getAmount(),
                        "balance", groupWallet.getBalance()
                ));
            } else {
                proposal.setStatus(Proposal.ProposalStatus.APPROVED);
                proposalRepo.save(proposal);
                executeWithdrawal(proposal, groupWallet);
                auditLog.logEvent("PROPOSAL_APPROVED", Map.of("proposalId", proposalId));
            }
        } else if (noVotes > totalMembers / 2) {
            proposal.setStatus(Proposal.ProposalStatus.REJECTED);
            proposalRepo.save(proposal);
            auditLog.logEvent("PROPOSAL_REJECTED", Map.of("proposalId", proposalId));
        }
    }

    private void executeWithdrawal(Proposal proposal, Wallet groupWallet) {
        AmountValidator.requirePositive(proposal.getAmount());
        Wallet personal = walletRepo.findByUserIdAndType(proposal.getCreatedBy().getId(), Wallet.WalletType.PERSONAL)
                .orElseThrow(() -> new NotFoundException("Personal wallet not found"));

        groupWallet.setBalance(groupWallet.getBalance().subtract(proposal.getAmount()));
        personal.setBalance(personal.getBalance().add(proposal.getAmount()));

        // Persist lower wallet id first so concurrent contribute (same
        // ordering) cannot deadlock under InnoDB row locks.
        if (groupWallet.getId() < personal.getId()) {
            walletRepo.save(groupWallet);
            walletRepo.save(personal);
        } else {
            walletRepo.save(personal);
            walletRepo.save(groupWallet);
        }

        transactionRepo.save(Transaction.builder().wallet(groupWallet).amount(proposal.getAmount())
                .type(Transaction.TransactionType.WITHDRAWAL)
                .reference("Approved proposal: " + proposal.getTitle()).build());
        transactionRepo.save(Transaction.builder().wallet(personal).amount(proposal.getAmount())
                .type(Transaction.TransactionType.DEPOSIT)
                .reference("Withdrawal from group " + proposal.getGroup().getId()).build());

        ledgerService.recordInternalTransfer(Transfer.Type.PROPOSAL_PAYOUT, proposal.getCreatedBy(),
                "Approved proposal: " + proposal.getTitle(), groupWallet, personal, proposal.getAmount());
    }

    @Transactional(readOnly = true)
    public List<ProposalResponse> getProposalsForGroup(Long groupId, User user) {
        memberRepo.findByGroupIdAndUserId(groupId, user.getId())
                .orElseThrow(() -> new ForbiddenException("Not a member of this group"));
        return proposalRepo.findByGroupId(groupId).stream()
                .map(p -> toResponse(p, user)).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProposalResponse> getProposalsForUserGroups(User user) {
        List<Long> groupIds = memberRepo.findByUserId(user.getId())
                .stream().map(m -> m.getGroup().getId()).collect(Collectors.toList());
        if (groupIds.isEmpty()) return List.of();
        return proposalRepo.findByGroupIdIn(groupIds).stream()
                .map(p -> toResponse(p, user)).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProposalResponse getProposal(Long proposalId, User user) {
        Proposal proposal = proposalRepo.findById(proposalId)
                .orElseThrow(() -> new NotFoundException("Proposal not found"));
        memberRepo.findByGroupIdAndUserId(proposal.getGroup().getId(), user.getId())
                .orElseThrow(() -> new ForbiddenException("Not a member of this group"));
        return toResponse(proposal, user);
    }

    @Transactional
    public ProposalResponse updateProposal(Long proposalId, Map<String, Object> body, User user) {
        Proposal proposal = proposalRepo.findById(proposalId)
                .orElseThrow(() -> new NotFoundException("Proposal not found"));
        if (!proposal.getCreatedBy().getId().equals(user.getId()))
            throw new ForbiddenException("Only creator can edit");
        if (proposal.getStatus() != Proposal.ProposalStatus.OPEN)
            throw new ConflictException("Cannot edit a closed proposal");

        if (body.containsKey("title")) proposal.setTitle((String) body.get("title"));
        if (body.containsKey("description")) proposal.setDescription((String) body.get("description"));
        if (body.containsKey("amount")) {
            // Changing the amount after anyone has voted would pay out a
            // different figure than members approved.
            if (voteRepo.countByProposalIdAndVote(proposalId, Vote.VoteValue.YES)
                    + voteRepo.countByProposalIdAndVote(proposalId, Vote.VoteValue.NO) > 0) {
                throw new ConflictException("Cannot change amount after votes have been cast");
            }
            BigDecimal newAmount = new BigDecimal(body.get("amount").toString());
            AmountValidator.requirePositive(newAmount);
            Wallet groupWallet = walletRepo.findByGroupIdAndType(proposal.getGroup().getId(), Wallet.WalletType.GROUP)
                    .orElseThrow(() -> new NotFoundException("Group wallet not found"));
            if (groupWallet.getBalance().compareTo(newAmount) < 0) {
                throw new BadRequestException("Proposal amount exceeds group balance");
            }
            proposal.setAmount(newAmount);
        }

        return toResponse(proposalRepo.save(proposal), user);
    }

    private ProposalResponse toResponse(Proposal proposal, User viewer) {
        long totalMembers = memberRepo.countByGroupId(proposal.getGroup().getId());
        long yesVotes = voteRepo.countByProposalIdAndVote(proposal.getId(), Vote.VoteValue.YES);
        long noVotes = voteRepo.countByProposalIdAndVote(proposal.getId(), Vote.VoteValue.NO);
        String myVote = voteRepo.findByProposalIdAndUserId(proposal.getId(), viewer.getId())
                .map(v -> v.getVote().name())
                .orElse(null);
        return new ProposalResponse(
                proposal.getId(),
                proposal.getTitle(),
                proposal.getDescription(),
                proposal.getAmount(),
                proposal.getStatus(),
                proposal.getCreatedAt(),
                new ProposalResponse.GroupSummary(proposal.getGroup().getId(), proposal.getGroup().getName()),
                yesVotes,
                noVotes,
                totalMembers,
                myVote
        );
    }

    @Transactional
    public void deleteProposal(Long proposalId, User user) {
        Proposal proposal = proposalRepo.findById(proposalId)
                .orElseThrow(() -> new NotFoundException("Proposal not found"));
        if (!proposal.getCreatedBy().getId().equals(user.getId()))
            throw new ForbiddenException("Only creator can delete");
        if (proposal.getStatus() != Proposal.ProposalStatus.OPEN)
            throw new ConflictException("Cannot delete a closed proposal");

        voteRepo.deleteByProposalId(proposalId);
        proposalRepo.delete(proposal);
        auditLog.logEvent("PROPOSAL_DELETED", Map.of("proposalId", proposalId));
    }
}

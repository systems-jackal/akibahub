package com.akibahub.proposal;

import com.akibahub.audit.AuditLogService;
import com.akibahub.group.entity.GroupMemberRepository;
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

    public ProposalService(ProposalRepository proposalRepo, VoteRepository voteRepo,
                           GroupMemberRepository memberRepo, WalletRepository walletRepo,
                           TransactionRepository transactionRepo, AuditLogService auditLog) {
        this.proposalRepo = proposalRepo;
        this.voteRepo = voteRepo;
        this.memberRepo = memberRepo;
        this.walletRepo = walletRepo;
        this.transactionRepo = transactionRepo;
        this.auditLog = auditLog;
    }

    // ---------- existing methods ----------

    @Transactional
    public Proposal createProposal(Long groupId, String title, String description,
                                   BigDecimal amount, User creator) {
        AmountValidator.requirePositive(amount);
        var membership = memberRepo.findByGroupIdAndUserId(groupId, creator.getId())
                .orElseThrow(() -> new ForbiddenException("Not a group member"));
        Proposal proposal = Proposal.builder().group(membership.getGroup()).title(title)
                .description(description).amount(amount).createdBy(creator)
                .status(Proposal.ProposalStatus.OPEN).build();
        proposal = proposalRepo.save(proposal);
        auditLog.logEvent("PROPOSAL_CREATED", proposal);
        return proposal;
    }

    @Transactional
    public void vote(Long proposalId, User voter, Vote.VoteValue voteValue) {
        Proposal proposal = proposalRepo.findById(proposalId)
                .orElseThrow(() -> new NotFoundException("Proposal not found"));
        if (!proposal.getStatus().equals(Proposal.ProposalStatus.OPEN))
            throw new ConflictException("Voting closed");
        memberRepo.findByGroupIdAndUserId(proposal.getGroup().getId(), voter.getId())
                .orElseThrow(() -> new ForbiddenException("Not a group member"));
        if (voteRepo.findByProposalIdAndUserId(proposalId, voter.getId()).isPresent())
            throw new ConflictException("Already voted");

        Vote vote = Vote.builder().proposal(proposal).user(voter).vote(voteValue).build();
        voteRepo.save(vote);
        auditLog.logEvent("VOTE_CAST", vote);

        long totalMembers = memberRepo.countByGroupId(proposal.getGroup().getId());
        long yesVotes = voteRepo.countByProposalIdAndVote(proposalId, Vote.VoteValue.YES);
        long noVotes = voteRepo.countByProposalIdAndVote(proposalId, Vote.VoteValue.NO);

        if (yesVotes > totalMembers / 2) {
            proposal.setStatus(Proposal.ProposalStatus.APPROVED);
            proposalRepo.save(proposal);
            executeWithdrawal(proposal);
            auditLog.logEvent("PROPOSAL_APPROVED", proposal);
        } else if (noVotes > totalMembers / 2) {
            proposal.setStatus(Proposal.ProposalStatus.REJECTED);
            proposalRepo.save(proposal);
            auditLog.logEvent("PROPOSAL_REJECTED", proposal);
        }
    }

    private void executeWithdrawal(Proposal proposal) {
        // Defense in depth: createProposal/updateProposal already validate
        // the amount, but this is the method that actually moves money out
        // of the group wallet. Re-checking here means this method is safe
        // to call on its own, regardless of what validation ran (or didn't
        // run) earlier in the call chain - money-moving code shouldn't have
        // to trust that every caller upstream did the right thing.
        AmountValidator.requirePositive(proposal.getAmount());
        Wallet groupWallet = walletRepo.findByGroupIdAndType(proposal.getGroup().getId(), Wallet.WalletType.GROUP)
                .orElseThrow(() -> new NotFoundException("Group wallet not found"));
        if (groupWallet.getBalance().compareTo(proposal.getAmount()) < 0)
            throw new BadRequestException("Insufficient group funds");

        groupWallet.setBalance(groupWallet.getBalance().subtract(proposal.getAmount()));
        walletRepo.save(groupWallet);
        transactionRepo.save(Transaction.builder().wallet(groupWallet).amount(proposal.getAmount())
                .type(Transaction.TransactionType.WITHDRAWAL)
                .reference("Approved proposal: " + proposal.getTitle()).build());

        Wallet personal = walletRepo.findByUserIdAndType(proposal.getCreatedBy().getId(), Wallet.WalletType.PERSONAL)
                .orElseThrow(() -> new NotFoundException("Personal wallet not found"));
        personal.setBalance(personal.getBalance().add(proposal.getAmount()));
        walletRepo.save(personal);
        transactionRepo.save(Transaction.builder().wallet(personal).amount(proposal.getAmount())
                .type(Transaction.TransactionType.DEPOSIT)
                .reference("Withdrawal from group " + proposal.getGroup().getId()).build());
    }

    public List<Proposal> getProposalsForGroup(Long groupId, User user) {
        memberRepo.findByGroupIdAndUserId(groupId, user.getId())
                .orElseThrow(() -> new ForbiddenException("Not a member of this group"));
        return proposalRepo.findByGroupId(groupId);
    }

    public List<Proposal> getProposalsForUserGroups(User user) {
        List<Long> groupIds = memberRepo.findByUserId(user.getId())
                .stream().map(m -> m.getGroup().getId()).collect(Collectors.toList());
        if (groupIds.isEmpty()) return List.of();
        return proposalRepo.findByGroupIdIn(groupIds);
    }

    // ---------- new methods ----------

    public Proposal getProposal(Long proposalId, User user) {
        Proposal proposal = proposalRepo.findById(proposalId)
                .orElseThrow(() -> new NotFoundException("Proposal not found"));
        memberRepo.findByGroupIdAndUserId(proposal.getGroup().getId(), user.getId())
                .orElseThrow(() -> new ForbiddenException("Not a member of this group"));
        return proposal;
    }

    @Transactional
    public Proposal updateProposal(Long proposalId, Map<String, Object> body, User user) {
        Proposal proposal = proposalRepo.findById(proposalId)
                .orElseThrow(() -> new NotFoundException("Proposal not found"));
        if (!proposal.getCreatedBy().getId().equals(user.getId()))
            throw new ForbiddenException("Only creator can edit");
        if (proposal.getStatus() != Proposal.ProposalStatus.OPEN)
            throw new ConflictException("Cannot edit a closed proposal");

        if (body.containsKey("title")) proposal.setTitle((String) body.get("title"));
        if (body.containsKey("description")) proposal.setDescription((String) body.get("description"));
        if (body.containsKey("amount")) {
            BigDecimal newAmount = new BigDecimal(body.get("amount").toString());
            AmountValidator.requirePositive(newAmount);
            proposal.setAmount(newAmount);
        }

        return proposalRepo.save(proposal);
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
        auditLog.logEvent("PROPOSAL_DELETED", proposal);
    }
}
package com.akibahub.dashboard;

import com.akibahub.group.entity.GroupMemberRepository;
import com.akibahub.proposal.entity.ProposalRepository;
import com.akibahub.proposal.entity.VoteRepository;
import com.akibahub.user.entity.User;
import com.akibahub.wallet.entity.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final WalletRepository walletRepo;
    private final GroupMemberRepository memberRepo;
    private final ProposalRepository proposalRepo;
    private final VoteRepository voteRepo;

    public Map<String, Object> getDashboard(User user) {
        // Personal balance
        BigDecimal personalBalance = walletRepo.findByUserIdAndType(user.getId(), com.akibahub.wallet.entity.Wallet.WalletType.PERSONAL)
                .map(w -> w.getBalance()).orElse(BigDecimal.ZERO);

        // Group balances sum
        List<Long> groupIds = memberRepo.findByUserId(user.getId()).stream()
                .map(m -> m.getGroup().getId()).toList();
        BigDecimal groupBalance = BigDecimal.ZERO;
        for (Long gid : groupIds) {
            groupBalance = groupBalance.add(
                    walletRepo.findByGroupIdAndType(gid, com.akibahub.wallet.entity.Wallet.WalletType.GROUP)
                            .map(w -> w.getBalance()).orElse(BigDecimal.ZERO));
        }

        long activeGroups = groupIds.size();

        // Pending votes: proposals in my groups that are OPEN and I haven't voted on
        long pendingVotes = 0;
        var proposals = proposalRepo.findByGroupIdIn(groupIds);
        for (var p : proposals) {
            if (p.getStatus() == com.akibahub.proposal.entity.Proposal.ProposalStatus.OPEN) {
                if (voteRepo.findByProposalIdAndUserId(p.getId(), user.getId()).isEmpty()) {
                    pendingVotes++;
                }
            }
        }

        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("personalBalance", personalBalance);
        dashboard.put("groupBalance", groupBalance);
        dashboard.put("activeGroups", activeGroups);
        dashboard.put("pendingVotes", pendingVotes);
        // recent transactions/proposals can be added later
        dashboard.put("recentTransactions", List.of());
        dashboard.put("recentProposals", List.of());
        return dashboard;
    }
}
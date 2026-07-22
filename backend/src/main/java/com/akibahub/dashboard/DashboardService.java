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

        var memberships = memberRepo.findByUserId(user.getId());
        List<Long> groupIds = memberships.stream().map(m -> m.getGroup().getId()).toList();

        // Asset distribution: one entry for personal savings, one per group
        // the user belongs to. This backs the dashboard's "Asset
        // Distribution" chart - previously the dashboard only exposed an
        // aggregate groupBalance total, which can't be broken down into a
        // per-group chart on the frontend without this.
        List<Map<String, Object>> assetDistribution = new ArrayList<>();
        assetDistribution.add(Map.of("label", "Personal", "value", personalBalance));

        BigDecimal groupBalance = BigDecimal.ZERO;
        for (var m : memberships) {
            BigDecimal bal = walletRepo.findByGroupIdAndType(m.getGroup().getId(), com.akibahub.wallet.entity.Wallet.WalletType.GROUP)
                    .map(w -> w.getBalance()).orElse(BigDecimal.ZERO);
            groupBalance = groupBalance.add(bal);
            assetDistribution.add(Map.of("label", m.getGroup().getName(), "value", bal));
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
        dashboard.put("assetDistribution", assetDistribution);
        // recent transactions/proposals can be added later
        dashboard.put("recentTransactions", List.of());
        dashboard.put("recentProposals", List.of());
        return dashboard;
    }
}
package com.akibahub.transaction;

import com.akibahub.group.entity.GroupMemberRepository;
import com.akibahub.user.entity.User;
import com.akibahub.wallet.entity.Transaction;
import com.akibahub.wallet.entity.TransactionRepository;
import com.akibahub.wallet.entity.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepo;
    private final WalletRepository walletRepo;
    private final GroupMemberRepository memberRepo;

    public TransactionService(TransactionRepository transactionRepo,
                              WalletRepository walletRepo,
                              GroupMemberRepository memberRepo) {
        this.transactionRepo = transactionRepo;
        this.walletRepo = walletRepo;
        this.memberRepo = memberRepo;
    }

    @Transactional(readOnly = true)
    public List<Transaction> getUserTransactions(User user, String type, Long groupId,
                                                 LocalDateTime start, LocalDateTime end) {
        List<Long> walletIds = new ArrayList<>();

        walletRepo.findByUserIdAndType(user.getId(), com.akibahub.wallet.entity.Wallet.WalletType.PERSONAL)
                .ifPresent(w -> walletIds.add(w.getId()));

        List<Long> groupIds = memberRepo.findByUserId(user.getId())
                .stream().map(m -> m.getGroup().getId()).collect(Collectors.toList());
        for (Long gid : groupIds) {
            walletRepo.findByGroupIdAndType(gid, com.akibahub.wallet.entity.Wallet.WalletType.GROUP)
                    .ifPresent(w -> walletIds.add(w.getId()));
        }

        if (walletIds.isEmpty()) return List.of();

        List<Transaction> all = transactionRepo.findByWalletIdIn(walletIds);

        return all.stream()
                .filter(t -> type == null || t.getType().name().equalsIgnoreCase(type))
                .filter(t -> groupId == null ||
                        (t.getWallet().getGroup() != null && t.getWallet().getGroup().getId().equals(groupId)))
                .filter(t -> start == null || !t.getTimestamp().isBefore(start))
                .filter(t -> end == null || !t.getTimestamp().isAfter(end))
                .collect(Collectors.toList());
    }
}

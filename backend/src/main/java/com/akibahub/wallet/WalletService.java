package com.akibahub.wallet;

import com.akibahub.audit.AuditLogService;
import com.akibahub.group.entity.GroupMemberRepository;
import com.akibahub.user.entity.User;
import com.akibahub.wallet.entity.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class WalletService {
    private final WalletRepository walletRepo;
    private final TransactionRepository transactionRepo;
    private final GroupMemberRepository memberRepo;
    private final AuditLogService auditLog;

    public WalletService(WalletRepository walletRepo, TransactionRepository transactionRepo,
                         GroupMemberRepository memberRepo, AuditLogService auditLog) {
        this.walletRepo = walletRepo;
        this.transactionRepo = transactionRepo;
        this.memberRepo = memberRepo;
        this.auditLog = auditLog;
    }

    public List<Wallet> getUserWallets(User user) {
        List<Long> groupIds = memberRepo.findByUserId(user.getId())
                .stream().map(m -> m.getGroup().getId()).toList();
        return walletRepo.findByUserIdOrGroupIdIn(user.getId(), groupIds);
    }

    @Transactional
    public Wallet depositToPersonal(User user, BigDecimal amount) {
        Wallet wallet = walletRepo.findByUserIdAndType(user.getId(), Wallet.WalletType.PERSONAL)
                .orElseThrow(() -> new RuntimeException("Personal wallet not found"));
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepo.save(wallet);
        transactionRepo.save(Transaction.builder().wallet(wallet).amount(amount).type(Transaction.TransactionType.DEPOSIT)
                .reference("Personal deposit").build());
        auditLog.logEvent("PERSONAL_DEPOSIT", Map.of("user", user.getPhoneNumber(), "amount", amount));
        return wallet;
    }

    @Transactional
    public Wallet withdrawFromPersonal(User user, BigDecimal amount) {
        Wallet wallet = walletRepo.findByUserIdAndType(user.getId(), Wallet.WalletType.PERSONAL)
                .orElseThrow(() -> new RuntimeException("Personal wallet not found"));
        if (wallet.getBalance().compareTo(amount) < 0) throw new RuntimeException("Insufficient balance");
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepo.save(wallet);
        transactionRepo.save(Transaction.builder().wallet(wallet).amount(amount).type(Transaction.TransactionType.WITHDRAWAL)
                .reference("Personal withdrawal").build());
        auditLog.logEvent("PERSONAL_WITHDRAWAL", Map.of("user", user.getPhoneNumber(), "amount", amount));
        return wallet;
    }

    @Transactional
    public void contributeToGroup(User user, Long groupId, BigDecimal amount) {
        memberRepo.findByGroupIdAndUserId(groupId, user.getId())
                .orElseThrow(() -> new RuntimeException("Not a member"));
        Wallet personal = walletRepo.findByUserIdAndType(user.getId(), Wallet.WalletType.PERSONAL).orElseThrow();
        if (personal.getBalance().compareTo(amount) < 0) throw new RuntimeException("Insufficient balance");
        Wallet groupWallet = walletRepo.findByGroupIdAndType(groupId, Wallet.WalletType.GROUP)
                .orElseThrow(() -> new RuntimeException("Group wallet not found"));

        personal.setBalance(personal.getBalance().subtract(amount));
        walletRepo.save(personal);
        groupWallet.setBalance(groupWallet.getBalance().add(amount));
        walletRepo.save(groupWallet);

        transactionRepo.save(Transaction.builder().wallet(personal).amount(amount).type(Transaction.TransactionType.WITHDRAWAL)
                .reference("Contribution to group " + groupId).build());
        transactionRepo.save(Transaction.builder().wallet(groupWallet).amount(amount).type(Transaction.TransactionType.DEPOSIT)
                .reference("Contribution from " + user.getPhoneNumber()).build());

        auditLog.logEvent("GROUP_CONTRIBUTION", Map.of("user", user.getPhoneNumber(), "amount", amount, "groupId", groupId));
    }
}
package com.akibahub.admin;

import com.akibahub.admin.dto.AdminDtos.*;
import com.akibahub.audit.entity.AuditLogRepository;
import com.akibahub.audit.AuditLogService;
import com.akibahub.group.entity.Group;
import com.akibahub.group.entity.GroupMemberRepository;
import com.akibahub.group.entity.GroupRepository;
import com.akibahub.proposal.entity.Proposal;
import com.akibahub.proposal.entity.ProposalRepository;
import com.akibahub.shared.exception.ForbiddenException;
import com.akibahub.shared.exception.NotFoundException;
import com.akibahub.user.entity.User;
import com.akibahub.user.entity.UserRepository;
import com.akibahub.wallet.entity.Wallet;
import com.akibahub.wallet.entity.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Every method here is only reachable through AdminController, which is
 * class-level @PreAuthorize("hasRole('ADMIN')") - this service assumes
 * that check has already happened and doesn't re-check it itself. There
 * is deliberately no way to become an ADMIN through any API endpoint;
 * promoting a user's role is an out-of-band (direct database) action.
 */
@Service
public class AdminService {

    private final UserRepository userRepo;
    private final GroupRepository groupRepo;
    private final GroupMemberRepository memberRepo;
    private final WalletRepository walletRepo;
    private final ProposalRepository proposalRepo;
    private final AuditLogRepository auditLogRepo;
    private final AuditLogService auditLog;

    public AdminService(UserRepository userRepo, GroupRepository groupRepo,
                        GroupMemberRepository memberRepo, WalletRepository walletRepo,
                        ProposalRepository proposalRepo, AuditLogRepository auditLogRepo,
                        AuditLogService auditLog) {
        this.userRepo = userRepo;
        this.groupRepo = groupRepo;
        this.memberRepo = memberRepo;
        this.walletRepo = walletRepo;
        this.proposalRepo = proposalRepo;
        this.auditLogRepo = auditLogRepo;
        this.auditLog = auditLog;
    }

    @Transactional(readOnly = true)
    public PlatformStats getStats() {
        return new PlatformStats(
                userRepo.count(),
                groupRepo.count(),
                proposalRepo.count(),
                proposalRepo.countByStatus(Proposal.ProposalStatus.OPEN),
                walletRepo.sumBalanceByType(Wallet.WalletType.PERSONAL),
                walletRepo.sumBalanceByType(Wallet.WalletType.GROUP)
        );
    }

    @Transactional(readOnly = true)
    public List<AdminUserSummary> getAllUsers() {
        return userRepo.findAll().stream()
                .map(u -> new AdminUserSummary(u.getId(), u.getFullName(), u.getPhoneNumber(),
                        u.getIdNumber(), u.getRole(), u.getStatus(), u.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AdminGroupSummary> getAllGroups() {
        List<Group> groups = groupRepo.findAll();
        return groups.stream().map(g -> {
            long memberCount = memberRepo.countByGroupId(g.getId());
            var balance = walletRepo.findByGroupIdAndType(g.getId(), Wallet.WalletType.GROUP)
                    .map(Wallet::getBalance).orElse(java.math.BigDecimal.ZERO);
            return new AdminGroupSummary(g.getId(), g.getName(), memberCount, balance, g.getCreatedAt());
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditLogEntry> getRecentAuditLog() {
        return auditLogRepo.findTop50ByOrderByCreatedAtDesc().stream()
                .map(a -> new AuditLogEntry(a.getId(), a.getEventType(), a.getPayload(), a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    /**
     * Suspends or reactivates a user. A suspended user is rejected at
     * login (see AuthService.login) even with a correct password - this
     * doesn't forcibly end any session they currently hold (access
     * tokens are still stateless and short-lived; they'll simply be
     * unable to log back in or refresh once their current token expires).
     */
    @Transactional
    public AdminUserSummary setUserStatus(Long userId, User.AccountStatus newStatus, User actingAdmin) {
        if (userId.equals(actingAdmin.getId())) {
            throw new ForbiddenException("You cannot change your own account status");
        }
        User target = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        target.setStatus(newStatus);
        userRepo.save(target);

        auditLog.logEvent("ADMIN_USER_STATUS_CHANGED", Map.of(
                "targetUserId", userId,
                "newStatus", newStatus.name(),
                "changedBy", actingAdmin.getId()
        ));

        return new AdminUserSummary(target.getId(), target.getFullName(), target.getPhoneNumber(),
                target.getIdNumber(), target.getRole(), target.getStatus(), target.getCreatedAt());
    }
}
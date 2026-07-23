package com.akibahub.group;

import com.akibahub.audit.AuditLogService;
import com.akibahub.group.dto.GroupMemberResponse;
import com.akibahub.group.dto.GroupResponse;
import com.akibahub.group.entity.*;
import com.akibahub.ledger.entity.LedgerEntry;
import com.akibahub.ledger.entity.LedgerEntryRepository;
import com.akibahub.proposal.entity.ProposalRepository;
import com.akibahub.shared.exception.BadRequestException;
import com.akibahub.shared.exception.ConflictException;
import com.akibahub.shared.exception.ForbiddenException;
import com.akibahub.shared.exception.NotFoundException;
import com.akibahub.user.entity.User;
import com.akibahub.wallet.entity.TransactionRepository;
import com.akibahub.wallet.entity.Wallet;
import com.akibahub.wallet.entity.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GroupService {
    private final GroupRepository groupRepo;
    private final GroupMemberRepository memberRepo;
    private final WalletRepository walletRepo;
    private final LedgerEntryRepository ledgerEntryRepo;
    private final ProposalRepository proposalRepo;
    private final TransactionRepository transactionRepo;
    private final AuditLogService auditLog;

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no ambiguous 0/O/1/I
    private static final SecureRandom RANDOM = new SecureRandom();

    public GroupService(GroupRepository groupRepo, GroupMemberRepository memberRepo,
                        WalletRepository walletRepo, LedgerEntryRepository ledgerEntryRepo,
                        ProposalRepository proposalRepo, TransactionRepository transactionRepo,
                        AuditLogService auditLog) {
        this.groupRepo = groupRepo;
        this.memberRepo = memberRepo;
        this.walletRepo = walletRepo;
        this.ledgerEntryRepo = ledgerEntryRepo;
        this.proposalRepo = proposalRepo;
        this.transactionRepo = transactionRepo;
        this.auditLog = auditLog;
    }

    @Transactional(readOnly = true)
    public GroupResponse getGroup(Long groupId, User user) {
        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));
        requireMembership(groupId, user.getId());
        return GroupResponse.from(group);
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> getMyGroups(User user) {
        return groupRepo.findByMemberUserId(user.getId()).stream()
                .map(GroupResponse::from)
                .toList();
    }

    @Transactional
    public GroupResponse createGroup(String name, String description, String rules, User creator) {
        if (name == null || name.isBlank()) {
            throw new BadRequestException("Group name is required");
        }
        String trimmedName = name.trim();
        if (trimmedName.length() > 100) {
            throw new BadRequestException("Group name must be 100 characters or fewer");
        }

        String code = generateUniqueCode();
        Group group = Group.builder()
                .name(trimmedName)
                .description(description == null ? null : description.trim())
                .rules(rules == null ? null : rules.trim())
                .inviteCode(code)
                .createdBy(creator)
                .build();
        group = groupRepo.save(group);

        memberRepo.save(GroupMember.builder().group(group).user(creator).build());
        walletRepo.save(Wallet.builder().group(group).type(Wallet.WalletType.GROUP).balance(BigDecimal.ZERO).build());

        auditLog.logEvent("GROUP_CREATED", Map.of(
                "groupId", group.getId(),
                "name", group.getName(),
                "createdBy", creator.getId()
        ));
        return GroupResponse.from(group);
    }

    @Transactional
    public GroupResponse joinGroup(String inviteCode, User user) {
        String normalized = inviteCode == null ? "" : inviteCode.trim();
        if (normalized.isBlank()) {
            throw new BadRequestException("Invite code required");
        }

        Group group = groupRepo.findByInviteCodeIgnoreCase(normalized)
                .orElseThrow(() -> new NotFoundException("Invalid invite code"));

        if (memberRepo.findByGroupIdAndUserId(group.getId(), user.getId()).isPresent()) {
            throw new ConflictException("You are already a member of this group");
        }

        memberRepo.save(GroupMember.builder().group(group).user(user).build());
        auditLog.logEvent("MEMBER_JOINED", Map.of("groupId", group.getId(), "user", user.getPhoneNumber()));
        return GroupResponse.from(group);
    }

    @Transactional
    public GroupResponse updateGroup(Long groupId, String name, String description, String rules, User user) {
        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));

        if (!group.getCreatedBy().getId().equals(user.getId())) {
            throw new ForbiddenException("Only the group creator can edit");
        }

        if (name != null) group.setName(name);
        if (description != null) group.setDescription(description);
        if (rules != null) group.setRules(rules);

        return GroupResponse.from(groupRepo.save(group));
    }

    @Transactional
    public void deleteGroup(Long groupId, User user) {
        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));

        if (!group.getCreatedBy().getId().equals(user.getId())) {
            throw new ForbiddenException("Only the group creator can delete");
        }

        Wallet groupWallet = walletRepo.findByGroupIdAndType(groupId, Wallet.WalletType.GROUP)
                .orElseThrow(() -> new NotFoundException("Group wallet not found"));

        if (groupWallet.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new ConflictException("Group wallet must be empty before deletion");
        }

        if (proposalRepo.countByGroupId(groupId) > 0) {
            throw new ConflictException("Cannot delete a group that has proposals");
        }

        List<Long> walletIds = List.of(groupWallet.getId());
        if (!transactionRepo.findByWalletIdIn(walletIds).isEmpty()) {
            throw new ConflictException("Cannot delete a group that has transaction history");
        }

        if (!ledgerEntryRepo.findByWalletIdOrderByCreatedAtAsc(groupWallet.getId()).isEmpty()) {
            throw new ConflictException("Cannot delete a group that has ledger history");
        }

        memberRepo.deleteByGroupId(groupId);
        walletRepo.delete(groupWallet);
        groupRepo.delete(group);

        auditLog.logEvent("GROUP_DELETED", Map.of("groupId", groupId, "deletedBy", user.getId()));
    }

    @Transactional
    public String generateInviteCode(Long groupId, User user) {
        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));

        if (!group.getCreatedBy().getId().equals(user.getId())) {
            throw new ForbiddenException("Only the group creator can generate invite code");
        }

        if (group.getInviteCode() == null || group.getInviteCode().isBlank()) {
            String code = generateUniqueCode();
            group.setInviteCode(code);
            groupRepo.save(group);
        }
        return group.getInviteCode();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getGroupStats(Long groupId, User user) {
        groupRepo.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));

        requireMembership(groupId, user.getId());

        Wallet groupWallet = walletRepo.findByGroupIdAndType(groupId, Wallet.WalletType.GROUP)
                .orElseThrow(() -> new NotFoundException("Group wallet not found"));

        long memberCount = memberRepo.countByGroupId(groupId);
        return Map.of(
            "totalSavings", groupWallet.getBalance(),
            "members", memberCount
        );
    }

    @Transactional(readOnly = true)
    public List<GroupMemberResponse> getGroupMembers(Long groupId, User user) {
        requireMembership(groupId, user.getId());
        return memberRepo.findByGroupId(groupId).stream()
                .map(GroupMemberResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getGroupGrowth(Long groupId, User user) {
        requireMembership(groupId, user.getId());
        Wallet groupWallet = walletRepo.findByGroupIdAndType(groupId, Wallet.WalletType.GROUP)
                .orElseThrow(() -> new NotFoundException("Group wallet not found"));

        List<LedgerEntry> entries = ledgerEntryRepo.findByWalletIdOrderByCreatedAtAsc(groupWallet.getId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM");

        List<Map<String, Object>> series = new ArrayList<>();
        for (LedgerEntry entry : entries) {
            series.add(Map.of(
                    "label", entry.getCreatedAt().format(formatter),
                    "value", entry.getBalanceAfter()
            ));
        }
        return series;
    }

    private void requireMembership(Long groupId, Long userId) {
        memberRepo.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ForbiddenException("Not a member of this group"));
    }

    private String generateUniqueCode() {
        for (int attempt = 0; attempt < 20; attempt++) {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
            }
            String code = sb.toString();
            if (groupRepo.findByInviteCodeIgnoreCase(code).isEmpty()) {
                return code;
            }
        }
        throw new ConflictException("Could not generate a unique invite code. Please try again.");
    }
}

package com.akibahub.group;

import com.akibahub.audit.AuditLogService;
import com.akibahub.group.entity.*;
import com.akibahub.shared.exception.ConflictException;
import com.akibahub.shared.exception.ForbiddenException;
import com.akibahub.shared.exception.NotFoundException;
import com.akibahub.user.entity.User;
import com.akibahub.wallet.entity.Wallet;
import com.akibahub.wallet.entity.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

@Service
public class GroupService {
    private final GroupRepository groupRepo;
    private final GroupMemberRepository memberRepo;
    private final WalletRepository walletRepo;
    private final AuditLogService auditLog;
    
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public GroupService(GroupRepository groupRepo, GroupMemberRepository memberRepo,
                        WalletRepository walletRepo, AuditLogService auditLog) {
        this.groupRepo = groupRepo;
        this.memberRepo = memberRepo;
        this.walletRepo = walletRepo;
        this.auditLog = auditLog;
    }

    @Transactional(readOnly = true)
    public Group getGroup(Long groupId, User user) {
        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));
        requireMembership(groupId, user.getId());
        return group;
    }

    @Transactional(readOnly = true)
    public List<Group> getMyGroups(User user) {
        return groupRepo.findByMemberUserId(user.getId());
    }

    @Transactional
    public Group createGroup(String name, String description, String rules, User creator) {
        String code = generateUniqueCode();
        Group group = Group.builder()
                .name(name)
                .description(description)
                .rules(rules)
                .inviteCode(code)
                .createdBy(creator)
                .build();
        group = groupRepo.save(group);
        
        memberRepo.save(GroupMember.builder().group(group).user(creator).build());
        walletRepo.save(Wallet.builder().group(group).type(Wallet.WalletType.GROUP).balance(BigDecimal.ZERO).build());
        
        auditLog.logEvent("GROUP_CREATED", group);
        return group;
    }

    @Transactional
    public void joinGroup(String inviteCode, User user) {
        Group group = groupRepo.findByInviteCode(inviteCode)
                .orElseThrow(() -> new NotFoundException("Invalid invite code"));
        
        if (memberRepo.findByGroupIdAndUserId(group.getId(), user.getId()).isPresent()) {
            throw new ConflictException("Already a member");
        }
        
        memberRepo.save(GroupMember.builder().group(group).user(user).build());
        auditLog.logEvent("MEMBER_JOINED", Map.of("groupId", group.getId(), "user", user.getPhoneNumber()));
    }

    @Transactional
    public Group updateGroup(Long groupId, String name, String description, String rules, User user) {
        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));
        
        if (!group.getCreatedBy().getId().equals(user.getId())) {
            throw new ForbiddenException("Only the group creator can edit");
        }
        
        if (name != null) group.setName(name);
        if (description != null) group.setDescription(description);
        if (rules != null) group.setRules(rules);
        
        return groupRepo.save(group);
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
    public List<Group> getAllGroups() { 
        return groupRepo.findAll(); 
    }

    @Transactional(readOnly = true)
    public List<GroupMember> getGroupMembers(Long groupId, User user) {
        requireMembership(groupId, user.getId());
        return memberRepo.findByGroupId(groupId);
    }

    // Shared by every group-scoped read that should only be visible to
    // members of that group (getGroup, getGroupMembers, getGroupStats).
    // Previously each method either duplicated this check inline
    // (getGroupStats) or - in the case of getGroup and getGroupMembers -
    // simply didn't have it at all, which meant any authenticated user
    // could view any group's details or list every member's name and
    // phone number, regardless of whether they'd ever joined that group.
    private void requireMembership(Long groupId, Long userId) {
        memberRepo.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ForbiddenException("Not a member of this group"));
    }

    private String generateUniqueCode() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
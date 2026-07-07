package com.akibahub.group;

import com.akibahub.audit.AuditLogService;
import com.akibahub.group.entity.*;
import com.akibahub.user.entity.User;
import com.akibahub.wallet.entity.Wallet;
import com.akibahub.wallet.entity.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class GroupService {
    private final GroupRepository groupRepo;
    private final GroupMemberRepository memberRepo;
    private final WalletRepository walletRepo;
    private final AuditLogService auditLog;

    public GroupService(GroupRepository groupRepo, GroupMemberRepository memberRepo,
                        WalletRepository walletRepo, AuditLogService auditLog) {
        this.groupRepo = groupRepo;
        this.memberRepo = memberRepo;
        this.walletRepo = walletRepo;
        this.auditLog = auditLog;
    }

    public Group getGroup(Long groupId) {
        return groupRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
    }

    public List<Group> getMyGroups(User user) {
        return groupRepo.findByMemberUserId(user.getId());
    }

    public Group updateGroup(Long groupId, String name, String description, User user) {
        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        if (!group.getCreatedBy().getId().equals(user.getId())) {
            throw new RuntimeException("Only the group creator can edit");
        }
        if (name != null) group.setName(name);
        if (description != null) group.setDescription(description);
        return groupRepo.save(group);
    }

    public void deleteGroup(Long groupId, User user) {
        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        if (!group.getCreatedBy().getId().equals(user.getId())) {
            throw new RuntimeException("Only the group creator can delete");
        }
        Wallet groupWallet = walletRepo.findByGroupIdAndType(groupId, Wallet.WalletType.GROUP)
                .orElseThrow(() -> new RuntimeException("Group wallet not found"));
        if (groupWallet.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new RuntimeException("Group wallet must be empty before deletion");
        }
        memberRepo.deleteByGroupId(groupId);
        walletRepo.delete(groupWallet);
        groupRepo.delete(group);
    }

    public String generateInviteCode(Long groupId, User user) {
        // MVP: return the group detail URL; a real token can be added later
        return "https://akiba.unitybridge.dev/group.html?id=" + groupId;
    }

    public Map<String, Object> getGroupStats(Long groupId, User user) {
        // check existence and membership
        groupRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        memberRepo.findByGroupIdAndUserId(groupId, user.getId())
                .orElseThrow(() -> new RuntimeException("Not a member"));
        Wallet groupWallet = walletRepo.findByGroupIdAndType(groupId, Wallet.WalletType.GROUP)
                .orElseThrow(() -> new RuntimeException("Group wallet not found"));
        long memberCount = memberRepo.countByGroupId(groupId);
        return Map.of(
            "totalSavings", groupWallet.getBalance(),
            "members", memberCount
        );
    }

    @Transactional
    public Group createGroup(String name, String description, User creator) {
        Group group = Group.builder().name(name).description(description).createdBy(creator).build();
        group = groupRepo.save(group);
        memberRepo.save(GroupMember.builder().group(group).user(creator).build());
        walletRepo.save(Wallet.builder().group(group).type(Wallet.WalletType.GROUP).balance(BigDecimal.ZERO).build());
        auditLog.logEvent("GROUP_CREATED", group);
        return group;
    }

    @Transactional
    public void joinGroup(Long groupId, User user) {
        Group group = groupRepo.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));
        if (memberRepo.findByGroupIdAndUserId(groupId, user.getId()).isPresent())
            throw new RuntimeException("Already a member");
        memberRepo.save(GroupMember.builder().group(group).user(user).build());
        auditLog.logEvent("MEMBER_JOINED", Map.of("groupId", groupId, "user", user.getPhoneNumber()));
    }

    public List<Group> getAllGroups() { return groupRepo.findAll(); }

    public List<GroupMember> getGroupMembers(Long groupId) { return memberRepo.findByGroupId(groupId); }
}
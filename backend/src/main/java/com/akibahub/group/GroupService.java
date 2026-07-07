package com.akibahub.group;

import com.akibahub.audit.AuditLogService;
import com.akibahub.group.entity.*;
import com.akibahub.user.entity.User;
import com.akibahub.wallet.entity.Wallet;
import com.akibahub.wallet.entity.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.math.BigDecimal;
import java.util.List;

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
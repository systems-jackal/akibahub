package com.akibahub.group.service;

import com.akibahub.group.model.*;
import com.akibahub.group.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final InviteCodeRepository inviteCodeRepository;
    private final AuditPublisher auditPublisher;

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${invite.code.length:8}")
    private int inviteCodeLength;

    @Value("${invite.code.expiry-hours:48}")
    private int inviteCodeExpiryHours;

    @Transactional
    public Group createGroup(String userId, String userEmail,
                             String name, String description) {
        Group group = Group.builder()
                .name(name)
                .description(description)
                .createdBy(userId)
                .build();
        Group saved = groupRepository.save(group);

        // Creator becomes ADMIN automatically
        GroupMember admin = GroupMember.builder()
                .groupId(saved.getId())
                .userId(userId)
                .userEmail(userEmail)
                .role(GroupRole.ADMIN)
                .build();
        groupMemberRepository.save(admin);

        auditPublisher.publish("GROUP_CREATED", userId, userEmail,
                "group", saved.getId(), saved.getId(),
                "name:" + name);

        log.info("Group created: {} by {}", saved.getId(), userId);
        return saved;
    }

    @Transactional
    public GroupMember joinGroup(String userId, String userEmail,
                                 String displayName, String inviteCode) {
        InviteCode invite = inviteCodeRepository.findByCode(inviteCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid invite code"));

        if (!invite.isValid()) {
            throw new IllegalArgumentException("Invite code is expired or exhausted");
        }

        if (groupMemberRepository.existsByGroupIdAndUserId(invite.getGroupId(), userId)) {
            throw new IllegalStateException("Already a member of this group");
        }

        Group group = groupRepository.findById(invite.getGroupId())
                .orElseThrow(() -> new IllegalStateException("Group not found"));

        long memberCount = groupMemberRepository.countByGroupIdAndActiveTrue(invite.getGroupId());
        if (memberCount >= group.getMaxMembers()) {
            throw new IllegalStateException("Group is full");
        }

        GroupMember member = GroupMember.builder()
                .groupId(invite.getGroupId())
                .userId(userId)
                .userEmail(userEmail)
                .displayName(displayName)
                .role(GroupRole.MEMBER)
                .build();

        GroupMember saved = groupMemberRepository.save(member);
        invite.recordUse();
        inviteCodeRepository.save(invite);

        auditPublisher.publish("MEMBER_JOINED", userId, userEmail,
                "group_member", saved.getId(), invite.getGroupId(),
                "via_invite:" + inviteCode);

        return saved;
    }

    @Transactional
    public InviteCode generateInviteCode(String groupId, String requestingUserId) {
        // Only ADMIN or TREASURER can generate invite codes
        boolean isAuthorized = groupMemberRepository
                .existsByGroupIdAndUserIdAndRole(groupId, requestingUserId, GroupRole.ADMIN)
                || groupMemberRepository
                .existsByGroupIdAndUserIdAndRole(groupId, requestingUserId, GroupRole.TREASURER);

        if (!isAuthorized) {
            throw new SecurityException("Only admins and treasurers can generate invite codes");
        }

        String code = generateUniqueCode();
        InviteCode inviteCode = InviteCode.builder()
                .code(code)
                .groupId(groupId)
                .createdBy(requestingUserId)
                .expiresAt(LocalDateTime.now().plusHours(inviteCodeExpiryHours))
                .build();

        InviteCode saved = inviteCodeRepository.save(inviteCode);

        auditPublisher.publish("INVITE_CODE_GENERATED", requestingUserId, null,
                "invite_code", saved.getId(), groupId, "code:" + code);

        return saved;
    }

    @Transactional
    public void changeMemberRole(String groupId, String targetUserId,
                                 GroupRole newRole, String requestingUserId) {
        boolean isAdmin = groupMemberRepository
                .existsByGroupIdAndUserIdAndRole(groupId, requestingUserId, GroupRole.ADMIN);
        if (!isAdmin) {
            throw new SecurityException("Only admins can change member roles");
        }

        GroupMember member = groupMemberRepository
                .findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        GroupRole previousRole = member.getRole();
        member.setRole(newRole);
        groupMemberRepository.save(member);

        auditPublisher.publish("MEMBER_ROLE_CHANGED", requestingUserId, null,
                "group_member", member.getId(), groupId,
                "from:" + previousRole + " to:" + newRole);
    }

    @Transactional
    public void removeMember(String groupId, String targetUserId, String requestingUserId) {
        boolean isAdmin = groupMemberRepository
                .existsByGroupIdAndUserIdAndRole(groupId, requestingUserId, GroupRole.ADMIN);
        if (!isAdmin) {
            throw new SecurityException("Only admins can remove members");
        }

        GroupMember member = groupMemberRepository
                .findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        member.setActive(false);
        groupMemberRepository.save(member);

        auditPublisher.publish("MEMBER_REMOVED", requestingUserId, null,
                "group_member", member.getId(), groupId,
                "removed_user:" + targetUserId);
    }

    @Transactional(readOnly = true)
    public Group getGroup(String groupId, String requestingUserId) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, requestingUserId)) {
            throw new SecurityException("Not a member of this group");
        }
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
    }

    @Transactional(readOnly = true)
    public List<GroupMember> getMembers(String groupId, String requestingUserId) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, requestingUserId)) {
            throw new SecurityException("Not a member of this group");
        }
        return groupMemberRepository.findByGroupIdAndActiveTrue(groupId);
    }

    @Transactional(readOnly = true)
    public List<Group> getUserGroups(String userId) {
        List<GroupMember> memberships = groupMemberRepository.findByUserIdAndActiveTrue(userId);
        return memberships.stream()
                .map(m -> groupRepository.findById(m.getGroupId()).orElseThrow())
                .toList();
    }

    // Internal — called by proposal-service to verify membership
    @Transactional(readOnly = true)
    public boolean isMember(String groupId, String userId) {
        return groupMemberRepository.existsByGroupIdAndUserId(groupId, userId);
    }

    private String generateUniqueCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(inviteCodeLength);
            for (int i = 0; i < inviteCodeLength; i++) {
                sb.append(CHARS.charAt(secureRandom.nextInt(CHARS.length())));
            }
            code = sb.toString();
        } while (inviteCodeRepository.existsByCode(code));
        return code;
    }
}

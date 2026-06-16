package com.akibahub.service.impl;

import com.akibahub.model.Group;
import com.akibahub.model.GroupMember;
import com.akibahub.model.InviteCode;
import com.akibahub.repository.GroupMemberRepository;
import com.akibahub.repository.GroupRepository;
import com.akibahub.repository.InviteCodeRepository;
import com.akibahub.service.GroupService;
import com.akibahub.service.LedgerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final InviteCodeRepository inviteCodeRepository;
    private final LedgerService ledgerService;

    public GroupServiceImpl(GroupRepository groupRepository,
                            GroupMemberRepository memberRepository,
                            InviteCodeRepository inviteCodeRepository,
                            LedgerService ledgerService) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.inviteCodeRepository = inviteCodeRepository;
        this.ledgerService = ledgerService;
    }

    @Override
    @Transactional
    public Group createGroup(String name, String description, Long adminId) {
        Group group = new Group();
        group.setName(name);
        group.setDescription(description);
        group.setAdminId(adminId);
        group = groupRepository.save(group);

        GroupMember member = new GroupMember();
        member.setGroupId(group.getId());
        member.setUserId(adminId);
        member.setRole("ADMIN");
        memberRepository.save(member);

        ledgerService.log("GROUP_CREATED", "GROUP", group.getId(),
                Map.of("name", name, "adminId", adminId), adminId, group.getId());
        return group;
    }

    @Override
    @Transactional
    public String generateInviteCode(Long groupId, Long creatorId) {
        String code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        InviteCode invite = new InviteCode();
        invite.setCode(code);
        invite.setGroupId(groupId);
        invite.setCreatedBy(creatorId);
        invite.setExpiresAt(LocalDateTime.now().plusDays(7));
        inviteCodeRepository.save(invite);
        return code;
    }

    @Override
    @Transactional
    public GroupMember joinGroup(String code, Long userId) {
        InviteCode invite = inviteCodeRepository.findByCodeAndIsActiveTrue(code)
                .orElseThrow(() -> new RuntimeException("Invalid or expired invite code"));
        if (invite.getUsedCount() >= invite.getMaxUses()) {
            throw new RuntimeException("Invite code max uses exceeded");
        }
        invite.setUsedCount(invite.getUsedCount() + 1);
        inviteCodeRepository.save(invite);

        GroupMember member = new GroupMember();
        member.setGroupId(invite.getGroupId());
        member.setUserId(userId);
        member = memberRepository.save(member);

        ledgerService.log("MEMBER_JOINED", "GROUP_MEMBER", member.getId(),
                Map.of("groupId", invite.getGroupId()), userId, invite.getGroupId());
        return member;
    }

    @Override
    public List<GroupMember> getGroupMembers(Long groupId) {
        return memberRepository.findByGroupId(groupId);
    }
}
package com.akibahub.service.impl;

import com.akibahub.dto.request.CreateGroupRequest;
import com.akibahub.dto.request.JoinGroupRequest;
import com.akibahub.dto.response.GroupResponse;
import com.akibahub.model.*;
import com.akibahub.repository.GroupMemberRepository;
import com.akibahub.repository.GroupRepository;
import com.akibahub.service.GroupService;
import com.akibahub.util.InviteCodeGenerator;
import org.springframework.stereotype.Service;

@Service
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;

    public GroupServiceImpl(GroupRepository groupRepository,
                            GroupMemberRepository memberRepository) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    public GroupResponse createGroup(CreateGroupRequest request) {

        Group group = Group.builder()
                .name(request.getName())
                .createdBy(request.getCreatedBy())
                .inviteCode(InviteCodeGenerator.generateCode())
                .totalBalance(0.0)
                .build();

        Group saved = groupRepository.save(group);

        // auto-add creator as admin
        GroupMember admin = GroupMember.builder()
                .groupId(saved.getId())
                .userId(request.getCreatedBy())
                .role(GroupRole.ADMIN)
                .build();

        memberRepository.save(admin);

        return map(saved);
    }

    @Override
    public GroupResponse joinGroup(JoinGroupRequest request) {

        Group group = groupRepository.findByInviteCode(request.getInviteCode())
                .orElseThrow(() -> new RuntimeException("Invalid invite code"));

        GroupMember member = GroupMember.builder()
                .groupId(group.getId())
                .userId(request.getUserId())
                .role(GroupRole.MEMBER)
                .build();

        memberRepository.save(member);

        return map(group);
    }

    private GroupResponse map(Group group) {
        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .inviteCode(group.getInviteCode())
                .totalBalance(group.getTotalBalance())
                .createdBy(group.getCreatedBy())
                .build();
    }
}
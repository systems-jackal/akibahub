package com.akibahub.service;

import com.akibahub.model.*;
import com.akibahub.repository.GroupMemberRepository;
import com.akibahub.repository.GroupRepository;
import com.akibahub.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    public GroupService(GroupRepository groupRepository,
                        GroupMemberRepository groupMemberRepository,
                        UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
    }

    // CREATE GROUP
    public Group createGroup(Long userId, String name, String description) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Group group = new Group();
        group.setName(name);
        group.setDescription(description);
        group.setInviteCode(generateInviteCode());
        group.setCreatedBy(user);

        Group savedGroup = groupRepository.save(group);

        // creator becomes ADMIN
        GroupMember member = new GroupMember();
        member.setGroup(savedGroup);
        member.setUser(user);
        member.setRole(GroupRole.ADMIN);

        groupMemberRepository.save(member);

        return savedGroup;
    }

    // JOIN GROUP
    public GroupMember joinGroup(Long userId, String inviteCode) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Group group = groupRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new RuntimeException("Invalid invite code"));

        GroupMember member = new GroupMember();
        member.setGroup(group);
        member.setUser(user);
        member.setRole(GroupRole.MEMBER);

        return groupMemberRepository.save(member);
    }

    private String generateInviteCode() {
        return "GRP-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
package com.akibahub.service;

import com.akibahub.model.Group;
import com.akibahub.model.GroupMember;

import java.util.List;

public interface GroupService {
    Group createGroup(String name, String description, Long adminId);
    String generateInviteCode(Long groupId, Long creatorId);
    GroupMember joinGroup(String code, Long userId);
    List<GroupMember> getGroupMembers(Long groupId);
}
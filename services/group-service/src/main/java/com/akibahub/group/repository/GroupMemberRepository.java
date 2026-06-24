package com.akibahub.group.repository;

import com.akibahub.group.model.GroupMember;
import com.akibahub.group.model.GroupRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, String> {
    boolean existsByGroupIdAndUserId(String groupId, String userId);
    boolean existsByGroupIdAndUserIdAndRole(String groupId, String userId, GroupRole role);
    Optional<GroupMember> findByGroupIdAndUserId(String groupId, String userId);
    List<GroupMember> findByGroupIdAndActiveTrue(String groupId);
    List<GroupMember> findByUserIdAndActiveTrue(String userId);
    long countByGroupIdAndActiveTrue(String groupId);
}

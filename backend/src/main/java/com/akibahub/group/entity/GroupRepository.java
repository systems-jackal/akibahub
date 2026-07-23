package com.akibahub.group.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {

    @Query("SELECT g FROM Group g JOIN GroupMember m ON g.id = m.group.id WHERE m.user.id = :userId")
    List<Group> findByMemberUserId(@Param("userId") Long userId);

    Optional<Group> findByInviteCodeIgnoreCase(String inviteCode);
}
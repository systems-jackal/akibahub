package com.akibahub.group.repository;

import com.akibahub.group.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, String> {
    List<Group> findByCreatedBy(String userId);
    boolean existsByIdAndActive(String id, boolean active);
}

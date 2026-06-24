package com.akibahub.group.repository;

import com.akibahub.group.model.InviteCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InviteCodeRepository extends JpaRepository<InviteCode, String> {
    Optional<InviteCode> findByCode(String code);
    boolean existsByCode(String code);
}

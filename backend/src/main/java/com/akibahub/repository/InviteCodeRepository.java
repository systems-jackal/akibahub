package com.akibahub.repository;

import com.akibahub.model.InviteCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InviteCodeRepository extends JpaRepository<InviteCode, Long> {
    Optional<InviteCode> findByCodeAndIsActiveTrue(String code);
}
package com.akibahub.repository;

import com.akibahub.model.GroupWithdrawalRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupWithdrawalRepository extends JpaRepository<GroupWithdrawalRequest, Long> {
}
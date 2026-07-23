package com.akibahub.payments.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PendingPaymentRepository extends JpaRepository<PendingPayment, Long> {
    Optional<PendingPayment> findByReference(String reference);

    Optional<PendingPayment> findByReferenceAndUser_Id(String reference, Long userId);
}

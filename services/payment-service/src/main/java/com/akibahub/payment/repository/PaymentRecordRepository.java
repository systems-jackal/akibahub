package com.akibahub.payment.repository;

import com.akibahub.payment.model.PaymentRecord;
import com.akibahub.payment.model.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, String> {
    Optional<PaymentRecord> findByInternalReference(String internalReference);
    Optional<PaymentRecord> findByPayheroReference(String payheroReference);
    boolean existsByInternalReferenceAndStatus(String reference, PaymentStatus status);
    Page<PaymentRecord> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
}

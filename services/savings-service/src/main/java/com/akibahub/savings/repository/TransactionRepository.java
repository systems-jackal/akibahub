package com.akibahub.savings.repository;

import com.akibahub.savings.model.Transaction;
import com.akibahub.savings.model.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    Page<Transaction> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    Optional<Transaction> findByPaymentReference(String paymentReference);
    boolean existsByPaymentReferenceAndStatus(String reference, TransactionStatus status);
}

package com.akibahub.savings.repository;

import com.akibahub.savings.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, String> {
    Optional<Wallet> findByUserId(String userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Wallet> findWithLockByUserId(String userId);
}

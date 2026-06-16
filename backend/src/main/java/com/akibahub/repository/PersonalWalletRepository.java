package com.akibahub.repository;

import com.akibahub.model.PersonalWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonalWalletRepository extends JpaRepository<PersonalWallet, Long> {
    Optional<PersonalWallet> findByUserId(Long userId);
}
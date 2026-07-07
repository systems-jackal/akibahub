package com.akibahub.wallet.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByUserIdAndType(Long userId, Wallet.WalletType type);
    Optional<Wallet> findByGroupIdAndType(Long groupId, Wallet.WalletType type);
    @Query("SELECT w FROM Wallet w WHERE w.user.id = :userId OR w.group.id IN :groupIds")
    List<Wallet> findByUserIdOrGroupIdIn(Long userId, List<Long> groupIds);
}
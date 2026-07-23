package com.akibahub.auth.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    /**
     * JOIN FETCH the user so AuthService.refresh can call user.toDto()
     * after this transaction ends (open-in-view is false).
     */
    @Query("SELECT rt FROM RefreshToken rt JOIN FETCH rt.user WHERE rt.tokenHash = :tokenHash AND rt.revoked = false")
    Optional<RefreshToken> findByTokenHashAndRevokedFalse(@Param("tokenHash") String tokenHash);

    List<RefreshToken> findByUserIdAndRevokedFalse(Long userId);
}
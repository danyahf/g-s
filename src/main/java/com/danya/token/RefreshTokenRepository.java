package com.danya.token;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(UUID uuid);

    void deleteByToken(UUID uuid);

    void deleteAllByUserId(Long userId);

    long deleteByExpiresAtBefore(LocalDateTime cutoff);
}

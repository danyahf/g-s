package com.danya.token;

import com.danya.exception.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    @Value("${auth.refresh-token.ttl}")
    private Duration tokenTtl;

    public RefreshToken issueRefreshToken(Long userId) {
        RefreshToken token = new RefreshToken(userId, tokenTtl);
        refreshTokenRepository.save(token);
        return token;
    }

    public RefreshToken findByUuid(UUID token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("The refresh token is invalid or expired"));
    }

    public void removeByUuid(UUID token) {
        refreshTokenRepository.deleteByToken(token);
    }

    @Transactional
    public void removeAll(long userId) {
        refreshTokenRepository.deleteAllByUserId(userId);
    }

    /**
     * Periodic cleanup of expired refresh tokens.
     * <p>
     * Runs every day at 03:00.
     * Cron format: second minute hour day-of-month month day-of-week
     * "0 0 3 * * *" = 03:00:00 every day
     */
    @Transactional
    @Scheduled(cron = "0 0 3 * * *")
    public void purgeExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        long deleted = refreshTokenRepository.deleteByExpiresAtBefore(now);

        if (deleted > 0) {
            log.info("Purged {} expired refresh tokens", deleted);
        }
    }
}

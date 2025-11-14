package com.danya.security.bruteForceProtection;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Component
public class InMemoryBruteForceProtector implements BruteForceProtector {
    private final int maxFailures;
    private final Duration lockDuration;

    private final Map<String, UserLoginState> userAttempts = new ConcurrentHashMap<>();

    public InMemoryBruteForceProtector(
            @Value("${security.bruteforce.max-failures:3}") int maxFailures,
            @Value("${security.bruteforce.lock-duration:5m}") Duration lockDuration
    ) {
        this.maxFailures = maxFailures;
        this.lockDuration = lockDuration;
    }

    @Override
    public void assertLoginAllowed(String username) {
        UserLoginState userLoginState = userAttempts.get(username);
        if (userLoginState == null) {
            return;
        }

        if (userLoginState.isCurrentlyLocked()) {
            throw new TooManyLoginAttemptsException("Too many login attempts. Please try again later");
        }
    }

    @Override
    public void recordLoginFailure(String username) {
        UserLoginState state = userAttempts.computeIfAbsent(username, u -> new UserLoginState());
        state.incrementFailures();

        if (state.failedAttempts >= maxFailures) {
            state.lockUntil(Instant.now().plus(lockDuration));
            log.warn("User '{}' locked until {}", username, state.lockedUntil);
        }

        userAttempts.put(username, state);
    }

    @Override
    public void recordLoginSuccess(String username) {
        UserLoginState userLoginState = userAttempts.get(username);
        if (userLoginState == null) {
            return;
        }
        userLoginState.resetFailures();
        userLoginState.unlock();
    }

    static class UserLoginState {
        private int failedAttempts = 0;
        private Instant lockedUntil;

        public void incrementFailures() {
            failedAttempts += 1;
        }

        public void lockUntil(Instant expirationTime) {
            lockedUntil = expirationTime;
        }

        public void resetFailures() {
            failedAttempts = 0;
        }

        public void unlock() {
            lockedUntil = null;
        }

        public boolean isCurrentlyLocked() {
            if (lockedUntil == null) {
                return false;
            }

            if (Instant.now().isAfter(lockedUntil)) {
                unlock();
                return false;
            }

            return true;
        }
    }
}

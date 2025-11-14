package com.danya.bruteForce;


import com.danya.security.bruteForceProtection.InMemoryBruteForceProtector;
import com.danya.security.bruteForceProtection.TooManyLoginAttemptsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryBruteForceProtectorTest {

    private InMemoryBruteForceProtector service;
    private static final String USER = "john.doe";

    @BeforeEach
    void setUp() {
        service = new InMemoryBruteForceProtector(3, Duration.ofSeconds(1));
    }

    @Test
    void assertLoginAllowedDoesNotThrowWhenUserNotLocked() {
        assertDoesNotThrow(() -> service.assertLoginAllowed(USER));
    }

    @Test
    void recordLoginFailureIncrementsAttemptsAndLocksAfterMaxFailures() {
        service.recordLoginFailure(USER);
        service.recordLoginFailure(USER);
        service.recordLoginFailure(USER);

        assertThrows(TooManyLoginAttemptsException.class, () -> service.assertLoginAllowed(USER));
    }

    @Test
    void recordLoginSuccessResetsFailuresAndUnlocksUser() {
        service.recordLoginFailure(USER);
        service.recordLoginFailure(USER);
        service.recordLoginFailure(USER);

        service.recordLoginSuccess(USER);
        assertDoesNotThrow(() -> service.assertLoginAllowed(USER));
    }

    @Test
    void lockExpiresAfterDuration() {
        service = new InMemoryBruteForceProtector(3, Duration.ofNanos(1));

        service.recordLoginFailure(USER);
        service.recordLoginFailure(USER);
        service.recordLoginFailure(USER);

        assertDoesNotThrow(() -> service.assertLoginAllowed(USER));
    }
}

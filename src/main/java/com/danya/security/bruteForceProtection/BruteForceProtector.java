package com.danya.security.bruteForceProtection;


public interface BruteForceProtector {

    /**
     * Check if the user is allowed to attempt login right now.
     * If not allowed, this method MUST throw TooManyLoginAttemptsException.
     * If allowed, it returns normally and does not mutate counters.
     */
    void assertLoginAllowed(String username);

    /**
     * Record an unsuccessful login attempt for this username.
     * This may increase the failedAttempts counter or start a lock.
     */
    void recordLoginFailure(String username);

    /**
     * Record a successful login attempt for this username.
     * This should reset counters and clear any lock.
     */
    void recordLoginSuccess(String username);
}


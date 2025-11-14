package com.danya.security.bruteForceProtection;

public class TooManyLoginAttemptsException extends RuntimeException {
    public TooManyLoginAttemptsException(String message) {
        super(message);
    }
}

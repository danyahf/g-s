package com.danya.workload.api.exception;

public class InvalidRequestException extends ClientErrorException {
    public InvalidRequestException(String message) {
        super(message);
    }
}

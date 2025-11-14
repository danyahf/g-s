package com.danya.workload.api.exception;

public class ResourceNotFoundException extends ClientErrorException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

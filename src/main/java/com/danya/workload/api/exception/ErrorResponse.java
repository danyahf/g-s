package com.danya.workload.api.exception;

public record ErrorResponse(
        int status,
        String error,
        String message,
        String path
) {
}

package com.danya.workload.api.client;

import com.danya.workload.api.exception.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class WorkloadErrorDecoder implements ErrorDecoder {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Exception decode(String s, Response response) {
        ErrorResponse errorResponse = parseErrorResponse(response);
        logError(errorResponse);
        int status = errorResponse.status();

        if (status >= 400 && status < 500) {
            return switch (status) {
                case 400 -> new InvalidRequestException(errorResponse.message());
                case 404 -> new ResourceNotFoundException(errorResponse.message());
                default -> new ClientErrorException(errorResponse.message());
            };
        }

        //5xx
        return new WorkloadServiceException(errorResponse.message());
    }

    private void logError(ErrorResponse errorResponse) {
        String logMessage = "[{}] Workload service error - status {}: {}";
        log.error(logMessage,
                errorResponse.error(),
                errorResponse.status(),
                errorResponse.message());
    }

    private ErrorResponse parseErrorResponse(Response response) {
        try {
            return objectMapper.readValue(response.body().asInputStream(), ErrorResponse.class);
        } catch (IOException e) {
            log.error("Failed to parse error response from workload service. Status: {}, Reason: {}",
                    response.status(), response.reason(), e);

            return new ErrorResponse(
                    response.status(),
                    response.reason() != null ? response.reason() : "Unknown Error",
                    "Unable to parse error details from workload service",
                    "unknown"
            );
        }
    }
}

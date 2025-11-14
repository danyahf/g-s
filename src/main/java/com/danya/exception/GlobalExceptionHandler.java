package com.danya.exception;


import com.danya.security.bruteForceProtection.TooManyLoginAttemptsException;
import com.danya.workload.api.exception.ClientErrorException;
import com.danya.workload.api.exception.InvalidRequestException;
import com.danya.workload.api.exception.ResourceNotFoundException;
import com.danya.workload.api.exception.WorkloadServiceException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorDto> handleEntityNotFound(
            EntityNotFoundException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ErrorDto body = new ErrorDto(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(status)
                .body(body);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorDto> handleBadCredentials(
            AuthenticationException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ErrorDto body = new ErrorDto(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(status)
                .body(body);
    }

    @ExceptionHandler(TrainerDoesNotMatchTrainingTypeException.class)
    public ResponseEntity<ErrorDto> handleTrainerTrainingMismatch(
            TrainerDoesNotMatchTrainingTypeException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.CONFLICT;
        ErrorDto body = new ErrorDto(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(status)
                .body(body);
    }

    @ExceptionHandler(InvalidCurrentPasswordException.class)
    public ResponseEntity<ErrorDto> handleInvalidCurrentPassword(
            InvalidCurrentPasswordException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorDto body = new ErrorDto(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(status)
                .body(body);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorDto> handleInvalidTokenException(
            InvalidCurrentPasswordException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ErrorDto body = new ErrorDto(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(status)
                .body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handle(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String validationMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        HttpStatus status = HttpStatus.BAD_REQUEST;

        ErrorDto body = new ErrorDto(
                status.value(),
                status.getReasonPhrase(),
                validationMessage,
                request.getRequestURI()
        );
        return ResponseEntity.status(ex.getStatusCode())
                .body(body);
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<ErrorDto> handleBadBody(
            InvalidFormatException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = String.format(
                "Invalid value %s for type %s",
                ex.getValue(),
                ex.getTargetType().getSimpleName()
        );

        ErrorDto body = new ErrorDto(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(status)
                .body(body);
    }

    @ExceptionHandler(TooManyLoginAttemptsException.class)
    public ResponseEntity<ErrorDto> handleTooManyLoginAttemptsException(
            TooManyLoginAttemptsException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        ErrorDto body = new ErrorDto(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(status)
                .body(body);
    }

    @ExceptionHandler({Exception.class, WorkloadServiceException.class, ClientErrorException.class})
    public ResponseEntity<ErrorDto> handleInternalException() {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorDto(500, "Internal Server Error",
                        "Something went wrong. Please try again later.", null));
    }
}

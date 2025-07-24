package com.lending.backend.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    private String generateRequestId() {
        return "req_" + UUID.randomUUID().toString().replace("-", "");
    }

    @ExceptionHandler(ApiException.class)
    public ApiProblemDetail handleApiException(ApiException ex, HttpServletRequest request) {
        String requestId = ex.getRequestId() != null ? ex.getRequestId() : generateRequestId();

        ApiProblemDetail problemDetail = ProblemDetailBuilder.createProblemDetail(
                ex.getStatus(),
                ex.getCode(),
                ex.getMessage(),
                requestId);

        problemDetail.setProperty("errorType", ex.getClass().getSimpleName());
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiProblemDetail handleValidationExceptions(MethodArgumentNotValidException ex) {
        String requestId = generateRequestId();

        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null
                                ? fieldError.getDefaultMessage()
                                : "Invalid value"));

        return ProblemDetailBuilder.createValidationProblemDetail(
                ErrorConstants.VALIDATION_ERROR,
                ErrorConstants.MSG_VALIDATION_ERROR,
                requestId,
                errors);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ApiProblemDetail handleAuthenticationException(AuthenticationException ex) {
        String requestId = generateRequestId();

        ApiProblemDetail problemDetail = ProblemDetailBuilder.createProblemDetail(
                HttpStatus.UNAUTHORIZED,
                ErrorConstants.AUTHENTICATION_ERROR,
                ex.getMessage(),
                requestId);

        problemDetail.setProperty("errorType", ex.getClass().getSimpleName());
        return problemDetail;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ApiProblemDetail handleAccessDeniedException(AccessDeniedException ex) {
        String requestId = generateRequestId();

        ApiProblemDetail problemDetail = ProblemDetailBuilder.createProblemDetail(
                HttpStatus.FORBIDDEN,
                ErrorConstants.ACCESS_DENIED,
                "Access Denied: " + ex.getMessage(),
                requestId);

        problemDetail.setProperty("errorType", ex.getClass().getSimpleName());
        return problemDetail;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ApiProblemDetail handleResourceNotFoundException(ResourceNotFoundException ex) {
        String requestId = ex.getRequestId() != null ? ex.getRequestId() : generateRequestId();

        ApiProblemDetail problemDetail = ProblemDetailBuilder.createProblemDetail(
                HttpStatus.NOT_FOUND,
                ex.getCode() != null ? ex.getCode() : ErrorConstants.RESOURCE_NOT_FOUND,
                ex.getMessage(),
                requestId);

        problemDetail.setProperty("errorType", ex.getClass().getSimpleName());
        return problemDetail;
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ApiProblemDetail handleNoResourceFoundException(NoResourceFoundException ex) {
        String requestId = generateRequestId();

        ApiProblemDetail problemDetail = ProblemDetailBuilder.createProblemDetail(
                HttpStatus.NOT_FOUND,
                ErrorConstants.RESOURCE_NOT_FOUND,
                "The requested resource was not found: " + ex.getResourcePath(),
                requestId);

        problemDetail.setProperty("errorType", ex.getClass().getSimpleName());
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ApiProblemDetail handleAllExceptions(Exception ex, HttpServletRequest request) {
        String requestId = generateRequestId();

        // Log the full exception for debugging purposes
        log.error("Unhandled exception: {}", ex.getMessage(), ex);

        ApiProblemDetail problemDetail = ProblemDetailBuilder.createInternalServerError(
                ErrorConstants.SERVER_ERROR,
                "An unexpected error occurred: " + ex.getMessage(),
                requestId);

        problemDetail.setProperty("errorType", ex.getClass().getSimpleName());
        return problemDetail;
    }
}

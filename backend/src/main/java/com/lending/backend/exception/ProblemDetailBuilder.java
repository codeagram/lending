package com.lending.backend.exception;

import org.springframework.http.HttpStatus;

import java.net.URI;
import java.util.Map;

/**
 * Utility class for building RFC 7807 ProblemDetail objects.
 */
public class ProblemDetailBuilder {
    private static final String PROBLEM_BASE_URI = "https://api.lending.com/problems";

    /**
     * Creates a ProblemDetail for a specific error type and status.
     */
    public static ApiProblemDetail createProblemDetail(
            HttpStatus status,
            String errorCode,
            String detail,
            String requestId) {
        
        URI problemType = URI.create(String.format("%s/%s", PROBLEM_BASE_URI, errorCode));
        
        return ApiProblemDetail.builder()
                .type(problemType)
                .title(status.getReasonPhrase())
                .status(status.value())
                .detail(detail)
                .requestId(requestId)
                .timestamp(java.time.Instant.now())
                .build();
    }

    /**
     * Creates a ProblemDetail for validation errors.
     */
    public static ApiProblemDetail createValidationProblemDetail(
            String errorCode,
            String detail,
            String requestId,
            Map<String, String> validationErrors) {
        
        ApiProblemDetail problemDetail = createProblemDetail(
                HttpStatus.BAD_REQUEST, 
                errorCode, 
                detail, 
                requestId);
        
        if (validationErrors != null && !validationErrors.isEmpty()) {
            problemDetail.setProperty("errors", validationErrors);
        }
        
        return problemDetail;
    }

    /**
     * Creates a ProblemDetail for internal server errors.
     */
    public static ApiProblemDetail createInternalServerError(
            String errorCode,
            String detail,
            String requestId) {
        
        return createProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                errorCode,
                detail,
                requestId);
    }
}

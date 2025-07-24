package com.lending.backend.common.exception;

/**
 * Common error codes and messages used across the application.
 */
public final class ErrorConstants {
    // Common error codes
    public static final String INVALID_REQUEST = "invalid_request";
    public static final String AUTHENTICATION_ERROR = "authentication_error";
    public static final String AUTHORIZATION_ERROR = "authorization_error";
    public static final String ACCESS_DENIED = "access_denied";
    public static final String NOT_FOUND = "not_found";
    public static final String RESOURCE_NOT_FOUND = "resource_not_found";
    public static final String RATE_LIMIT_EXCEEDED = "rate_limit_exceeded";
    public static final String SERVER_ERROR = "server_error";
    public static final String SERVICE_UNAVAILABLE = "service_unavailable";
    public static final String VALIDATION_ERROR = "validation_error";

    // Common error messages
    public static final String MSG_INVALID_REQUEST = "The request was invalid";
    public static final String MSG_UNAUTHORIZED = "Authentication is required";
    public static final String MSG_FORBIDDEN = "You don't have permission to access this resource";
    public static final String MSG_NOT_FOUND = "The requested resource was not found";
    public static final String MSG_RATE_LIMIT = "Too many requests, please try again later";
    public static final String MSG_SERVER_ERROR = "An unexpected error occurred";
    public static final String MSG_SERVICE_UNAVAILABLE = "The service is currently unavailable";
    public static final String MSG_VALIDATION_ERROR = "Validation failed";

    // Documentation URLs
    public static final String DOCS_BASE_URL = "https://docs.your-api.com/errors";

    private ErrorConstants() {
        // Private constructor to prevent instantiation
    }
}

package com.lending.backend.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested resource is not found.
 */
public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(
                HttpStatus.NOT_FOUND,
                String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue),
                "resource_not_found");
    }

    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message, "resource_not_found");
    }
}

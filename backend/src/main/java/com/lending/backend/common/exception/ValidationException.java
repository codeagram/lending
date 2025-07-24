package com.lending.backend.common.exception;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Exception thrown when validation fails in the application.
 * Contains a list of validation errors.
 */
@Getter
public class ValidationException extends RuntimeException {

    private final List<String> errors = new ArrayList<>();

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, List<String> errors) {
        super(message);
        this.errors.addAll(errors);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public void addError(String error) {
        this.errors.add(error);
    }

    public void addErrors(List<String> errors) {
        this.errors.addAll(errors);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}

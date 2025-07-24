package com.lending.backend.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final String code;
    private final String docUrl;
    private final String requestId;

    public ApiException(HttpStatus status, String message) {
        this(status, message, null, null, null);
    }

    public ApiException(HttpStatus status, String message, String code) {
        this(status, message, code, null, null);
    }

    public ApiException(HttpStatus status, String message, String code, String docUrl, String requestId) {
        super(message);
        this.status = status;
        this.code = code;
        this.docUrl = docUrl;
        this.requestId = requestId;
    }
}

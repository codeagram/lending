package com.lending.backend.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * RFC 7807 Problem Details implementation.
 * This is a custom implementation to avoid conflicts with Spring's
 * ProblemDetail.
 * See: https://tools.ietf.org/html/rfc7807
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "type", "title", "status", "detail", "instance" })
public class ApiProblemDetail {
    /**
     * A URI reference [RFC3986] that identifies the problem type.
     */
    private URI type;

    /**
     * A short, human-readable summary of the problem type.
     */
    private String title;

    /**
     * The HTTP status code ([RFC7231], Section 6) generated by the origin server
     * for this occurrence of the problem.
     */
    private int status;

    /**
     * A human-readable explanation specific to this occurrence of the problem.
     */
    private String detail;

    /**
     * A URI reference that identifies the specific occurrence of the problem.
     */
    private URI instance;

    /**
     * A unique identifier for this specific occurrence of the problem.
     */
    @JsonProperty("request_id")
    private String requestId;

    /**
     * Timestamp when the error occurred.
     */
    private Instant timestamp;

    /**
     * Additional properties that extend the problem details.
     */
    @Builder.Default
    private Map<String, Object> properties = new HashMap<>();

    public static ApiProblemDetail forStatus(HttpStatus status) {
        return ApiProblemDetail.builder()
                .status(status.value())
                .title(status.getReasonPhrase())
                .timestamp(Instant.now())
                .build();
    }

    public static ApiProblemDetail forStatusAndDetail(HttpStatus status, String detail) {
        return ApiProblemDetail.builder()
                .status(status.value())
                .title(status.getReasonPhrase())
                .detail(detail)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Set a custom property in the problem details.
     */
    public void setProperty(String name, Object value) {
        if (this.properties == null) {
            this.properties = new java.util.HashMap<>();
        }
        this.properties.put(name, value);
    }
}

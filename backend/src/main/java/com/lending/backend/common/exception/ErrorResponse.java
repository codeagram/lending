package com.lending.backend.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    @JsonProperty("error")
    private ErrorDetails error;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDetails {
        private String errorType;
        private String message;
        private String code;
        private String docUrl;
        private String requestId;
        private Map<String, String> params;
        private Instant timestamp;
    }

    public static ErrorResponse fromException(Exception ex, String requestId) {
        return ErrorResponse.builder()
                .error(ErrorDetails.builder()
                        .errorType(ex.getClass().getSimpleName())
                        .message(ex.getMessage())
                        .timestamp(Instant.now())
                        .requestId(requestId)
                        .build())
                .build();
    }
}

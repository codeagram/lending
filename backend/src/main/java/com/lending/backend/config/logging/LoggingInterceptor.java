package com.lending.backend.config.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.lending.backend.common.utils.LogUtils;

/**
 * Interceptor for setting up and clearing logging context for each request.
 * This interceptor logs the start and completion of each HTTP request,
 * along with relevant details like HTTP method, URI, and response status.
 */
@Component
public class LoggingInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);
    private static final String UNKNOWN = "unknown";

    @Override
    public boolean preHandle(
            @NonNull final HttpServletRequest request,
            @NonNull final HttpServletResponse response,
            @NonNull final Object handler) {
        try {
            LogUtils.setupRequestContext();
            log.info("Request started: {} {}",
                    request.getMethod(),
                    getRequestUri(request));
        } catch (Exception e) {
            log.warn("Failed to setup request logging context", e);
        }
        return true;
    }

    @Override
    public void afterCompletion(
            @NonNull final HttpServletRequest request,
            @NonNull final HttpServletResponse response,
            @NonNull final Object handler,
            @Nullable final Exception ex) {
        try {
            if (ex != null) {
                log.error("Request failed: {} {}",
                        request.getMethod(),
                        getRequestUri(request),
                        ex);
            } else {
                log.info("Request completed: {} {} - Status: {}",
                        request.getMethod(),
                        getRequestUri(request),
                        response.getStatus());
            }
        } catch (Exception e) {
            log.warn("Error during request completion logging", e);
        } finally {
            try {
                LogUtils.clearContext();
            } catch (Exception e) {
                log.warn("Failed to clear logging context", e);
            }
        }
    }

    private String getRequestUri(@NonNull final HttpServletRequest request) {
        try {
            return request.getRequestURI();
        } catch (Exception e) {
            log.warn("Failed to get request URI", e);
            return UNKNOWN;
        }
    }
}

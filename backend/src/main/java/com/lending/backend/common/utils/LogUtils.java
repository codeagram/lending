package com.lending.backend.common.utils;

import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * Utility class for consistent logging across the application.
 * Provides methods for structured logging with request context.
 */
public class LogUtils {

    private static final String REQUEST_ID = "requestId";
    private static final String USER_AGENT = "userAgent";
    private static final String CLIENT_IP = "clientIp";
    private static final String REQUEST_METHOD = "method";
    private static final String REQUEST_URI = "uri";

    private LogUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Set up MDC (Mapped Diagnostic Context) for the current request.
     * Should be called at the beginning of request processing.
     */
    public static void setupRequestContext() {
        try {
            // Generate a unique request ID
            MDC.put(REQUEST_ID, UUID.randomUUID().toString());

            // Add request details if available
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                MDC.put(REQUEST_METHOD, request.getMethod());
                MDC.put(REQUEST_URI, request.getRequestURI());
                MDC.put(CLIENT_IP, getClientIpAddress(request));
                MDC.put(USER_AGENT,
                        request.getHeader("User-Agent") != null ? request.getHeader("User-Agent") : "unknown");
            }
        } catch (Exception e) {
            // Don't fail the request if logging setup fails
        }
    }

    /**
     * Clear MDC context.
     * Should be called at the end of request processing.
     */
    public static void clearContext() {
        try {
            MDC.clear();
        } catch (Exception e) {
            // Ignore
        }
    }

    /**
     * Log a debug message with request context.
     */
    public static void debug(Logger logger, String message, Object... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(message, args);
        }
    }

    /**
     * Log an info message with request context.
     */
    public static void info(Logger logger, String message, Object... args) {
        if (logger.isInfoEnabled()) {
            logger.info(message, args);
        }
    }

    /**
     * Log a warning message with request context.
     */
    public static void warn(Logger logger, String message, Object... args) {
        logger.warn(message, args);
    }

    /**
     * Log an error message with request context and exception.
     */
    public static void error(Logger logger, String message, Throwable throwable, Object... args) {
        logger.error(message + " {}", args, throwable);
    }

    /**
     * Get the current HTTP request.
     */
    private static HttpServletRequest getCurrentRequest() {
        try {
            return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        } catch (IllegalStateException e) {
            return null;
        }
    }

    /**
     * Get client IP address from request.
     */
    private static String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}

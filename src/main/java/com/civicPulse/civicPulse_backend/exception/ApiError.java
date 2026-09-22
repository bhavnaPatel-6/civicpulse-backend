package com.civicPulse.civicPulse_backend.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Har error response ka ek hi shape — frontend me ek hi parsing logic kaafi hai.
 * Null fields JSON me nahi aate.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,          // "NOT_FOUND", "VALIDATION_FAILED", ...
        String message,        // user ko dikhane layak message
        String path,           // /api/complaints/5
        String traceId,        // logs se match karne ke liye
        Map<String, String> fieldErrors   // sirf validation errors me
) {
    public static ApiError of(int status, String error, String message,
                              String path, String traceId) {
        return new ApiError(LocalDateTime.now(), status, error, message,
                path, traceId, null);
    }

    public static ApiError validation(int status, String message, String path,
                                      String traceId, Map<String, String> fieldErrors) {
        return new ApiError(LocalDateTime.now(), status, "VALIDATION_FAILED",
                message, path, traceId, fieldErrors);
    }
}
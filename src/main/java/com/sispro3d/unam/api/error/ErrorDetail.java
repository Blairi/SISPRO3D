package com.sispro3d.unam.api.error;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standardized error payload returned by the REST API.
 *
 * @param timestamp when the error occurred
 * @param status    HTTP status code
 * @param error     HTTP status reason phrase
 * @param message   human-readable error message
 * @param path      request path that triggered the error
 * @param details   optional per-field validation messages
 */
public record ErrorDetail(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details) {

    public static ErrorDetail of(int status, String error, String message, String path, List<String> details) {
        return new ErrorDetail(LocalDateTime.now(), status, error, message, path, details);
    }

    public static ErrorDetail of(int status, String error, String message, String path) {
        return new ErrorDetail(LocalDateTime.now(), status, error, message, path, List.of());
    }
}

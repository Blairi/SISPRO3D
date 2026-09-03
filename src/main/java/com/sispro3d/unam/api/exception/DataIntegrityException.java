package com.sispro3d.unam.api.exception;

/**
 * Thrown when an operation would violate a data integrity constraint via the
 * REST API (e.g. deleting a category that still has associated services, or
 * creating a duplicate review). Mapped to HTTP 409 Conflict.
 */
public class DataIntegrityException extends RuntimeException {

    public DataIntegrityException(String message) {
        super(message);
    }
}

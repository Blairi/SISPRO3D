package com.sispro3d.unam.api.exception;

/**
 * Thrown when a request is semantically invalid (e.g. an illegal state
 * transition) via the REST API. Mapped to HTTP 400 Bad Request.
 */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}

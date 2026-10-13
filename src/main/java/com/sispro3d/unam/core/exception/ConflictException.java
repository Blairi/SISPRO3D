package com.sispro3d.unam.core.exception;

/**
 * La acción es válida en principio pero choca con el estado actual de los
 * datos (por ejemplo, una cotización que ya tiene orden de trabajo).
 * Se traduce a HTTP 409 en la capa de errores de la API.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
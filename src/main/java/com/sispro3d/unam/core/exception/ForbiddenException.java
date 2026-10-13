package com.sispro3d.unam.core.exception;

/**
 * El actor autenticado no tiene permiso para la acción solicitada.
 * Se traduce a HTTP 403 en la capa de errores de la API.
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
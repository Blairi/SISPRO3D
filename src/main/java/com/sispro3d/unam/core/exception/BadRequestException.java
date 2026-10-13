package com.sispro3d.unam.core.exception;

/**
 * La petición está bien formada pero viola una regla de negocio: una transición
 * de estado que no corresponde, un rango de valores inválido o una vigencia
 * vencida. Se traduce a HTTP 400 en la capa de errores de la API.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
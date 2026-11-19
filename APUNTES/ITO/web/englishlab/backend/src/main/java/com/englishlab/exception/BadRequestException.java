package com.englishlab.exception;

/**
 * Petición con datos inválidos (p. ej. contenido de actividad mal formado) -> HTTP 400.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
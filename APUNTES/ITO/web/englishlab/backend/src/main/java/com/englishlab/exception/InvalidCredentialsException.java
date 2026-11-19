package com.englishlab.exception;

/**
 * Credenciales invalidas en el login -> HTTP 401 (caso de dominio, distinto del 401 de la cadena de seguridad).
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}

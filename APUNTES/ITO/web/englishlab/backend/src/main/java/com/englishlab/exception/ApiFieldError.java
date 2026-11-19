package com.englishlab.exception;

/**
 * Error de un campo concreto (para el array `fieldErrors` de la respuesta de validacion).
 */
public record ApiFieldError(String field, String message) {}

package com.englishlab.domain;

/**
 * Modo de un intento: practica libre ({@link #NORMAL}) o reto cronometrado ({@link #CHALLENGE}).
 * El modo decide qué puntuación se guarda (ver {@link MasteryConfig}) y si el answer puede
 * llegar vacío (pregunta sin responder por timeout).
 */
public enum AttemptMode {

    NORMAL,
    CHALLENGE;

    /** Valor por defecto cuando el cliente no envía `mode`. */
    public static final String DEFAULT_MODE = "NORMAL";

    /** Patrón de Bean Validation para el campo `mode` del DTO. */
    public static final String PATTERN = "NORMAL|CHALLENGE";

    /** true si el valor recibido corresponde al modo RETO (comparación estricta). */
    public static boolean isChallenge(String mode) {
        return CHALLENGE.name().equals(mode);
    }
}

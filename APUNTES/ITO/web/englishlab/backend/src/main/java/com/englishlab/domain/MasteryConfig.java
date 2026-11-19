package com.englishlab.domain;

/**
 * Constantes del modelo de dominio adaptativo (sin números mágicos repartidos por el código).
 */
public final class MasteryConfig {

    /** Peso del rendimiento reciente en el cálculo de dominio. */
    public static final double W_RECENT = 0.65;

    /** Peso del dominio histórico en el cálculo de dominio. */
    public static final double W_HIST = 0.35;

    /** Dominio por defecto cuando el alumno aún no tiene fila de estado. */
    public static final double DEFAULT_MASTERY = 0.30;

    /** Umbral a partir del cual un objetivo se considera dominado. */
    public static final double MASTERED_THRESHOLD = 0.80;

    /** Número de intentos recientes usados para el promedio de rendimiento reciente. */
    public static final int RECENT_WINDOW = 5;

    // ------------------------------------------------------------------ modo RETO (challenge)
    // ASSUMPTION: el modo RETO pesa en el dominio con estos valores. Se califican igual con
    // GradingService, pero la puntuación que entra en `recent` se transforma según el modo.
    // En NORMAL se mantiene lo de siempre: correcta 1.0, incorrecta 0.0 (lo emite GradingService).

    /** Puntuación de un intento correcto en modo RETO. */
    public static final double CHALLENGE_CORRECT_SCORE = 1.0;

    /** Puntuación de una pregunta sin responder en modo RETO (timeout): ni premio ni castigo. */
    public static final double CHALLENGE_UNANSWERED_SCORE = 0.0;

    /** Puntuación de un intento incorrecto en modo RETO (penaliza adivinar). */
    public static final double CHALLENGE_WRONG_SCORE = -0.5;

    private MasteryConfig() {
    }
}
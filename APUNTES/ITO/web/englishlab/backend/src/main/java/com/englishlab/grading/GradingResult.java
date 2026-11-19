package com.englishlab.grading;

/**
 * Resultado de calificar una respuesta. En este MVP el score es binario: 1.0 o 0.0.
 */
public record GradingResult(boolean correct, double score) {}

package com.englishlab.dto.attempt;

/**
 * Resultado de un intento. La solución y la explicación solo se incluyen DESPUÉS de calificar.
 */
public record AttemptResponse(
        boolean correct,
        double score,
        String solution,
        String explanation,
        double mastery
) {}
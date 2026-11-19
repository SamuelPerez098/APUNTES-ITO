package com.englishlab.dto.recommendation;

/**
 * Recomendación de la siguiente actividad. En el caso normal {@code objectiveId},
 * {@code activityId} y {@code reason} vienen informados; si todo está dominado, quedan en
 * {@code null} y {@code message} explica la situación.
 */
public record RecommendationResponse(
        Long objectiveId,
        Long activityId,
        String reason,
        String message
) {}
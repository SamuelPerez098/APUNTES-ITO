package com.englishlab.domain;

import java.time.Instant;

/**
 * Actividad candidata para recomendación, con la fecha de su último intento (null si nunca
 * se intentó).
 */
public record ActivityCandidate(Long activityId, Instant lastAttemptAt) {}
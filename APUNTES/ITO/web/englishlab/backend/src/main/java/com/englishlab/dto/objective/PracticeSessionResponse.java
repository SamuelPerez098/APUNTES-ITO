package com.englishlab.dto.objective;

import java.util.List;

/**
 * Sesión de práctica de un objetivo: solo ids de actividad, en el orden en que deben
 * presentarse. El detalle sin clave de respuesta se pide a {@code GET /api/v1/me/activities/{id}}.
 */
public record PracticeSessionResponse(List<Long> activityIds) {}

package com.englishlab.dto.activity;

/**
 * Resumen de una actividad para la práctica libre del estudiante.
 * NUNCA expone el contenido ni la clave de respuesta: el detalle se pide a
 * {@code GET /api/v1/me/activities/{id}}.
 */
public record ActivityStudentSummaryDto(
        Long id,
        String title,
        String type,
        String level,
        String skill
) {}

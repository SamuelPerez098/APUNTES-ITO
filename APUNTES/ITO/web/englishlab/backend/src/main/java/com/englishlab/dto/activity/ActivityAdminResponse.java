package com.englishlab.dto.activity;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Vista ADMIN/TEACHER de una actividad: metadatos + contenido completo
 * (incluye la clave de respuesta en {@code content}).
 */
public record ActivityAdminResponse(
        Long id,
        Long objectiveId,
        String type,
        String level,
        String skill,
        String title,
        Long categoryId,
        JsonNode content
) {}
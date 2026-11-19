package com.englishlab.dto.activity;

import java.util.List;

/**
 * Vista de estudiante de UNA actividad completa (metadatos + contenido).
 * JAMÁS expone answer, accepted ni explanation.
 */
public record ActivityStudentDetailDto(
        Long id,
        String type,
        String level,
        String skill,
        String title,
        String prompt,
        List<ActivityOption> options
) {}
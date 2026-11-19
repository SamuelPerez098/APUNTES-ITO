package com.englishlab.dto.activity;

import java.util.List;

/**
 * DTO completo para ADMIN/TEACHER (incluye la clave de respuesta).
 */
public record ActivityAdminDto(
        String prompt,
        List<ActivityOption> options,
        String answer,
        List<String> accepted,
        String explanation
) {}

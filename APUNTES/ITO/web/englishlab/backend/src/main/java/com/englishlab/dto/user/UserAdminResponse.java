package com.englishlab.dto.user;

import java.time.Instant;

/**
 * Vista ADMIN de un usuario (nunca expone el hash de la contraseña).
 */
public record UserAdminResponse(
        Long id,
        String name,
        String email,
        String role,
        Long teacherId,
        String teacherName,
        String phone,
        Instant createdAt
) {}
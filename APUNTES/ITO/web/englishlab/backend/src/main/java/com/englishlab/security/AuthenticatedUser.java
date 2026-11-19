package com.englishlab.security;

import com.englishlab.model.entity.RoleName;
import java.time.Instant;

/**
 * Datos del usuario autenticado extraidos del JWT (se usan como principal del SecurityContext).
 */
public record AuthenticatedUser(Long id, RoleName role, String jti, Instant expiresAt) {}

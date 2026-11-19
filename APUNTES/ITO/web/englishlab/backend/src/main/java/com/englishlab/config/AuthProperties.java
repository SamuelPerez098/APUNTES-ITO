package com.englishlab.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Propiedades de autenticación. {@code log-reset-token} controla si el token de
 * recuperación de contraseña se escribe en el log (SOLO para desarrollo local).
 */
@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(
        @DefaultValue("false") boolean logResetToken
) {}
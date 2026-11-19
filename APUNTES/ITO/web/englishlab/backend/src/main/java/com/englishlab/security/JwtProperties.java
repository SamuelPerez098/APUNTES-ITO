package com.englishlab.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Propiedades del JWT. El secreto viene SIEMPRE de la variable de entorno JWT_SECRET
 * (sin valor por defecto en el codigo ni en application.properties.example).
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        @DefaultValue("3600000") long expirationMs
) {}

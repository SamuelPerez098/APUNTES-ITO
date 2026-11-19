package com.englishlab.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuración del envío de correo de la aplicación. {@code enabled} apaga el envío real
 * (por defecto), de modo que el arranque no exige un servidor SMTP local.
 */
@ConfigurationProperties(prefix = "app.mail")
public record MailProperties(
        @DefaultValue("false") boolean enabled,
        @DefaultValue("no-reply@englishlab.local") String from
) {}
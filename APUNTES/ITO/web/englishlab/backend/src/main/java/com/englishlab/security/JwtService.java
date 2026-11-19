package com.englishlab.security;

import com.englishlab.model.entity.RoleName;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

/**
 * Genera y valida JWT (HMAC-SHA256) con claims: sub (id), role, jti (UUID) y exp.
 */
@Service
public class JwtService {

    private static final int MIN_SECRET_BYTES = 32; // 256 bits para HMAC-SHA256

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(JwtProperties properties) {
        String secret = properties.secret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "app.jwt.secret no configurado: define la variable de entorno JWT_SECRET (>= 256 bits).");
        }
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "JWT_SECRET debe tener al menos 256 bits (" + MIN_SECRET_BYTES + " bytes); tiene " + bytes.length + ".");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expirationMs = properties.expirationMs();
    }

    public String generateToken(Long userId, RoleName role) {
        Instant now = Instant.now();
        String jti = UUID.randomUUID().toString();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role.name())
                .id(jti)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Valida firma y expiracion. Lanza {@link io.jsonwebtoken.JwtException} si el token
     * es invalido o expiro, o {@link IllegalArgumentException} si los claims no son validos.
     */
    public AuthenticatedUser parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        Long userId = Long.valueOf(claims.getSubject());
        RoleName role = RoleName.valueOf(claims.get("role", String.class));
        String jti = claims.getId();
        Instant expiresAt = claims.getExpiration().toInstant();
        return new AuthenticatedUser(userId, role, jti, expiresAt);
    }
}

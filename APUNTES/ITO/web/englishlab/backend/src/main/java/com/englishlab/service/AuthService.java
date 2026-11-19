package com.englishlab.service;

import com.englishlab.config.AuthProperties;
import com.englishlab.dto.auth.AuthResponse;
import com.englishlab.dto.auth.ForgotPasswordRequest;
import com.englishlab.dto.auth.LoginRequest;
import com.englishlab.dto.auth.RegisterRequest;
import com.englishlab.dto.auth.ResetPasswordRequest;
import com.englishlab.dto.auth.UserResponse;
import com.englishlab.exception.BusinessRuleException;
import com.englishlab.exception.InvalidCredentialsException;
import com.englishlab.model.entity.PasswordResetToken;
import com.englishlab.model.entity.RevokedToken;
import com.englishlab.model.entity.Role;
import com.englishlab.model.entity.RoleName;
import com.englishlab.model.entity.User;
import com.englishlab.repository.PasswordResetTokenRepository;
import com.englishlab.repository.RevokedTokenRepository;
import com.englishlab.repository.RoleRepository;
import com.englishlab.repository.UserRepository;
import com.englishlab.security.AuthenticatedUser;
import com.englishlab.security.JwtService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    /** Validez del token de recuperacion: 30 minutos. */
    private static final Duration RESET_TOKEN_TTL = Duration.ofMinutes(30);
    private static final int RESET_TOKEN_BYTES = 32;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RevokedTokenRepository revokedTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthProperties authProperties;
    private final NotificationService notificationService;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            RevokedTokenRepository revokedTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthProperties authProperties,
            NotificationService notificationService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.revokedTokenRepository = revokedTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authProperties = authProperties;
        this.notificationService = notificationService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessRuleException("El email ya está registrado");
        }
        Role studentRole = roleRepository.findByName(RoleName.STUDENT)
                .orElseThrow(() -> new IllegalStateException(
                        "Rol STUDENT no encontrado en la base de datos; ¿se aplicaron las migraciones/seed?"));
        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(studentRole);
        user.setCreatedAt(Instant.now());
        user = userRepository.save(user);

        final Long savedUserId = user.getId();
        final String savedEmail = user.getEmail();
        notifyAfterCommit(() -> notificationService.sendWelcome(savedUserId, savedEmail));

        String token = jwtService.generateToken(user.getId(), RoleName.STUDENT);
        return new AuthResponse(token, toResponse(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales inválidas"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Credenciales inválidas");
        }
        String token = jwtService.generateToken(user.getId(), user.getRole().getName());
        return new AuthResponse(token, toResponse(user));
    }

    @Transactional
    public void logout(AuthenticatedUser principal) {
        RevokedToken revoked = new RevokedToken();
        revoked.setJti(principal.jti());
        revoked.setUser(userRepository.getReferenceById(principal.id()));
        revoked.setExpiresAt(principal.expiresAt());
        revoked.setRevokedAt(Instant.now());
        revokedTokenRepository.save(revoked);
    }

    /**
     * Responde igual exista o no el correo (no filtra usuarios). En Inc 1 el token
     * se loguea en lugar de enviarse por correo (el correo llega en Inc 3).
     */
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = normalizeEmail(request.email());
        userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
            String rawToken = generateResetToken();
            String tokenHash = sha256Hex(rawToken);

            PasswordResetToken prt = new PasswordResetToken();
            prt.setUser(user);
            prt.setTokenHash(tokenHash);
            prt.setExpiresAt(Instant.now().plus(RESET_TOKEN_TTL));
            prt.setCreatedAt(Instant.now());
            passwordResetTokenRepository.save(prt);

            // El token SOLO se loguea si app.auth.log-reset-token=true (default false),
            // para poder probar en local sin exponerlo nunca en despliegue.
            if (authProperties.logResetToken()) {
                log.info("Token de recuperación de contraseña para {}: {}", email, rawToken);
            }

            final Long userId = user.getId();
            final String userEmail = user.getEmail();
            final String token = rawToken;
            notifyAfterCommit(() -> notificationService.sendPasswordReset(userId, userEmail, token));
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String tokenHash = sha256Hex(request.token());
        PasswordResetToken prt = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessRuleException("Token inválido o expirado"));
        if (prt.getUsedAt() != null) {
            throw new BusinessRuleException("El token ya fue utilizado");
        }
        if (prt.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessRuleException("Token expirado");
        }
        User user = prt.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        prt.setUsedAt(Instant.now());
        userRepository.save(user);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole().getName().name());
    }

    /** Ejecuta la acción tras el commit (si hay transacción activa) para no romper el flujo ni dejar FKs inconsistentes. */
    private void notifyAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String generateResetToken() {
        byte[] bytes = new byte[RESET_TOKEN_BYTES];
        new SecureRandom().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}

package com.englishlab.service;

import com.englishlab.repository.RevokedTokenRepository;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tarea programada que purga los tokens revocados ya vencidos de `revoked_tokens`
 * (filas con {@code expires_at < now()}) para que la tabla no crezca indefinidamente.
 */
@Component
public class RevokedTokenCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(RevokedTokenCleanupTask.class);

    private final RevokedTokenRepository revokedTokenRepository;

    public RevokedTokenCleanupTask(RevokedTokenRepository revokedTokenRepository) {
        this.revokedTokenRepository = revokedTokenRepository;
    }

    /** Cada dia a las 03:00. */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeExpiredRevokedTokens() {
        long deleted = revokedTokenRepository.deleteByExpiresAtBefore(Instant.now());
        if (deleted > 0) {
            log.info("Purga de revoked_tokens vencidos: {} fila(s) eliminadas", deleted);
        }
    }
}

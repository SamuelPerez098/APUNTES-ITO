package com.englishlab.repository;

import com.englishlab.model.entity.RevokedToken;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {

    boolean existsByJti(String jti);

    long deleteByExpiresAtBefore(Instant expiresAt);
}

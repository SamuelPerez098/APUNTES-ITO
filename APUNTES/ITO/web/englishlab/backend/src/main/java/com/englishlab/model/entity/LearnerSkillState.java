package com.englishlab.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Dominio del alumno por objetivo (V3: PK user_id+objective_id, mastery, attempts_count,
 * correct_count, last_attempt_at; V18: previous_mastery).
 */
@Entity
@Table(name = "learner_skill_state")
@IdClass(LearnerSkillStateId.class)
@Getter
@Setter
@NoArgsConstructor
public class LearnerSkillState {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Column(name = "objective_id", nullable = false)
    private Long objectiveId;

    @Column(name = "mastery", nullable = false, precision = 5, scale = 4)
    private BigDecimal mastery;

    /** Dominio que habia antes del ultimo intento (V18); null si aun no hay sesiones. */
    @Column(name = "previous_mastery", precision = 5, scale = 4)
    private BigDecimal previousMastery;

    @Column(name = "attempts_count", nullable = false)
    private int attemptsCount;

    @Column(name = "correct_count", nullable = false)
    private int correctCount;

    @Column(name = "last_attempt_at")
    private Instant lastAttemptAt;
}
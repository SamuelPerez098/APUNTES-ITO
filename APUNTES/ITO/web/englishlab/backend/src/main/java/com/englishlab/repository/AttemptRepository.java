package com.englishlab.repository;

import com.englishlab.model.entity.Attempt;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {

    /** Scores de los últimos intentos (más recientes primero) del alumno en un objetivo. */
    @Query("select a.score from Attempt a "
            + "where a.user.id = :userId and a.activity.objective.id = :objectiveId "
            + "order by a.attemptedAt desc, a.id desc")
    List<BigDecimal> findRecentScores(
            @Param("userId") Long userId,
            @Param("objectiveId") Long objectiveId,
            Pageable pageable);

    @Query("select a from Attempt a "
            + "where a.user.id = :userId and a.activity.objective.id = :objectiveId")
    List<Attempt> findByUserIdAndObjectiveId(
            @Param("userId") Long userId,
            @Param("objectiveId") Long objectiveId);
}
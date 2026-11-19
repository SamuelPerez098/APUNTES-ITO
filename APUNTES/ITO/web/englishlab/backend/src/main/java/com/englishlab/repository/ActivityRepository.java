package com.englishlab.repository;

import com.englishlab.model.entity.Activity;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActivityRepository extends JpaRepository<Activity, Long>, JpaSpecificationExecutor<Activity> {

    List<Activity> findByObjectiveIdOrderByIdAsc(Long objectiveId);

    Page<Activity> findByObjectiveId(Long objectiveId, Pageable pageable);

    /**
     * Sesión de práctica sin repetición: nunca intentadas primero (en orden aleatorio), luego
     * las de intento más antiguo. Al agotarse el banco el reciclado vuelve a salir aleatorio.
     * El último intento se agrega por actividad para el alumno indicado (un solo JOIN, sin N+1)
     * y todos los filtros/valores llegan como parámetros: nada se interpola en el SQL.
     */
    @Query(value = """
            select a.id
            from activities a
            left join attempts att on att.activity_id = a.id and att.user_id = :userId
            where a.objective_id = :objectiveId
            group by a.id
            order by max(att.attempted_at) asc nulls first, random()
            limit :size
            """, nativeQuery = true)
    List<Long> findPracticeSessionActivityIds(
            @Param("objectiveId") Long objectiveId,
            @Param("userId") Long userId,
            @Param("size") int size);
}
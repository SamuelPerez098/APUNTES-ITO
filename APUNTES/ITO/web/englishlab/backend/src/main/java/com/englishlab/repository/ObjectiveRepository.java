package com.englishlab.repository;

import com.englishlab.model.entity.Objective;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ObjectiveRepository extends JpaRepository<Objective, Long> {

    List<Objective> findAllByOrderByIdAsc();

    /**
     * Carga todos los objetivos con sus prerrequisitos (y el objetivo prerrequisito) en
     * una sola consulta con JOIN FETCH, para calcular estados sin N+1.
     */
    @Query("select distinct o from Objective o "
            + "left join fetch o.prerequisiteLinks pl "
            + "left join fetch pl.prerequisite "
            + "order by o.id")
    List<Objective> findAllWithPrerequisites();
}
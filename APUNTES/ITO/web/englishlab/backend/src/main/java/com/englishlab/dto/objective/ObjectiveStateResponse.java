package com.englishlab.dto.objective;

import com.englishlab.domain.ObjectiveState;

/**
 * Estado de un objetivo para el alumno autenticado.
 *
 * @param previousMastery dominio antes del ultimo intento; null = sin sesiones previas
 */
public record ObjectiveStateResponse(
        Long objectiveId,
        String code,
        String name,
        String cefrLevel,
        String skill,
        double mastery,
        Double previousMastery,
        ObjectiveState state
) {}

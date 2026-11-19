package com.englishlab.dto.assignment;

import com.englishlab.dto.activity.ActivityStudentDto;

/**
 * Asignación vista por el alumno: la actividad SIN la clave de respuesta (solo prompt +
 * options), más el estado y el id de la actividad.
 */
public record StudentAssignmentResponse(
        Long assignmentId,
        Long activityId,
        String status,
        ActivityStudentDto activity
) {}
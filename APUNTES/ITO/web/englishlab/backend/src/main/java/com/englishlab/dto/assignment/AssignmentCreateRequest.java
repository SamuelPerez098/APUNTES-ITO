package com.englishlab.dto.assignment;

import jakarta.validation.constraints.NotNull;

public record AssignmentCreateRequest(
        @NotNull(message = "El alumno es obligatorio")
        Long studentId,

        @NotNull(message = "La actividad es obligatoria")
        Long activityId
) {}
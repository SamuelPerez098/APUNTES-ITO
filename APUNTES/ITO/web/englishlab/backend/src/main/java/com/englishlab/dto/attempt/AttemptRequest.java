package com.englishlab.dto.attempt;

import com.englishlab.domain.AttemptMode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Petición de un intento. {@code mode} es opcional (por defecto NORMAL). En modo RETO el
 * {@code answer} puede venir vacío/null: significa que la pregunta quedó sin responder
 * (timeout) y no se llama al calificador.
 */
@RequiredAnswerInNormalMode
public record AttemptRequest(
        @NotNull(message = "La actividad es obligatoria")
        Long activityId,

        String answer,

        Long assignmentId,

        @Pattern(regexp = AttemptMode.PATTERN, message = "El modo solo puede ser NORMAL o CHALLENGE")
        String mode
) {

    public AttemptRequest {
        mode = (mode == null || mode.isBlank()) ? AttemptMode.DEFAULT_MODE : mode;
    }

    /** Constructor de compatibilidad: sin modo explícito equivale a NORMAL. */
    public AttemptRequest(Long activityId, String answer, Long assignmentId) {
        this(activityId, answer, assignmentId, null);
    }
}

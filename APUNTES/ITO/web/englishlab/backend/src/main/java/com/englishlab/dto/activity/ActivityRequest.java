package com.englishlab.dto.activity;

import com.englishlab.grading.ActivityType;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Alta/edición de actividad. {@code content} se valida en el servicio según el tipo.
 */
public record ActivityRequest(
        @NotNull(message = "El objetivo es obligatorio")
        Long objectiveId,

        @NotNull(message = "El tipo es obligatorio")
        ActivityType type,

        @NotBlank(message = "El nivel es obligatorio")
        @Pattern(regexp = "A1|A2|B1|B2", message = "El nivel debe ser A1, A2, B1 o B2")
        String level,

        @NotBlank(message = "La habilidad es obligatoria")
        @Size(max = 20, message = "La habilidad no puede superar los 20 caracteres")
        String skill,

        @NotBlank(message = "El título es obligatorio")
        @Size(max = 200, message = "El título no puede superar los 200 caracteres")
        String title,

        @NotNull(message = "El contenido es obligatorio")
        JsonNode content,

        Long categoryId
) {}
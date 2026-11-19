package com.englishlab.service;

import com.englishlab.dto.activity.ActivityOption;
import com.englishlab.dto.activity.ActivityStudentDetailDto;
import com.englishlab.dto.activity.ActivityStudentDto;
import com.englishlab.grading.ActivityType;
import com.englishlab.model.entity.Activity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Lectura del JSON {@code content} de una actividad para construir vistas y extraer la
 * solución/explicación SOLO cuando corresponde (después de calificar).
 */
@Service
public class ActivityContentService {

    private final ObjectMapper objectMapper;

    public ActivityContentService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public JsonNode parse(String content) {
        try {
            return objectMapper.readTree(content);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Contenido JSON inválido almacenado en la base de datos", e);
        }
    }

    /** Vista de estudiante: prompt + options (nunca answer/accepted/explanation). */
    public ActivityStudentDto toStudentDto(String type, String content) {
        JsonNode root = parse(content);
        String prompt = textOrNull(root.get("prompt"));
        if (ActivityType.MULTIPLE_CHOICE.name().equals(type)) {
            List<ActivityOption> options = new ArrayList<>();
            JsonNode optionsNode = root.get("options");
            if (optionsNode != null && optionsNode.isArray()) {
                for (JsonNode option : optionsNode) {
                    options.add(new ActivityOption(textOrNull(option.get("id")), textOrNull(option.get("text"))));
                }
            }
            return new ActivityStudentDto(prompt, options);
        }
        return new ActivityStudentDto(prompt, null);
    }

    /**
     * Solución para mostrar tras calificar. Para MULTIPLE_CHOICE es la CLAVE de la opción
     * correcta (el id, p. ej. "b"), reutilizable como {@code answer}; para FILL_BLANK, el
     * texto aceptado (igual que acepta el calificador).
     */
    public String extractSolution(ActivityType type, JsonNode content) {
        if (type == ActivityType.MULTIPLE_CHOICE) {
            return textOrNull(content.get("answer"));
        }
        JsonNode accepted = content.get("accepted");
        if (accepted != null && accepted.isArray()) {
            List<String> values = new ArrayList<>();
            for (JsonNode value : accepted) {
                values.add(value.asText());
            }
            return String.join(", ", values);
        }
        return null;
    }

    public String extractExplanation(JsonNode content) {
        return textOrNull(content.get("explanation"));
    }

    private String textOrNull(JsonNode node) {
        return node == null || node.isNull() ? null : node.asText();
    }
}
package com.englishlab.grading;

import com.englishlab.exception.BusinessRuleException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Locale;
import org.springframework.stereotype.Service;

/**
 * Califica respuestas en el servidor. La clave de respuesta nunca viaja al navegador;
 * aqui solo se compara contra el contenido de la actividad.
 *
 * <p>Formato de {@code content} (JSON):
 * <ul>
 *   <li>MULTIPLE_CHOICE: {@code {"prompt":"...","options":[...],"answer":"b","explanation":"..."}}</li>
 *   <li>FILL_BLANK: {@code {"prompt":"She ___ to school.","accepted":["goes"],"explanation":"..."}}</li>
 * </ul>
 */
@Service
public class GradingService {

    private final ObjectMapper objectMapper;

    public GradingService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public GradingResult grade(ActivityType type, String content, String userAnswer) {
        if (type == null) {
            throw new BusinessRuleException("Tipo de actividad desconocido");
        }
        JsonNode root;
        try {
            root = objectMapper.readTree(content);
        } catch (Exception e) {
            throw new BusinessRuleException("Contenido de actividad no válido", e);
        }
        if (root == null || root.isNull()) {
            throw new BusinessRuleException("Contenido de actividad no válido");
        }
        return switch (type) {
            case MULTIPLE_CHOICE -> gradeMultipleChoice(root, userAnswer);
            case FILL_BLANK -> gradeFillBlank(root, userAnswer);
        };
    }

    private GradingResult gradeMultipleChoice(JsonNode root, String userAnswer) {
        JsonNode answerNode = root.get("answer");
        if (answerNode == null || answerNode.isNull() || answerNode.asText().isBlank()) {
            throw new BusinessRuleException("La actividad no tiene respuesta configurada");
        }
        boolean correct = answerNode.asText().equals(trim(userAnswer));
        return new GradingResult(correct, correct ? 1.0 : 0.0);
    }

    private GradingResult gradeFillBlank(JsonNode root, String userAnswer) {
        JsonNode acceptedNode = root.get("accepted");
        if (acceptedNode == null || !acceptedNode.isArray() || acceptedNode.isEmpty()) {
            throw new BusinessRuleException("La actividad no tiene respuestas aceptadas configuradas");
        }
        String normalized = normalize(userAnswer);
        for (JsonNode accepted : acceptedNode) {
            if (normalize(accepted.asText()).equals(normalized)) {
                return new GradingResult(true, 1.0);
            }
        }
        return new GradingResult(false, 0.0);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Normalizacion para completar espacios: trim, colapsar espacios internos,
     * ignorar mayusculas y punto final.
     */
    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\.+$", "")
                .trim();
    }
}

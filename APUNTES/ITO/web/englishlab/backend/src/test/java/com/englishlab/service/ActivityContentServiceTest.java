package com.englishlab.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.englishlab.dto.activity.ActivityStudentDto;
import com.englishlab.grading.ActivityType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias del parseo de contenido de actividad (solución y vista de estudiante).
 */
class ActivityContentServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ActivityContentService service = new ActivityContentService(objectMapper);

    @Test
    void multipleChoiceSolutionIsTheAnswerKeyNotTheText() throws Exception {
        JsonNode content = objectMapper.readTree(
                "{\"prompt\":\"___ is my best friend, Maria.\","
                        + "\"options\":[{\"id\":\"a\",\"text\":\"He\"},{\"id\":\"b\",\"text\":\"She\"},{\"id\":\"c\",\"text\":\"It\"}],"
                        + "\"answer\":\"b\",\"explanation\":\"usamos she\"}");

        assertThat(service.extractSolution(ActivityType.MULTIPLE_CHOICE, content)).isEqualTo("b");
    }

    @Test
    void fillBlankSolutionIsTheAcceptedText() throws Exception {
        JsonNode content = objectMapper.readTree(
                "{\"prompt\":\"She ___ to school.\",\"accepted\":[\"goes\",\"walks\"]}");

        assertThat(service.extractSolution(ActivityType.FILL_BLANK, content)).isEqualTo("goes, walks");
    }

    @Test
    void studentDtoNeverExposesAnswerKeys() throws Exception {
        ActivityStudentDto dto = service.toStudentDto("MULTIPLE_CHOICE",
                "{\"prompt\":\"___ is my friend.\","
                        + "\"options\":[{\"id\":\"a\",\"text\":\"He\"},{\"id\":\"b\",\"text\":\"She\"}],"
                        + "\"answer\":\"b\",\"accepted\":[\"She\"],\"explanation\":\"...\"}");

        assertThat(dto.prompt()).isEqualTo("___ is my friend.");
        assertThat(dto.options()).hasSize(2);

        String json = objectMapper.writeValueAsString(dto);
        assertThat(json).contains("prompt", "options");
        assertThat(json).doesNotContain("answer", "accepted", "explanation");
    }
}
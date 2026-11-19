package com.englishlab.dto.activity;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Garantiza que el DTO de estudiante NUNCA expone la clave de respuesta.
 */
class ActivityDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void studentDtoDoesNotExposeAnswer() throws Exception {
        ActivityStudentDto dto = new ActivityStudentDto(
                "She ___ to school.",
                List.of(new ActivityOption("a", "goes"), new ActivityOption("b", "went")));

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("prompt").contains("options");
        assertThat(json).doesNotContain("answer", "accepted", "explanation");
    }
}

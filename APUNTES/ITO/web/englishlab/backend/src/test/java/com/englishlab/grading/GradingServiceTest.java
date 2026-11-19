package com.englishlab.grading;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.englishlab.exception.BusinessRuleException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias del calificador (sin Spring ni base de datos).
 */
class GradingServiceTest {

    private final GradingService gradingService = new GradingService(new ObjectMapper());

    private static final String MULTIPLE_CHOICE =
            "{\"prompt\":\"Which word is a verb?\",\"options\":[{\"id\":\"a\",\"text\":\"run\"},{\"id\":\"b\",\"text\":\"red\"}],\"answer\":\"a\",\"explanation\":\"run is a verb\"}";

    private static final String FILL_BLANK =
            "{\"prompt\":\"She ___ to school.\",\"accepted\":[\"goes\"],\"explanation\":\"third person singular\"}";

    @Test
    void multipleChoiceCorrect() {
        GradingResult result = gradingService.grade(ActivityType.MULTIPLE_CHOICE, MULTIPLE_CHOICE, "a");
        assertThat(result.correct()).isTrue();
        assertThat(result.score()).isEqualTo(1.0);
    }

    @Test
    void multipleChoiceIncorrect() {
        GradingResult result = gradingService.grade(ActivityType.MULTIPLE_CHOICE, MULTIPLE_CHOICE, "b");
        assertThat(result.correct()).isFalse();
        assertThat(result.score()).isEqualTo(0.0);
    }

    @Test
    void fillBlankCorrect() {
        GradingResult result = gradingService.grade(ActivityType.FILL_BLANK, FILL_BLANK, "goes");
        assertThat(result.correct()).isTrue();
        assertThat(result.score()).isEqualTo(1.0);
    }

    @Test
    void fillBlankIgnoresCaseSpacesAndFinalPeriod() {
        GradingResult result = gradingService.grade(ActivityType.FILL_BLANK, FILL_BLANK, "  Goes.  ");
        assertThat(result.correct()).isTrue();
    }

    @Test
    void fillBlankAcceptsAnyAcceptedValue() {
        String content = "{\"prompt\":\"...\",\"accepted\":[\"goes\",\"walks\",\"runs\"],\"explanation\":\"...\"}";
        GradingResult result = gradingService.grade(ActivityType.FILL_BLANK, content, "RUNS");
        assertThat(result.correct()).isTrue();
        assertThat(result.score()).isEqualTo(1.0);
    }

    @Test
    void fillBlankIncorrect() {
        GradingResult result = gradingService.grade(ActivityType.FILL_BLANK, FILL_BLANK, "fly");
        assertThat(result.correct()).isFalse();
        assertThat(result.score()).isEqualTo(0.0);
    }

    @Test
    void unknownTypeThrowsBusinessRuleException() {
        assertThatThrownBy(() -> gradingService.grade(null, "{}", "x"))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void malformedContentThrowsBusinessRuleException() {
        assertThatThrownBy(() -> gradingService.grade(ActivityType.MULTIPLE_CHOICE, "{not json", "a"))
                .isInstanceOf(BusinessRuleException.class);
    }
}

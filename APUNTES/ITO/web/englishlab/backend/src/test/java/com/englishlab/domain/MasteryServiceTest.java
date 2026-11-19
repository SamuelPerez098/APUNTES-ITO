package com.englishlab.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias de la lógica pura del dominio adaptativo (sin base de datos).
 */
class MasteryServiceTest {

    private final MasteryService masteryService = new MasteryService();

    @Test
    void formulaFirstCorrectAndFirstIncorrect() {
        // primer acierto desde default 0.30 -> 0.65*1 + 0.35*0.30 = 0.755
        assertThat(masteryService.computeMastery(1.0, MasteryConfig.DEFAULT_MASTERY))
                .isEqualTo(0.755, within(1e-9));
        // primer fallo -> 0.65*0 + 0.35*0.30 = 0.105
        assertThat(masteryService.computeMastery(0.0, MasteryConfig.DEFAULT_MASTERY))
                .isEqualTo(0.105, within(1e-9));
    }

    @Test
    void formulaClampsToRange() {
        assertThat(masteryService.computeMastery(1.0, 1.0)).isEqualTo(1.0, within(1e-9));
        assertThat(masteryService.computeMastery(0.0, 0.0)).isEqualTo(0.0, within(1e-9));
        assertThat(masteryService.computeMastery(2.0, 2.0)).isEqualTo(1.0);
        assertThat(masteryService.computeMastery(-1.0, -1.0)).isEqualTo(0.0);
    }

    @Test
    void roundMasteryToFourDecimals() {
        assertThat(masteryService.roundMastery(0.755)).isEqualTo(0.755, within(1e-9));
        assertThat(masteryService.roundMastery(0.105)).isEqualTo(0.105, within(1e-9));
        assertThat(masteryService.roundMastery(0.7550000000000001)).isEqualTo(0.755, within(1e-9));
    }

    @Test
    void eachState() {
        assertThat(masteryService.computeState(0.80, true)).isEqualTo(ObjectiveState.MASTERED);
        assertThat(masteryService.computeState(0.95, false)).isEqualTo(ObjectiveState.MASTERED);
        assertThat(masteryService.computeState(0.30, false)).isEqualTo(ObjectiveState.BLOCKED);
        assertThat(masteryService.computeState(0.30, true)).isEqualTo(ObjectiveState.AVAILABLE);
    }

    @Test
    void recommendBreaksTiesByCefrThenCode() {
        // misma mastery -> gana CEFR menor (A1)
        ObjectiveDomain b1 = new ObjectiveDomain(2L, "BBB", "n", "B1", "grammar", 0.30, Set.of());
        ObjectiveDomain a1 = new ObjectiveDomain(1L, "AAA", "n", "A1", "grammar", 0.30, Set.of());
        Optional<Long> r1 = masteryService.recommend(List.of(b1, a1), Map.of(1L, 0.30, 2L, 0.30));
        assertThat(r1).contains(1L);

        // misma mastery y CEFR -> gana código menor
        ObjectiveDomain c1 = new ObjectiveDomain(3L, "Z01", "n", "A2", "grammar", 0.30, Set.of());
        ObjectiveDomain c2 = new ObjectiveDomain(4L, "A02", "n", "A2", "grammar", 0.30, Set.of());
        Optional<Long> r2 = masteryService.recommend(List.of(c1, c2), Map.of(3L, 0.30, 4L, 0.30));
        assertThat(r2).contains(4L);
    }

    @Test
    void recommendPicksLowestMastery() {
        ObjectiveDomain high = new ObjectiveDomain(6L, "HIG", "n", "A1", "grammar", 0.50, Set.of());
        ObjectiveDomain low = new ObjectiveDomain(5L, "LOW", "n", "B2", "grammar", 0.40, Set.of());
        Optional<Long> r = masteryService.recommend(List.of(high, low), Map.of(5L, 0.40, 6L, 0.50));
        assertThat(r).contains(5L);
    }

    @Test
    void recommendEmptyWhenAllMastered() {
        ObjectiveDomain m = new ObjectiveDomain(1L, "AAA", "n", "A1", "grammar", 0.90, Set.of());
        assertThat(masteryService.recommend(List.of(m), Map.of(1L, 0.90))).isEmpty();
    }

    @Test
    void recommendSkipsBlocked() {
        ObjectiveDomain blocked = new ObjectiveDomain(1L, "B1", "n", "A1", "grammar", 0.30, Set.of(2L));
        assertThat(masteryService.recommend(List.of(blocked), Map.of(1L, 0.30, 2L, 0.30))).isEmpty();
    }

    @Test
    void chooseActivityPrefersNeverAttemptedThenOldestThenLowestId() {
        Instant old = Instant.parse("2020-01-01T00:00:00Z");
        Instant newer = Instant.parse("2021-01-01T00:00:00Z");

        // nunca intentada (null) gana
        assertThat(masteryService.chooseActivity(List.of(
                new ActivityCandidate(10L, old),
                new ActivityCandidate(11L, null)))).contains(11L);

        // todas intentadas -> la más antigua
        assertThat(masteryService.chooseActivity(List.of(
                new ActivityCandidate(10L, newer),
                new ActivityCandidate(11L, old)))).contains(11L);

        // empate de fecha -> menor id
        assertThat(masteryService.chooseActivity(List.of(
                new ActivityCandidate(12L, old),
                new ActivityCandidate(11L, old)))).contains(11L);
    }

    @Test
    void acyclicSeedGraphHasNoCycle() {
        List<long[]> edges = List.of(
                new long[]{4, 2}, new long[]{5, 2}, new long[]{6, 4}, new long[]{7, 4},
                new long[]{8, 2}, new long[]{9, 4}, new long[]{10, 7}, new long[]{11, 4},
                new long[]{12, 11}, new long[]{13, 10}, new long[]{14, 7});
        assertThat(masteryService.hasCycle(edges)).isFalse();
    }

    @Test
    void cyclicGraphIsDetected() {
        List<long[]> edges = List.of(new long[]{1, 2}, new long[]{2, 3}, new long[]{3, 1});
        assertThat(masteryService.hasCycle(edges)).isTrue();
    }
}
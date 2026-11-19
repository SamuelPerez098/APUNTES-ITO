package com.englishlab.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Lógica pura del dominio adaptativo: fórmula de dominio, estados de objetivo,
 * recomendación, selección de actividad y detección de ciclos en el grafo de prerrequisitos.
 * Sin dependencias de base de datos (testeable de forma unitaria).
 */
@Service
public class MasteryService {

    /** mastery = clamp(W_RECENT * recent + W_HIST * historical, 0, 1). */
    public double computeMastery(double recent, double historical) {
        return clamp(MasteryConfig.W_RECENT * recent + MasteryConfig.W_HIST * historical, 0.0, 1.0);
    }

    /** Redondea el dominio a 4 decimales (numeric(5,4)). */
    public double roundMastery(double mastery) {
        return BigDecimal.valueOf(mastery).setScale(4, RoundingMode.HALF_UP).doubleValue();
    }

    public ObjectiveState computeState(double mastery, boolean allPrerequisitesMastered) {
        if (mastery >= MasteryConfig.MASTERED_THRESHOLD) {
            return ObjectiveState.MASTERED;
        }
        if (!allPrerequisitesMastered) {
            return ObjectiveState.BLOCKED;
        }
        return ObjectiveState.AVAILABLE;
    }

    /**
     * Elige el objetivo recomendado: el AVAILABLE con menor dominio; desempate por nivel CEFR
     * (menor) y luego por código.
     */
    public Optional<Long> recommend(List<ObjectiveDomain> objectives, Map<Long, Double> mastery) {
        return objectives.stream()
                .filter(o -> isAvailable(o, mastery))
                .min(Comparator
                        .comparingDouble(ObjectiveDomain::mastery)
                        .thenComparingInt(o -> cefrRank(o.cefrLevel()))
                        .thenComparing(ObjectiveDomain::code))
                .map(ObjectiveDomain::id);
    }

    /** Elige la actividad menos recientemente intentada (nunca intentada cuenta como la menos reciente). */
    public Optional<Long> chooseActivity(List<ActivityCandidate> candidates) {
        return candidates.stream()
                .min(Comparator
                        .comparing((ActivityCandidate c) -> c.lastAttemptAt(),
                                Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparingLong(ActivityCandidate::activityId))
                .map(ActivityCandidate::activityId);
    }

    public int cefrRank(String cefrLevel) {
        return switch (cefrLevel == null ? "" : cefrLevel) {
            case "A1" -> 0;
            case "A2" -> 1;
            case "B1" -> 2;
            case "B2" -> 3;
            default -> Integer.MAX_VALUE;
        };
    }

    /**
     * Detecta si el grafo de prerrequisitos (aristas objetivo -> prerrequisito) tiene un ciclo.
     */
    public boolean hasCycle(List<long[]> edges) {
        Map<Long, List<Long>> adjacency = new HashMap<>();
        Set<Long> nodes = new HashSet<>();
        for (long[] edge : edges) {
            adjacency.computeIfAbsent(edge[0], k -> new ArrayList<>()).add(edge[1]);
            nodes.add(edge[0]);
            nodes.add(edge[1]);
        }
        Map<Long, Integer> color = new HashMap<>(); // 0 = white, 1 = gray, 2 = black
        for (Long node : nodes) {
            if (dfs(node, adjacency, color)) {
                return true;
            }
        }
        return false;
    }

    private boolean dfs(Long node, Map<Long, List<Long>> adjacency, Map<Long, Integer> color) {
        int state = color.getOrDefault(node, 0);
        if (state == 1) {
            return true; // arista hacia un nodo en exploración -> ciclo
        }
        if (state == 2) {
            return false;
        }
        color.put(node, 1);
        for (Long neighbor : adjacency.getOrDefault(node, List.of())) {
            if (dfs(neighbor, adjacency, color)) {
                return true;
            }
        }
        color.put(node, 2);
        return false;
    }

    private boolean isAvailable(ObjectiveDomain objective, Map<Long, Double> mastery) {
        if (objective.mastery() >= MasteryConfig.MASTERED_THRESHOLD) {
            return false;
        }
        return objective.prerequisites().stream()
                .allMatch(p -> mastery.getOrDefault(p, MasteryConfig.DEFAULT_MASTERY) >= MasteryConfig.MASTERED_THRESHOLD);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
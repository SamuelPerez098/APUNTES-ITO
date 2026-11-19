package com.englishlab.service;

import com.englishlab.domain.ActivityCandidate;
import com.englishlab.domain.MasteryConfig;
import com.englishlab.domain.MasteryService;
import com.englishlab.domain.ObjectiveDomain;
import com.englishlab.dto.recommendation.RecommendationResponse;
import com.englishlab.model.entity.Activity;
import com.englishlab.model.entity.Attempt;
import com.englishlab.repository.ActivityRepository;
import com.englishlab.repository.AttemptRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Recomendación de la siguiente actividad a partir del objetivo recomendado.
 */
@Service
public class RecommendationService {

    private final ObjectiveStateService objectiveStateService;
    private final ActivityRepository activityRepository;
    private final AttemptRepository attemptRepository;
    private final MasteryService masteryService;

    public RecommendationService(
            ObjectiveStateService objectiveStateService,
            ActivityRepository activityRepository,
            AttemptRepository attemptRepository,
            MasteryService masteryService) {
        this.objectiveStateService = objectiveStateService;
        this.activityRepository = activityRepository;
        this.attemptRepository = attemptRepository;
        this.masteryService = masteryService;
    }

    @Transactional(readOnly = true)
    public RecommendationResponse next(Long userId) {
        List<ObjectiveDomain> domains = objectiveStateService.loadDomains(userId);
        Map<Long, ObjectiveDomain> byId = domains.stream()
                .collect(Collectors.toMap(ObjectiveDomain::id, d -> d));
        Map<Long, Double> mastery = domains.stream()
                .collect(Collectors.toMap(ObjectiveDomain::id, ObjectiveDomain::mastery));

        Optional<Long> recommendedId = masteryService.recommend(domains, mastery);
        if (recommendedId.isEmpty()) {
            return new RecommendationResponse(null, null, null,
                    "Has dominado todos los objetivos disponibles. ¡Buen trabajo!");
        }

        ObjectiveDomain recommended = byId.get(recommendedId.get());
        List<Activity> activities = activityRepository.findByObjectiveIdOrderByIdAsc(recommended.id());
        if (activities.isEmpty()) {
            return new RecommendationResponse(null, null, null,
                    "No hay actividades disponibles en este momento.");
        }

        Map<Long, Instant> lastAttempt = lastAttemptByActivity(userId, recommended.id());
        List<ActivityCandidate> candidates = activities.stream()
                .map(a -> new ActivityCandidate(a.getId(), lastAttempt.get(a.getId())))
                .toList();
        Long activityId = masteryService.chooseActivity(candidates).orElseThrow();

        List<String> masteredPrerequisites = recommended.prerequisites().stream()
                .map(byId::get)
                .filter(p -> p != null && p.mastery() >= MasteryConfig.MASTERED_THRESHOLD)
                .map(ObjectiveDomain::name)
                .toList();

        return new RecommendationResponse(recommended.id(), activityId, buildReason(recommended, masteredPrerequisites), null);
    }

    private Map<Long, Instant> lastAttemptByActivity(Long userId, Long objectiveId) {
        Map<Long, Instant> last = new HashMap<>();
        for (Attempt attempt : attemptRepository.findByUserIdAndObjectiveId(userId, objectiveId)) {
            Long activityId = attempt.getActivity().getId();
            Instant attemptedAt = attempt.getAttemptedAt();
            if (!last.containsKey(activityId) || attemptedAt.isAfter(last.get(activityId))) {
                last.put(activityId, attemptedAt);
            }
        }
        return last;
    }

    private String buildReason(ObjectiveDomain objective, List<String> masteredPrerequisites) {
        String masteryPct = String.format(Locale.ROOT, "%.0f%%", objective.mastery() * 100);
        StringBuilder reason = new StringBuilder();
        reason.append("Recomendado: ").append(objective.name())
                .append(" (").append(objective.cefrLevel()).append(").");
        reason.append(" Dominio actual: ").append(masteryPct).append(".");
        if (masteredPrerequisites.isEmpty()) {
            reason.append(" No tiene prerrequisitos pendientes.");
        } else {
            reason.append(" Prerrequisitos ya dominados: ")
                    .append(String.join(", ", masteredPrerequisites)).append(".");
        }
        return reason.toString();
    }
}
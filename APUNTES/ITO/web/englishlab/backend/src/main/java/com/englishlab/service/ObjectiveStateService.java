package com.englishlab.service;

import com.englishlab.domain.MasteryConfig;
import com.englishlab.domain.MasteryService;
import com.englishlab.domain.ObjectiveDomain;
import com.englishlab.domain.ObjectiveState;
import com.englishlab.dto.activity.ActivityStudentSummaryDto;
import com.englishlab.dto.common.PageResponse;
import com.englishlab.dto.objective.ObjectiveResponse;
import com.englishlab.dto.objective.ObjectiveStateResponse;
import com.englishlab.dto.objective.PracticeSessionResponse;
import com.englishlab.exception.BusinessRuleException;
import com.englishlab.exception.NotFoundException;
import com.englishlab.model.entity.Activity;
import com.englishlab.model.entity.LearnerSkillState;
import com.englishlab.model.entity.Objective;
import com.englishlab.repository.ActivityRepository;
import com.englishlab.repository.LearnerSkillStateRepository;
import com.englishlab.repository.ObjectiveRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Estados de los objetivos para un alumno (MASTERED/BLOCKED/AVAILABLE/RECOMMENDED).
 * Carga en dos consultas (objetivos+prerrequisitos con JOIN FETCH; estados del usuario) y
 * calcula en memoria, sin N+1.
 */
@Service
public class ObjectiveStateService {

    /** Tamaño por defecto y tope de una sesión de práctica (el cliente solo sugiere). */
    private static final int DEFAULT_PRACTICE_SESSION_SIZE = 10;
    private static final int MAX_PRACTICE_SESSION_SIZE = 20;

    private final ObjectiveRepository objectiveRepository;
    private final LearnerSkillStateRepository learnerSkillStateRepository;
    private final ActivityRepository activityRepository;
    private final MasteryService masteryService;

    public ObjectiveStateService(
            ObjectiveRepository objectiveRepository,
            LearnerSkillStateRepository learnerSkillStateRepository,
            ActivityRepository activityRepository,
            MasteryService masteryService) {
        this.objectiveRepository = objectiveRepository;
        this.learnerSkillStateRepository = learnerSkillStateRepository;
        this.activityRepository = activityRepository;
        this.masteryService = masteryService;
    }

    @Transactional(readOnly = true)
    public List<ObjectiveStateResponse> myObjectives(Long userId) {
        List<Objective> objectives = objectiveRepository.findAllWithPrerequisites();
        Map<Long, LearnerSkillState> states = loadStates(userId);
        List<ObjectiveDomain> domains = toDomains(objectives, states);
        Map<Long, Double> mastery = toMasteryMap(domains);
        Optional<Long> recommended = masteryService.recommend(domains, mastery);

        return domains.stream()
                .map(d -> toResponse(d, mastery, recommended, states.get(d.id())))
                .toList();
    }

    /**
     * Práctica libre: actividades paginadas de un objetivo para el alumno autenticado.
     * El estado se calcula con la misma lógica que {@link #myObjectives(Long)}; un objetivo
     * BLOCKED no se puede practicar. Solo devuelve el resumen (sin prompt ni clave).
     */
    @Transactional(readOnly = true)
    public PageResponse<ActivityStudentSummaryDto> objectiveActivities(
            Long userId, Long objectiveId, Pageable pageable) {
        requirePracticableObjective(userId, objectiveId);

        Page<Activity> page = activityRepository.findByObjectiveId(objectiveId, pageable);
        List<ActivityStudentSummaryDto> content = page.getContent().stream()
                .map(a -> new ActivityStudentSummaryDto(
                        a.getId(), a.getTitle(), a.getType(), a.getLevel(), a.getSkill()))
                .toList();
        return new PageResponse<>(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    /**
     * Sesión de práctica sin repetición: ids de actividades del objetivo elegidos por el
     * servidor (nunca intentadas primero, luego las de intento más antiguo). Mismas reglas
     * de acceso que {@link #objectiveActivities}: 404 si el objetivo no existe y 422 si
     * está BLOCKED. El tamaño por defecto es 10 y nunca supera 20.
     */
    @Transactional(readOnly = true)
    public PracticeSessionResponse practiceSession(Long userId, Long objectiveId, Integer size) {
        requirePracticableObjective(userId, objectiveId);

        int limit = size == null || size < 1
                ? DEFAULT_PRACTICE_SESSION_SIZE
                : Math.min(size, MAX_PRACTICE_SESSION_SIZE);
        return new PracticeSessionResponse(
                activityRepository.findPracticeSessionActivityIds(objectiveId, userId, limit));
    }

    /**
     * Carga los estados en dos consultas y valida que el objetivo exista (404) y no esté
     * bloqueado por prerrequisitos (422). Misma lógica que {@link #myObjectives(Long)}.
     */
    private void requirePracticableObjective(Long userId, Long objectiveId) {
        List<ObjectiveDomain> domains = loadDomains(userId);
        ObjectiveDomain domain = domains.stream()
                .filter(d -> d.id().equals(objectiveId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Objetivo no encontrado"));
        Map<Long, Double> mastery = toMasteryMap(domains);
        Optional<Long> recommended = masteryService.recommend(domains, mastery);
        if (resolveState(domain, mastery, recommended) == ObjectiveState.BLOCKED) {
            throw new BusinessRuleException("El objetivo está bloqueado por prerrequisitos");
        }
    }

    /** Listado paginado de objetivos (para el editor de actividades del frontend). */
    @Transactional(readOnly = true)
    public PageResponse<ObjectiveResponse> listObjectives(Pageable pageable) {
        Page<Objective> page = objectiveRepository.findAll(pageable);
        List<ObjectiveResponse> content = page.getContent().stream()
                .map(o -> new ObjectiveResponse(o.getId(), o.getCode(), o.getName(), o.getCefrLevel(), o.getSkill()))
                .toList();
        return new PageResponse<>(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    /** Dominios planos del alumno (para el cálculo de recomendación en otras partes). */
    @Transactional(readOnly = true)
    public List<ObjectiveDomain> loadDomains(Long userId) {
        List<Objective> objectives = objectiveRepository.findAllWithPrerequisites();
        return toDomains(objectives, loadStates(userId));
    }

    private Map<Long, LearnerSkillState> loadStates(Long userId) {
        return learnerSkillStateRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(LearnerSkillState::getObjectiveId, state -> state));
    }

    private List<ObjectiveDomain> toDomains(
            List<Objective> objectives, Map<Long, LearnerSkillState> states) {
        return objectives.stream().map(o -> toDomain(o, states.get(o.getId()))).toList();
    }

    private ObjectiveDomain toDomain(Objective objective, LearnerSkillState state) {
        Set<Long> prerequisites = objective.getPrerequisiteLinks().stream()
                .map(link -> link.getPrerequisite().getId())
                .collect(Collectors.toSet());
        double mastery = state == null
                ? MasteryConfig.DEFAULT_MASTERY
                : state.getMastery().doubleValue();
        return new ObjectiveDomain(
                objective.getId(),
                objective.getCode(),
                objective.getName(),
                objective.getCefrLevel(),
                objective.getSkill(),
                mastery,
                prerequisites);
    }

    private ObjectiveStateResponse toResponse(
            ObjectiveDomain domain,
            Map<Long, Double> mastery,
            Optional<Long> recommended,
            LearnerSkillState state) {
        return new ObjectiveStateResponse(
                domain.id(),
                domain.code(),
                domain.name(),
                domain.cefrLevel(),
                domain.skill(),
                masteryService.roundMastery(domain.mastery()),
                state == null || state.getPreviousMastery() == null
                        ? null
                        : state.getPreviousMastery().doubleValue(),
                resolveState(domain, mastery, recommended));
    }

    private ObjectiveState resolveState(
            ObjectiveDomain domain, Map<Long, Double> mastery, Optional<Long> recommended) {
        boolean allPrerequisitesMastered = domain.prerequisites().stream()
                .allMatch(p -> mastery.getOrDefault(p, MasteryConfig.DEFAULT_MASTERY) >= MasteryConfig.MASTERED_THRESHOLD);
        ObjectiveState state = masteryService.computeState(domain.mastery(), allPrerequisitesMastered);
        if (recommended.isPresent() && recommended.get().equals(domain.id())) {
            state = ObjectiveState.RECOMMENDED;
        }
        return state;
    }

    private Map<Long, Double> toMasteryMap(List<ObjectiveDomain> domains) {
        return domains.stream().collect(Collectors.toMap(ObjectiveDomain::id, ObjectiveDomain::mastery));
    }
}
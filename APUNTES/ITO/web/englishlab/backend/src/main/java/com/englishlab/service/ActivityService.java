package com.englishlab.service;

import com.englishlab.dto.activity.ActivityAdminResponse;
import com.englishlab.dto.activity.ActivityRequest;
import com.englishlab.dto.activity.ActivityStudentDetailDto;
import com.englishlab.dto.activity.ActivityStudentDto;
import com.englishlab.dto.common.PageResponse;
import com.englishlab.exception.BadRequestException;
import com.englishlab.exception.NotFoundException;
import com.englishlab.grading.ActivityType;
import com.englishlab.model.entity.Activity;
import com.englishlab.model.entity.Category;
import com.englishlab.model.entity.Objective;
import com.englishlab.repository.ActivityRepository;
import com.englishlab.repository.CategoryRepository;
import com.englishlab.repository.ObjectiveRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CRUD de actividades (ADMIN+TEACHER). Valida el JSON de {@code content} según el tipo
 * antes de persistir; la clave de respuesta solo se expone en la vista admin.
 */
@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final ObjectiveRepository objectiveRepository;
    private final CategoryRepository categoryRepository;
    private final ActivityContentService activityContentService;
    private final ObjectMapper objectMapper;

    public ActivityService(
            ActivityRepository activityRepository,
            ObjectiveRepository objectiveRepository,
            CategoryRepository categoryRepository,
            ActivityContentService activityContentService,
            ObjectMapper objectMapper) {
        this.activityRepository = activityRepository;
        this.objectiveRepository = objectiveRepository;
        this.categoryRepository = categoryRepository;
        this.activityContentService = activityContentService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<ActivityAdminResponse> list(
            String level, String skill, String type, Long categoryId, String text, Pageable pageable) {
        Specification<Activity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (level != null && !level.isBlank()) {
                predicates.add(cb.equal(root.get("level"), level.trim()));
            }
            if (skill != null && !skill.isBlank()) {
                predicates.add(cb.equal(root.get("skill"), skill.trim()));
            }
            if (type != null && !type.isBlank()) {
                predicates.add(cb.equal(root.get("type"), parseType(type)));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (text != null && !text.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + text.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Activity> page = activityRepository.findAll(spec, pageable);
        List<ActivityAdminResponse> content = page.getContent().stream().map(this::toResponse).toList();
        return new PageResponse<>(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    @Transactional
    public ActivityAdminResponse create(ActivityRequest request) {
        Objective objective = objectiveRepository.findById(request.objectiveId())
                .orElseThrow(() -> new NotFoundException("Objetivo no encontrado"));
        Category category = request.categoryId() == null
                ? null
                : categoryRepository.findById(request.categoryId())
                        .orElseThrow(() -> new NotFoundException("Categoría no encontrada"));
        validateContent(request.type(), request.content());

        Activity activity = new Activity();
        activity.setObjective(objective);
        activity.setType(request.type().name());
        activity.setLevel(request.level().trim());
        activity.setSkill(request.skill().trim());
        activity.setTitle(request.title().trim());
        activity.setContent(request.content().toString());
        activity.setCategory(category);
        return toResponse(activityRepository.save(activity));
    }

    @Transactional
    public ActivityAdminResponse update(Long id, ActivityRequest request) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Actividad no encontrada"));
        Objective objective = objectiveRepository.findById(request.objectiveId())
                .orElseThrow(() -> new NotFoundException("Objetivo no encontrado"));
        Category category = request.categoryId() == null
                ? null
                : categoryRepository.findById(request.categoryId())
                        .orElseThrow(() -> new NotFoundException("Categoría no encontrada"));
        validateContent(request.type(), request.content());

        activity.setObjective(objective);
        activity.setType(request.type().name());
        activity.setLevel(request.level().trim());
        activity.setSkill(request.skill().trim());
        activity.setTitle(request.title().trim());
        activity.setContent(request.content().toString());
        activity.setCategory(category);
        return toResponse(activity);
    }

    @Transactional
    public void delete(Long id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Actividad no encontrada"));
        activityRepository.delete(activity);
    }

    /**
     * Vista de estudiante de una actividad concreta (soporte a "Practicar lo recomendado").
     * Reutiliza el DTO de estudiante: nunca incluye answer, accepted ni explanation.
     */
    @Transactional(readOnly = true)
    public ActivityStudentDetailDto studentActivity(Long id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Actividad no encontrada"));
        ActivityStudentDto content = activityContentService.toStudentDto(activity.getType(), activity.getContent());
        return new ActivityStudentDetailDto(
                activity.getId(),
                activity.getType(),
                activity.getLevel(),
                activity.getSkill(),
                activity.getTitle(),
                content.prompt(),
                content.options());
    }

    private String parseType(String type) {
        try {
            return ActivityType.valueOf(type.trim().toUpperCase(Locale.ROOT)).name();
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Tipo de actividad inválido: " + type);
        }
    }

    private void validateContent(ActivityType type, JsonNode content) {
        if (content == null || content.isNull()) {
            throw new BadRequestException("El contenido de la actividad es obligatorio");
        }
        switch (type) {
            case MULTIPLE_CHOICE -> validateMultipleChoice(content);
            case FILL_BLANK -> validateFillBlank(content);
        }
    }

    private void validateMultipleChoice(JsonNode root) {
        if (!root.isObject()) {
            throw new BadRequestException("El contenido debe ser un objeto JSON");
        }
        String prompt = textOrNull(root.get("prompt"));
        if (prompt == null || prompt.isBlank()) {
            throw new BadRequestException("MULTIPLE_CHOICE requiere un prompt");
        }
        JsonNode options = root.get("options");
        if (options == null || !options.isArray() || options.size() < 3) {
            throw new BadRequestException("MULTIPLE_CHOICE requiere al menos 3 opciones");
        }
        Set<String> ids = new HashSet<>();
        for (JsonNode option : options) {
            if (option == null || !option.isObject()) {
                throw new BadRequestException("Cada opción debe ser un objeto");
            }
            String id = textOrNull(option.get("id"));
            if (id == null || id.isBlank()) {
                throw new BadRequestException("Cada opción debe tener un id");
            }
            if (!ids.add(id)) {
                throw new BadRequestException("Los ids de las opciones deben ser únicos");
            }
            String text = textOrNull(option.get("text"));
            if (text == null || text.isBlank()) {
                throw new BadRequestException("Cada opción debe tener un texto");
            }
        }
        String answer = textOrNull(root.get("answer"));
        if (answer == null || answer.isBlank()) {
            throw new BadRequestException("MULTIPLE_CHOICE requiere una respuesta (answer)");
        }
        if (!ids.contains(answer)) {
            throw new BadRequestException("La respuesta (answer) debe coincidir con el id de una opción");
        }
    }

    private void validateFillBlank(JsonNode root) {
        if (!root.isObject()) {
            throw new BadRequestException("El contenido debe ser un objeto JSON");
        }
        String prompt = textOrNull(root.get("prompt"));
        if (prompt == null || prompt.isBlank()) {
            throw new BadRequestException("FILL_BLANK requiere un prompt");
        }
        JsonNode accepted = root.get("accepted");
        if (accepted == null || !accepted.isArray() || accepted.isEmpty()) {
            throw new BadRequestException("FILL_BLANK requiere al menos una respuesta aceptada (accepted)");
        }
        for (JsonNode value : accepted) {
            if (value == null || value.isNull() || value.asText().isBlank()) {
                throw new BadRequestException("Las respuestas aceptadas no pueden estar vacías");
            }
        }
    }

    private String textOrNull(JsonNode node) {
        return node == null || node.isNull() ? null : node.asText();
    }

    private ActivityAdminResponse toResponse(Activity activity) {
        return new ActivityAdminResponse(
                activity.getId(),
                activity.getObjective().getId(),
                activity.getType(),
                activity.getLevel(),
                activity.getSkill(),
                activity.getTitle(),
                activity.getCategory() == null ? null : activity.getCategory().getId(),
                parseContent(activity.getContent()));
    }

    private JsonNode parseContent(String content) {
        try {
            return objectMapper.readTree(content);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Contenido JSON inválido almacenado en la base de datos", e);
        }
    }
}
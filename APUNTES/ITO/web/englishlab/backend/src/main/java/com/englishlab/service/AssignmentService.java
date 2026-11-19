package com.englishlab.service;

import com.englishlab.domain.MasteryConfig;
import com.englishlab.domain.MasteryService;
import com.englishlab.dto.assignment.AssignmentCreateRequest;
import com.englishlab.dto.assignment.AssignmentResponse;
import com.englishlab.dto.assignment.StudentAssignmentResponse;
import com.englishlab.dto.common.PageResponse;
import com.englishlab.dto.progress.ProgressResponse;
import com.englishlab.exception.NotFoundException;
import com.englishlab.model.entity.Activity;
import com.englishlab.model.entity.Assignment;
import com.englishlab.model.entity.AssignmentStatus;
import com.englishlab.model.entity.LearnerSkillState;
import com.englishlab.model.entity.Objective;
import com.englishlab.model.entity.User;
import com.englishlab.repository.ActivityRepository;
import com.englishlab.repository.AssignmentRepository;
import com.englishlab.repository.LearnerSkillStateRepository;
import com.englishlab.repository.ObjectiveRepository;
import com.englishlab.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Asignaciones: creación por el TEACHER, listado del alumno y progreso por alumno.
 */
@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final ObjectiveRepository objectiveRepository;
    private final LearnerSkillStateRepository learnerSkillStateRepository;
    private final ActivityContentService activityContentService;
    private final MasteryService masteryService;

    public AssignmentService(
            AssignmentRepository assignmentRepository,
            UserRepository userRepository,
            ActivityRepository activityRepository,
            ObjectiveRepository objectiveRepository,
            LearnerSkillStateRepository learnerSkillStateRepository,
            ActivityContentService activityContentService,
            MasteryService masteryService) {
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
        this.objectiveRepository = objectiveRepository;
        this.learnerSkillStateRepository = learnerSkillStateRepository;
        this.activityContentService = activityContentService;
        this.masteryService = masteryService;
    }

    @Transactional
    public AssignmentResponse create(Long teacherId, AssignmentCreateRequest request) {
        User student = userRepository.findById(request.studentId())
                .orElseThrow(() -> new NotFoundException("Estudiante no encontrado"));
        if (student.getTeacher() == null || !student.getTeacher().getId().equals(teacherId)) {
            throw new AccessDeniedException("El estudiante no está vinculado a este profesor");
        }
        Activity activity = activityRepository.findById(request.activityId())
                .orElseThrow(() -> new NotFoundException("Actividad no encontrada"));

        Assignment assignment = new Assignment();
        assignment.setTeacher(userRepository.getReferenceById(teacherId));
        assignment.setStudent(student);
        assignment.setActivity(activity);
        assignment.setStatus(AssignmentStatus.PENDING);
        assignment.setAssignedAt(Instant.now());
        assignment = assignmentRepository.save(assignment);
        return toResponse(assignment);
    }

    @Transactional(readOnly = true)
    public PageResponse<StudentAssignmentResponse> myAssignments(Long studentId, Pageable pageable) {
        Page<Assignment> page = assignmentRepository.findByStudentId(studentId, pageable);
        List<StudentAssignmentResponse> content = page.getContent().stream()
                .map(this::toStudentResponse)
                .toList();
        return new PageResponse<>(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    @Transactional(readOnly = true)
    public List<ProgressResponse> progress(Long teacherId, Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Estudiante no encontrado"));
        if (student.getTeacher() == null || !student.getTeacher().getId().equals(teacherId)) {
            throw new AccessDeniedException("El estudiante no está vinculado a este profesor");
        }

        List<Objective> objectives = objectiveRepository.findAllByOrderByIdAsc();
        Map<Long, Double> mastery = objectives.stream()
                .collect(Collectors.toMap(Objective::getId, o -> MasteryConfig.DEFAULT_MASTERY));
        for (LearnerSkillState state : learnerSkillStateRepository.findByUserId(studentId)) {
            mastery.put(state.getObjectiveId(), state.getMastery().doubleValue());
        }

        return objectives.stream()
                .map(o -> new ProgressResponse(
                        o.getId(),
                        o.getCode(),
                        o.getName(),
                        o.getCefrLevel(),
                        o.getSkill(),
                        masteryService.roundMastery(mastery.getOrDefault(o.getId(), MasteryConfig.DEFAULT_MASTERY))))
                .toList();
    }

    private AssignmentResponse toResponse(Assignment assignment) {
        return new AssignmentResponse(
                assignment.getId(),
                assignment.getStudent().getId(),
                assignment.getActivity().getId(),
                assignment.getStatus().name(),
                assignment.getAssignedAt(),
                assignment.getCompletedAt());
    }

    private StudentAssignmentResponse toStudentResponse(Assignment assignment) {
        Activity activity = assignment.getActivity();
        return new StudentAssignmentResponse(
                assignment.getId(),
                activity.getId(),
                assignment.getStatus().name(),
                activityContentService.toStudentDto(activity.getType(), activity.getContent()));
    }
}
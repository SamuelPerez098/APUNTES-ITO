package com.englishlab.service;

import com.englishlab.dto.common.PageResponse;
import com.englishlab.dto.teacher.StudentResponse;
import com.englishlab.exception.BusinessRuleException;
import com.englishlab.exception.NotFoundException;
import com.englishlab.model.entity.RoleName;
import com.englishlab.model.entity.User;
import com.englishlab.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestión de estudiantes por parte del TEACHER: vincula un estudiante a su
 * {@code teacher_id} y lista únicamente sus propios alumnos.
 */
@Service
public class TeacherStudentService {

    private final UserRepository userRepository;

    public TeacherStudentService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public StudentResponse assign(Long teacherId, String email) {
        User student = userRepository.findByEmailIgnoreCase(normalizeEmail(email))
                .orElseThrow(() -> new NotFoundException("Estudiante no encontrado"));
        if (student.getRole().getName() != RoleName.STUDENT) {
            throw new BusinessRuleException("El usuario no es un estudiante");
        }
        if (student.getTeacher() != null) {
            throw new BusinessRuleException("El estudiante ya tiene un profesor asignado");
        }
        student.setTeacher(userRepository.getReferenceById(teacherId));
        return toResponse(student);
    }

    @Transactional(readOnly = true)
    public PageResponse<StudentResponse> list(Long teacherId, String text, Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("teacher").get("id"), teacherId));
            if (text != null && !text.isBlank()) {
                String like = "%" + text.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("email")), like)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<User> page = userRepository.findAll(spec, pageable);
        List<StudentResponse> content = page.getContent().stream().map(this::toResponse).toList();
        return new PageResponse<>(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    private StudentResponse toResponse(User user) {
        return new StudentResponse(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
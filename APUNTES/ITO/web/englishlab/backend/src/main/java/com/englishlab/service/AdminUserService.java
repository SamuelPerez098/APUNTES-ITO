package com.englishlab.service;

import com.englishlab.dto.common.PageResponse;
import com.englishlab.dto.user.UserAdminRequest;
import com.englishlab.dto.user.UserAdminResponse;
import com.englishlab.exception.BadRequestException;
import com.englishlab.exception.BusinessRuleException;
import com.englishlab.exception.NotFoundException;
import com.englishlab.model.entity.Role;
import com.englishlab.model.entity.RoleName;
import com.englishlab.model.entity.User;
import com.englishlab.repository.RoleRepository;
import com.englishlab.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CRUD de usuarios para ADMIN. Filtros y paginación en servidor; teacherName resuelto
 * sin N+1 (entity graph role+teacher en {@link UserRepository#findAll}).
 */
@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserAdminResponse> list(String role, String text, Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (role != null && !role.isBlank()) {
                predicates.add(cb.equal(root.get("role").get("name"), parseRole(role)));
            }
            if (text != null && !text.isBlank()) {
                String like = "%" + text.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("email")), like)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<User> page = userRepository.findAll(spec, pageable);
        List<UserAdminResponse> content = page.getContent().stream().map(this::toResponse).toList();
        return new PageResponse<>(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    @Transactional
    public UserAdminResponse create(UserAdminRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessRuleException("El email ya está registrado");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new BusinessRuleException("La contraseña es obligatoria");
        }
        Role role = requireRole(request.role());

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(role);
        user.setPhone(request.phone());
        user.setCreatedAt(Instant.now());
        if (request.teacherId() != null) {
            user.setTeacher(resolveTeacher(request.teacherId()));
        }
        user = userRepository.save(user);
        return toResponse(user);
    }

    @Transactional
    public UserAdminResponse update(Long id, UserAdminRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new BusinessRuleException("El email ya está registrado");
        }
        Role role = requireRole(request.role());

        user.setName(request.name().trim());
        user.setEmail(email);
        user.setRole(role);
        user.setPhone(request.phone());
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        if (request.teacherId() != null) {
            user.setTeacher(resolveTeacher(request.teacherId()));
        } else {
            user.setTeacher(null);
        }
        return toResponse(user);
    }

    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        userRepository.delete(user);
    }

    private User resolveTeacher(Long teacherId) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new BusinessRuleException("El profesor indicado no existe"));
        if (teacher.getRole().getName() != RoleName.TEACHER) {
            throw new BusinessRuleException("teacherId debe referenciar a un TEACHER");
        }
        return teacher;
    }

    private Role requireRole(RoleName roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Rol " + roleName + " no encontrado en la base de datos"));
    }

    private RoleName parseRole(String role) {
        try {
            return RoleName.valueOf(role.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Rol inválido: " + role);
        }
    }

    private UserAdminResponse toResponse(User user) {
        User teacher = user.getTeacher();
        return new UserAdminResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().getName().name(),
                teacher == null ? null : teacher.getId(),
                teacher == null ? null : teacher.getName(),
                user.getPhone(),
                user.getCreatedAt());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
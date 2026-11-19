package com.englishlab.repository;

import com.englishlab.model.entity.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    /**
     * Recarga el usuario con su rol en la misma consulta: el filtro JWT resuelve el rol
     * desde la BD fuera de una transacción (open-in-view=false), así que no puede ser lazy.
     */
    @Override
    @EntityGraph(attributePaths = "role")
    Optional<User> findById(Long id);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    /**
     * Lista con filtros dinámicos (Specification) y fetch de las asociaciones to-one
     * {@code role} y {@code teacher} en una sola consulta (sin N+1 al resolver teacherName).
     */
    @Override
    @EntityGraph(attributePaths = {"role", "teacher"})
    Page<User> findAll(Specification<User> spec, Pageable pageable);
}
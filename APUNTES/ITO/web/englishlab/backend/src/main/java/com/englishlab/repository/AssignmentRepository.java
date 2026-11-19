package com.englishlab.repository;

import com.englishlab.model.entity.Assignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    @EntityGraph(attributePaths = "activity")
    Page<Assignment> findByStudentId(Long studentId, Pageable pageable);
}
package com.englishlab.repository;

import com.englishlab.model.entity.ObjectivePrerequisite;
import com.englishlab.model.entity.ObjectivePrerequisiteId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ObjectivePrerequisiteRepository
        extends JpaRepository<ObjectivePrerequisite, ObjectivePrerequisiteId> {
}
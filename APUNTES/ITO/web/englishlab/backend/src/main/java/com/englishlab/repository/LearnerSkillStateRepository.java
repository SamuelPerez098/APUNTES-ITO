package com.englishlab.repository;

import com.englishlab.model.entity.LearnerSkillState;
import com.englishlab.model.entity.LearnerSkillStateId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LearnerSkillStateRepository extends JpaRepository<LearnerSkillState, LearnerSkillStateId> {

    List<LearnerSkillState> findByUserId(Long userId);

    Optional<LearnerSkillState> findByUserIdAndObjectiveId(Long userId, Long objectiveId);
}
package com.englishlab.model.entity;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * PK compuesta de {@code learner_skill_state} (V3): user_id + objective_id.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class LearnerSkillStateId implements Serializable {

    private Long userId;

    private Long objectiveId;
}
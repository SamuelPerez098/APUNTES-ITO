package com.englishlab.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * PK compuesta de {@code objective_prerequisites} (V2): objective_id + prerequisite_id.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class ObjectivePrerequisiteId implements Serializable {

    @Column(name = "objective_id")
    private Long objectiveId;

    @Column(name = "prerequisite_id")
    private Long prerequisiteId;
}
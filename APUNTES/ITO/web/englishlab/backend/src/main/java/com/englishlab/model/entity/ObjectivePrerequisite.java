package com.englishlab.model.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Relación N:M de objectives (V2: objective_id, prerequisite_id; PK compuesta).
 * Un objetivo puede tener varios prerrequisitos; aquí se guarda cada arista del grafo.
 */
@Entity
@Table(name = "objective_prerequisites")
@Getter
@Setter
@NoArgsConstructor
public class ObjectivePrerequisite {

    @EmbeddedId
    private ObjectivePrerequisiteId id;

    @MapsId("objectiveId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "objective_id")
    private Objective objective;

    @MapsId("prerequisiteId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prerequisite_id")
    private Objective prerequisite;
}
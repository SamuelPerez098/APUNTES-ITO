package com.englishlab.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Objetivo de aprendizaje (V2: id, code, name, cefr_level, skill, description).
 */
@Entity
@Table(name = "objectives")
@Getter
@Setter
@NoArgsConstructor
public class Objective {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, length = 10)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "cefr_level", nullable = false, length = 2)
    private String cefrLevel;

    @Column(name = "skill", nullable = false, length = 20)
    private String skill;

    @Column(name = "description")
    private String description;

    /** Aristas de prerrequisitos salientes (este objetivo -> sus prerrequisitos). */
    @OneToMany(mappedBy = "objective", fetch = FetchType.LAZY)
    private List<ObjectivePrerequisite> prerequisiteLinks = new ArrayList<>();
}
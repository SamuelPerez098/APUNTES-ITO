package com.englishlab.domain;

import java.util.Set;

/**
 * Representación plana de un objetivo para el cálculo puro de estados y recomendación.
 */
public record ObjectiveDomain(
        Long id,
        String code,
        String name,
        String cefrLevel,
        String skill,
        double mastery,
        Set<Long> prerequisites
) {}
package com.englishlab.dto.objective;

public record ObjectiveResponse(
        Long id,
        String code,
        String name,
        String cefrLevel,
        String skill
) {}
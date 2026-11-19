package com.englishlab.dto.progress;

public record ProgressResponse(
        Long objectiveId,
        String code,
        String name,
        String cefrLevel,
        String skill,
        double mastery
) {}
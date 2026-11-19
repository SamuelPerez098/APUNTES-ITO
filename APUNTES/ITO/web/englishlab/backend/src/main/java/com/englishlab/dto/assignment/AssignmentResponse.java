package com.englishlab.dto.assignment;

import java.time.Instant;

public record AssignmentResponse(
        Long id,
        Long studentId,
        Long activityId,
        String status,
        Instant assignedAt,
        Instant completedAt
) {}
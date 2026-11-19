package com.englishlab.dto.teacher;

import java.time.Instant;

public record StudentResponse(Long id, String name, String email, Instant createdAt) {}
package com.englishlab.dto.activity;

import java.util.List;

/**
 * DTO que ve el estudiante. NUNCA expone la clave de respuesta
 * (ni `answer`, ni `accepted`, ni `explanation`).
 */
public record ActivityStudentDto(String prompt, List<ActivityOption> options) {}

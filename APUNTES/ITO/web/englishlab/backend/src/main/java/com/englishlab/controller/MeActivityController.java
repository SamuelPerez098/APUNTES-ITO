package com.englishlab.controller;

import com.englishlab.dto.activity.ActivityStudentDetailDto;
import com.englishlab.service.ActivityService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Actividades del estudiante autenticado. Vista pública de práctica: sin clave de respuesta.
 */
@RestController
@RequestMapping("/api/v1/me/activities")
public class MeActivityController {

    private final ActivityService activityService;

    public MeActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ActivityStudentDetailDto> detail(@PathVariable Long id) {
        return ResponseEntity.ok(activityService.studentActivity(id));
    }
}

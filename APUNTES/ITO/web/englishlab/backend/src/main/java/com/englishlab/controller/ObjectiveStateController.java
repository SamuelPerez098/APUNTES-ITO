package com.englishlab.controller;

import com.englishlab.dto.activity.ActivityStudentSummaryDto;
import com.englishlab.dto.common.PageResponse;
import com.englishlab.dto.objective.ObjectiveStateResponse;
import com.englishlab.dto.objective.PracticeSessionResponse;
import com.englishlab.security.AuthenticatedUser;
import com.englishlab.service.ObjectiveStateService;
import com.englishlab.util.PageableFactory;
import java.util.List;
import java.util.Set;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/objectives")
public class ObjectiveStateController {

    private static final Set<String> SORT_WHITELIST = Set.of("id", "title", "type", "level");

    private final ObjectiveStateService objectiveStateService;

    public ObjectiveStateController(ObjectiveStateService objectiveStateService) {
        this.objectiveStateService = objectiveStateService;
    }

    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<ObjectiveStateResponse>> list(
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(objectiveStateService.myObjectives(principal.id()));
    }

    /** Práctica libre de un objetivo desbloqueado: resumen paginado, sin clave de respuesta. */
    @GetMapping("/{objectiveId}/activities")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<PageResponse<ActivityStudentSummaryDto>> activities(
            @PathVariable Long objectiveId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(objectiveStateService.objectiveActivities(
                principal.id(), objectiveId,
                PageableFactory.of(page, size, sort, SORT_WHITELIST, "title")));
    }

    /**
     * Sesión de práctica sin repetición: el SERVIDOR elige las actividades (nunca intentadas
     * primero, luego las más antiguas). Solo ids; el detalle llega con {@code GET /me/activities/{id}}.
     */
    @GetMapping("/{objectiveId}/practice-session")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<PracticeSessionResponse> practiceSession(
            @PathVariable Long objectiveId,
            @RequestParam(required = false) Integer size,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(
                objectiveStateService.practiceSession(principal.id(), objectiveId, size));
    }
}

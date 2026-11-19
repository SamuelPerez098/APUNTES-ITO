package com.englishlab.controller;

import com.englishlab.dto.common.PageResponse;
import com.englishlab.dto.objective.ObjectiveResponse;
import com.englishlab.service.ObjectiveStateService;
import com.englishlab.util.PageableFactory;
import java.util.Set;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/content/objectives")
@PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
public class ObjectiveController {

    private static final Set<String> SORT_WHITELIST = Set.of("id", "code", "name", "cefrLevel", "skill");

    private final ObjectiveStateService objectiveStateService;

    public ObjectiveController(ObjectiveStateService objectiveStateService) {
        this.objectiveStateService = objectiveStateService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<ObjectiveResponse>> list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        return ResponseEntity.ok(objectiveStateService.listObjectives(
                PageableFactory.of(page, size, sort, SORT_WHITELIST, "id")));
    }
}
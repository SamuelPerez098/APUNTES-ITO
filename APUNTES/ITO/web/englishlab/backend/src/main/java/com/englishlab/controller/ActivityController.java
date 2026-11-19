package com.englishlab.controller;

import com.englishlab.dto.activity.ActivityAdminResponse;
import com.englishlab.dto.activity.ActivityRequest;
import com.englishlab.dto.common.PageResponse;
import com.englishlab.service.ActivityService;
import com.englishlab.util.PageableFactory;
import jakarta.validation.Valid;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/content/activities")
@PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
public class ActivityController {

    private static final Set<String> SORT_WHITELIST = Set.of("id", "title", "level", "skill", "type");

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<ActivityAdminResponse>> list(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String text,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        return ResponseEntity.ok(activityService.list(level, skill, type, categoryId, text,
                PageableFactory.of(page, size, sort, SORT_WHITELIST, "id")));
    }

    @PostMapping
    public ResponseEntity<ActivityAdminResponse> create(@Valid @RequestBody ActivityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(activityService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActivityAdminResponse> update(@PathVariable Long id, @Valid @RequestBody ActivityRequest request) {
        return ResponseEntity.ok(activityService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        activityService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
package com.englishlab.controller;

import com.englishlab.dto.assignment.AssignmentCreateRequest;
import com.englishlab.dto.assignment.AssignmentResponse;
import com.englishlab.dto.assignment.StudentAssignmentResponse;
import com.englishlab.dto.common.PageResponse;
import com.englishlab.dto.progress.ProgressResponse;
import com.englishlab.security.AuthenticatedUser;
import com.englishlab.service.AssignmentService;
import com.englishlab.util.PageableFactory;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class AssignmentController {

    private static final Set<String> SORT_WHITELIST = Set.of("id", "status", "assignedAt");

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping("/teacher/assignments")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<AssignmentResponse> create(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody AssignmentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assignmentService.create(principal.id(), request));
    }

    @GetMapping("/me/assignments")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<PageResponse<StudentAssignmentResponse>> myAssignments(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        return ResponseEntity.ok(assignmentService.myAssignments(principal.id(),
                PageableFactory.of(page, size, sort, SORT_WHITELIST, "id")));
    }

    @GetMapping("/teacher/students/{id}/progress")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<ProgressResponse>> progress(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.progress(principal.id(), id));
    }
}
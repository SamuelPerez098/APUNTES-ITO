package com.englishlab.controller;

import com.englishlab.dto.common.PageResponse;
import com.englishlab.dto.teacher.AssignStudentRequest;
import com.englishlab.dto.teacher.StudentResponse;
import com.englishlab.security.AuthenticatedUser;
import com.englishlab.service.TeacherStudentService;
import com.englishlab.util.PageableFactory;
import jakarta.validation.Valid;
import java.util.Set;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/teacher/students")
public class TeacherStudentController {

    private static final Set<String> SORT_WHITELIST = Set.of("id", "name", "email", "createdAt");

    private final TeacherStudentService teacherStudentService;

    public TeacherStudentController(TeacherStudentService teacherStudentService) {
        this.teacherStudentService = teacherStudentService;
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<StudentResponse> assign(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody AssignStudentRequest request) {
        return ResponseEntity.ok(teacherStudentService.assign(principal.id(), request.email()));
    }

    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<PageResponse<StudentResponse>> list(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam(required = false) String text,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        return ResponseEntity.ok(teacherStudentService.list(principal.id(), text,
                PageableFactory.of(page, size, sort, SORT_WHITELIST, "id")));
    }
}
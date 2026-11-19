package com.englishlab.controller;

import com.englishlab.dto.attempt.AttemptRequest;
import com.englishlab.dto.attempt.AttemptResponse;
import com.englishlab.security.AuthenticatedUser;
import com.englishlab.service.AttemptService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/attempts")
public class AttemptController {

    private final AttemptService attemptService;

    public AttemptController(AttemptService attemptService) {
        this.attemptService = attemptService;
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<AttemptResponse> submit(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody AttemptRequest request) {
        return ResponseEntity.ok(attemptService.submit(principal.id(), request));
    }
}
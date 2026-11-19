package com.englishlab.controller;

import com.englishlab.dto.recommendation.RecommendationResponse;
import com.englishlab.security.AuthenticatedUser;
import com.englishlab.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/next")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<RecommendationResponse> next(
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(recommendationService.next(principal.id()));
    }
}
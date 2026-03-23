package com.kry.triage.controller;

import com.kry.triage.model.AssessmentRequest;
import com.kry.triage.model.AssessmentResponse;
import com.kry.triage.service.RecommendationService;
import com.kry.triage.service.SlotService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/assessment")
public class AssessmentController {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final RecommendationService recommendationService;
    private final SlotService slotService;

    public AssessmentController(RecommendationService recommendationService, SlotService slotService) {
        this.recommendationService = recommendationService;
        this.slotService = slotService;
    }

    @PostMapping
    public AssessmentResponse assess(@Valid @RequestBody AssessmentRequest request) {
        String recommendation = recommendationService.recommend(request.score());
        List<String> slots = slotService.getAvailableSlots(LocalDateTime.now())
                .stream()
                .map(dt -> dt.format(FORMATTER))
                .toList();
        return new AssessmentResponse(recommendation, slots);
    }
}

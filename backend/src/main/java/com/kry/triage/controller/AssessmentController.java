package com.kry.triage.controller;

import com.kry.triage.model.AssessmentRequest;
import com.kry.triage.model.AssessmentResponse;
import com.kry.triage.model.Patient;
import com.kry.triage.repository.PatientRepository;
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
    private final PatientRepository patientRepository;

    public AssessmentController(RecommendationService recommendationService,
                                SlotService slotService,
                                PatientRepository patientRepository) {
        this.recommendationService = recommendationService;
        this.slotService = slotService;
        this.patientRepository = patientRepository;
    }

    /**
     * TODO: Use authenticated user id instead of hardcoded user lookup
     */
    @PostMapping
    public AssessmentResponse assess(@Valid @RequestBody AssessmentRequest request) {
        String recommendation = recommendationService.recommend(request.score());
        Patient patient = patientRepository.findByUsername("patient").orElseThrow();
        List<String> slots = slotService.getAvailableSlots(LocalDateTime.now(), patient, recommendation)
                .stream()
                .map(dt -> dt.format(FORMATTER))
                .toList();
        return new AssessmentResponse(recommendation, slots);
    }
}

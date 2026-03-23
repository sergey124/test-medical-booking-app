package com.kry.triage.model;

import java.util.List;

public record AssessmentResponse(
        String recommendation,
        List<String> availableSlots
) {}

package com.kry.triage.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record AssessmentRequest(
        @Min(5) @Max(15) int score
) {}

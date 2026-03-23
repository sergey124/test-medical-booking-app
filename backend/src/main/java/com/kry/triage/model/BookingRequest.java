package com.kry.triage.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookingRequest(
        @NotNull String slot,
        @NotBlank String recommendation
) {}

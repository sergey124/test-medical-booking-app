package com.kry.triage.model;

public record BookingResponse(
        String confirmationId,
        String slot,
        String recommendation,
        String specialistName,
        String specialistType
) {}

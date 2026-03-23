package com.kry.triage.controller;

import com.kry.triage.model.Booking;
import com.kry.triage.model.BookingRequest;
import com.kry.triage.model.BookingResponse;
import com.kry.triage.model.Patient;
import com.kry.triage.model.Specialist;
import com.kry.triage.repository.BookingRepository;
import com.kry.triage.repository.PatientRepository;
import com.kry.triage.service.SlotService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/booking")
public class BookingController {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final BookingRepository bookingRepository;
    private final PatientRepository patientRepository;
    private final SlotService slotService;

    public BookingController(BookingRepository bookingRepository,
                             PatientRepository patientRepository,
                             SlotService slotService) {
        this.bookingRepository = bookingRepository;
        this.patientRepository = patientRepository;
        this.slotService = slotService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse book(@Valid @RequestBody BookingRequest request) {
        LocalDateTime slotTime;
        try {
            slotTime = LocalDateTime.parse(request.slot(), FORMATTER);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid slot format. Use ISO-8601 (e.g. 2026-03-22T09:00:00)");
        }

        if (slotTime.isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slot is in the past");
        }

        Specialist specialist = slotService
                .findAvailableSpecialist(slotTime, request.recommendation())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "No available specialist for this slot and recommendation type"));

        // Resolve the current patient — hardcoded to the mock user for now.
        // When real auth is added, replace "patient" with the authenticated username.
        Patient patient = patientRepository.findByUsername("patient")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Patient record not found"));

        Booking saved = bookingRepository.save(
                new Booking(slotTime, request.recommendation(), specialist, patient));

        return new BookingResponse(
                saved.getId().toString(),
                saved.getSlot().format(FORMATTER),
                saved.getRecommendation(),
                specialist.getName(),
                specialist.getType().name()
        );
    }
}

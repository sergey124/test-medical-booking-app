package com.kry.triage.service;

import com.kry.triage.model.Booking;
import com.kry.triage.model.Patient;
import com.kry.triage.model.Specialist;
import com.kry.triage.model.SpecialistType;
import com.kry.triage.repository.BookingRepository;
import com.kry.triage.repository.SpecialistRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Calculates available 15-minute appointment slots based on specialist schedules.
 *
 * Scheduling rules (per specialist):
 *  - Each specialist has a fixed shift start time
 *  - Works for 4 hours, takes a 1-hour break, then works 4 more hours (total: 8h work + 1h break)
 *  - Break starts exactly 4 hours after shift start
 *  - Clinic operating window: 08:00–18:00
 *
 * Recommendation → eligible specialist types:
 *  - "Doctor"        → DOCTOR only
 *  - "Nurse" / "Chat" → NURSE or DOCTOR
 *
 */
@Service
public class SlotService {

    private static final LocalTime CLINIC_OPEN  = LocalTime.of(8, 0);
    private static final LocalTime CLINIC_CLOSE = LocalTime.of(18, 0);
    private static final int WORK_BEFORE_BREAK_HOURS = 4;
    private static final int BREAK_DURATION_HOURS    = 1;
    private static final int MAX_WORK_HOURS          = 8;
    private static final int DAYS_AHEAD              = 3;

    private final SpecialistRepository specialistRepository;
    private final BookingRepository bookingRepository;

    public SlotService(SpecialistRepository specialistRepository, BookingRepository bookingRepository) {
        this.specialistRepository = specialistRepository;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Returns all available slots within [now, now+DAYS_AHEAD days] where:
     * 1. The patient has no existing booking at that time.
     * 2. At least one specialist eligible for the recommendation is free.
     *
     * Bookings and specialists are each loaded from the DB exactly once.
     */
    public List<LocalDateTime> getAvailableSlots(LocalDateTime now, Patient patient, String recommendation) {
        List<Booking>    allBookings  = bookingRepository.findAll();
        List<Specialist> specialists  = specialistRepository.findAll();

        Set<LocalDateTime> patientBooked = allBookings.stream()
                .filter(b -> b.getPatient().getId().equals(patient.getId()))
                .map(Booking::getSlot)
                .collect(Collectors.toSet());

        Map<LocalDateTime, Set<UUID>> bookedBySlot = allBookings.stream()
                .collect(Collectors.groupingBy(
                        Booking::getSlot,
                        Collectors.mapping(b -> b.getSpecialist().getId(), Collectors.toSet())));

        List<LocalDateTime> slots = new ArrayList<>();
        LocalDate today = now.toLocalDate();

        for (int dayOffset = 0; dayOffset <= DAYS_AHEAD; dayOffset++) {
            LocalDate date = today.plusDays(dayOffset);
            LocalDateTime cursor = date.atTime(CLINIC_OPEN);
            LocalDateTime dayEnd  = date.atTime(CLINIC_CLOSE);

            while (cursor.isBefore(dayEnd)) {
                final LocalDateTime slot = cursor;
                if (!slot.isBefore(now) && !patientBooked.contains(slot)) {
                    Set<UUID> bookedIds = bookedBySlot.getOrDefault(slot, Collections.emptySet());
                    boolean anyFree = specialists.stream()
                            .filter(s -> isEligible(s, recommendation))
                            .filter(s -> isWorking(s, slot.toLocalTime()))
                            .anyMatch(s -> !bookedIds.contains(s.getId()));
                    if (anyFree) slots.add(slot);
                }
                cursor = cursor.plusMinutes(15);
            }
        }
        return slots;
    }

    /**
     * Finds the first specialist who is working at the given slot, has no booking
     * at that datetime, and is eligible for the given recommendation type.
     * Returns empty if none found.
     */
    public Optional<Specialist> findAvailableSpecialist(LocalDateTime slot, String recommendation) {
        List<Specialist> specialists = specialistRepository.findAll();
        Set<UUID> bookedSpecialistIds = bookingRepository.findAll().stream()
                .filter(b -> b.getSlot().equals(slot))
                .map(b -> b.getSpecialist().getId())
                .collect(Collectors.toSet());

        return specialists.stream()
                .filter(s -> isEligible(s, recommendation))
                .filter(s -> isWorking(s, slot.toLocalTime()))
                .filter(s -> !bookedSpecialistIds.contains(s.getId()))
                .findFirst();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Whether the specialist is actively working at the given time of day:
     * within their shift hours, not on their break, and within clinic hours.
     */
    boolean isWorking(Specialist specialist, LocalTime t) {
        LocalTime start     = specialist.getShiftStart();
        LocalTime breakStart = start.plusHours(WORK_BEFORE_BREAK_HOURS);
        LocalTime breakEnd   = breakStart.plusHours(BREAK_DURATION_HOURS);
        LocalTime shiftEnd   = start.plusHours(MAX_WORK_HOURS + BREAK_DURATION_HOURS);

        boolean withinShift  = !t.isBefore(start) && t.isBefore(shiftEnd);
        boolean onBreak      = !t.isBefore(breakStart) && t.isBefore(breakEnd);
        boolean withinClinic = !t.isBefore(CLINIC_OPEN) && t.isBefore(CLINIC_CLOSE);

        return withinShift && !onBreak && withinClinic;
    }

    /**
     * Whether a specialist's type is compatible with the requested recommendation.
     */
    boolean isEligible(Specialist specialist, String recommendation) {
        if ("Doctor".equalsIgnoreCase(recommendation)) {
            return specialist.getType() == SpecialistType.DOCTOR;
        }
        // Nurse and Chat can be handled by both nurses and doctors
        return true;
    }

}

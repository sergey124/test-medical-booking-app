package com.kry.triage.service;

import com.kry.triage.model.Booking;
import com.kry.triage.model.Specialist;
import com.kry.triage.model.SpecialistType;
import com.kry.triage.repository.BookingRepository;
import com.kry.triage.repository.SpecialistRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Calculates available 15-minute appointment slots based on specialist schedules.
 *
 * Scheduling rules (per specialist):
 *  - Each specialist has a fixed shift start time (08:00 or 09:00)
 *  - Works for 4 hours, takes a 1-hour break, then works 4 more hours (total: 8h work + 1h break)
 *  - Break starts exactly 4 hours after shift start
 *  - Clinic operating window: 08:00–18:00
 *
 * Recommendation → eligible specialist types:
 *  - "Doctor"        → DOCTOR only
 *  - "Nurse" / "Chat" → NURSE or DOCTOR
 *
 * Staffing (8 specialists, 2 shifts):
 *  - Shift A (08:00): 2 nurses + 2 doctors — break 12:00–13:00, shift ends 16:00
 *  - Shift B (09:00): 2 nurses + 2 doctors — break 13:00–14:00, shift ends 17:00
 */
@Service
public class SlotService {

    private static final LocalTime CLINIC_OPEN  = LocalTime.of(8, 0);
    private static final LocalTime CLINIC_CLOSE = LocalTime.of(18, 0);
    private static final int WORK_BEFORE_BREAK_HOURS = 4;
    private static final int BREAK_DURATION_HOURS    = 1;
    private static final int MAX_WORK_HOURS          = 8;

    private final SpecialistRepository specialistRepository;
    private final BookingRepository bookingRepository;

    public SlotService(SpecialistRepository specialistRepository, BookingRepository bookingRepository) {
        this.specialistRepository = specialistRepository;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Returns all available slots within [now, now+3 days] where at least one
     * specialist (of any eligible type) is available.
     */
    public List<LocalDateTime> getAvailableSlots(LocalDateTime now) {
        List<Specialist> specialists = specialistRepository.findAll();
        Map<LocalDateTime, Long> bookingCounts = buildBookingCountMap();

        List<LocalDateTime> slots = new ArrayList<>();
        LocalDate today = now.toLocalDate();

        for (int dayOffset = 0; dayOffset <= 3; dayOffset++) {
            LocalDate date = today.plusDays(dayOffset);
            LocalDateTime cursor = date.atTime(CLINIC_OPEN);
            LocalDateTime dayEnd  = date.atTime(CLINIC_CLOSE);

            while (cursor.isBefore(dayEnd)) {
                if (!cursor.isBefore(now) && hasAvailableSpecialist(cursor, specialists, bookingCounts, null)) {
                    slots.add(cursor);
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
        LocalTime shiftEnd   = start.plusHours(MAX_WORK_HOURS);

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

    private boolean hasAvailableSpecialist(
            LocalDateTime slot,
            List<Specialist> specialists,
            Map<LocalDateTime, Long> bookingCounts,
            String recommendation) {

        Set<UUID> bookedAtSlot = bookingRepository.findAll().stream()
                .filter(b -> b.getSlot().equals(slot))
                .map(b -> b.getSpecialist().getId())
                .collect(Collectors.toSet());

        return specialists.stream()
                .filter(s -> recommendation == null || isEligible(s, recommendation))
                .filter(s -> isWorking(s, slot.toLocalTime()))
                .anyMatch(s -> !bookedAtSlot.contains(s.getId()));
    }

    private Map<LocalDateTime, Long> buildBookingCountMap() {
        return bookingRepository.findAll().stream()
                .collect(Collectors.groupingBy(Booking::getSlot, Collectors.counting()));
    }
}

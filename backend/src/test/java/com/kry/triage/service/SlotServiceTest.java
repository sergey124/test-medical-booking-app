package com.kry.triage.service;

import com.kry.triage.model.Booking;
import com.kry.triage.model.Patient;
import com.kry.triage.model.Specialist;
import com.kry.triage.model.SpecialistType;
import com.kry.triage.repository.BookingRepository;
import com.kry.triage.repository.SpecialistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlotServiceTest {

    @Mock private SpecialistRepository specialistRepository;
    @Mock private BookingRepository bookingRepository;

    private SlotService slotService;

    private static final LocalTime SHIFT_A = LocalTime.of(8, 0);
    private static final LocalTime SHIFT_B = LocalTime.of(9, 0);

    private final Patient PATIENT = new Patient("patient");

    private final List<Specialist> ALL_SPECIALISTS = List.of(
            new Specialist("Nurse Anna",  SpecialistType.NURSE,  SHIFT_A),
            new Specialist("Nurse Ben",   SpecialistType.NURSE,  SHIFT_A),
            new Specialist("Dr. Carter",  SpecialistType.DOCTOR, SHIFT_A),
            new Specialist("Dr. Davis",   SpecialistType.DOCTOR, SHIFT_A),
            new Specialist("Nurse Elena", SpecialistType.NURSE,  SHIFT_B),
            new Specialist("Nurse Frank", SpecialistType.NURSE,  SHIFT_B),
            new Specialist("Dr. Garcia",  SpecialistType.DOCTOR, SHIFT_B),
            new Specialist("Dr. Harris",  SpecialistType.DOCTOR, SHIFT_B)
    );

    @BeforeEach
    void setUp() {
        when(specialistRepository.findAll()).thenReturn(ALL_SPECIALISTS);
        when(bookingRepository.findAll()).thenReturn(List.of());
        slotService = new SlotService(specialistRepository, bookingRepository);
    }

    // ── getAvailableSlots ────────────────────────────────────────────────────

    @Test
    void slotsAreWithinClinicHours() {
        // when
        List<LocalDateTime> slots = slotService.getAvailableSlots(
                LocalDate.now().atTime(8, 0), PATIENT, "Nurse");

        // then
        assertThat(slots).isNotEmpty();
        assertThat(slots).allSatisfy(slot -> {
            assertThat(slot.toLocalTime()).isAfterOrEqualTo(LocalTime.of(8, 0));
            assertThat(slot.toLocalTime()).isBefore(LocalTime.of(18, 0));
        });
    }

    @Test
    void noSlotsInThePast() {
        // given
        LocalDateTime now = LocalDateTime.now();

        // when
        List<LocalDateTime> slots = slotService.getAvailableSlots(now, PATIENT, "Nurse");

        // then
        assertThat(slots).allSatisfy(slot -> assertThat(slot).isAfterOrEqualTo(now));
    }

    @Test
    void slotsAre15MinuteIntervals() {
        // when
        List<LocalDateTime> slots = slotService.getAvailableSlots(
                LocalDate.now().atTime(8, 0), PATIENT, "Nurse");

        // then
        assertThat(slots).isNotEmpty();
        assertThat(slots).allSatisfy(slot -> assertThat(slot.getMinute() % 15).isEqualTo(0));
    }

    @Test
    void slotsSpanUpToThreeDays() {
        // given
        LocalDateTime now = LocalDate.now().atTime(8, 0);

        // when
        List<LocalDateTime> slots = slotService.getAvailableSlots(now, PATIENT, "Nurse");

        // then
        assertThat(slots).allSatisfy(slot ->
                assertThat(slot.toLocalDate()).isBeforeOrEqualTo(now.toLocalDate().plusDays(3)));
    }

    @Test
    void slotIsHiddenWhenPatientAlreadyHasBookingAtThatTime() {
        // given
        LocalDateTime bookedSlot = LocalDate.now().plusDays(1).atTime(10, 0);
        when(bookingRepository.findAll()).thenReturn(List.of(
                new Booking(bookedSlot, "Nurse", ALL_SPECIALISTS.get(0), PATIENT)));
        slotService = new SlotService(specialistRepository, bookingRepository);

        // when
        List<LocalDateTime> slots = slotService.getAvailableSlots(
                LocalDate.now().atTime(8, 0), PATIENT, "Nurse");

        // then
        assertThat(slots).doesNotContain(bookedSlot);
    }

    @Test
    void slotIsHiddenWhenAllEligibleSpecialistsAreBooked() {
        // given: all 4 doctors booked at 10:00 by another patient
        LocalDateTime fullSlot = LocalDate.now().plusDays(1).atTime(10, 0);
        Patient otherPatient = new Patient("other");
        List<Booking> allDoctorsBooked = ALL_SPECIALISTS.stream()
                .filter(s -> s.getType() == SpecialistType.DOCTOR)
                .map(s -> new Booking(fullSlot, "Doctor", s, otherPatient))
                .toList();
        when(bookingRepository.findAll()).thenReturn(allDoctorsBooked);
        slotService = new SlotService(specialistRepository, bookingRepository);

        // when
        List<LocalDateTime> doctorSlots = slotService.getAvailableSlots(
                LocalDate.now().atTime(8, 0), PATIENT, "Doctor");
        List<LocalDateTime> nurseSlots = slotService.getAvailableSlots(
                LocalDate.now().atTime(8, 0), PATIENT, "Nurse");

        // then: slot hidden for Doctor (all doctors taken), still visible for Nurse (nurses free)
        assertThat(doctorSlots).doesNotContain(fullSlot);
        assertThat(nurseSlots).contains(fullSlot);
    }

    @Test
    void bookingsAndSpecialistsAreEachLoadedOncePerCall() {
        // when
        slotService.getAvailableSlots(LocalDate.now().atTime(8, 0), PATIENT, "Nurse");

        // then
        verify(bookingRepository, times(1)).findAll();
        verify(specialistRepository, times(1)).findAll();
    }

    // ── findAvailableSpecialist ──────────────────────────────────────────────

    @Test
    void findsAnotherSpecialistWhenOneIsAlreadyBooked() {
        // given
        LocalDateTime slot = LocalDate.now().plusDays(1).atTime(10, 0);
        Specialist bookedSpec = ALL_SPECIALISTS.get(0);
        when(bookingRepository.findAll()).thenReturn(List.of(
                new Booking(slot, "Nurse", bookedSpec, PATIENT)));
        slotService = new SlotService(specialistRepository, bookingRepository);

        // when
        Optional<Specialist> found = slotService.findAvailableSpecialist(slot, "Nurse");

        // then
        assertThat(found).isPresent();
        assertThat(found.get()).isNotSameAs(bookedSpec);
    }

    @Test
    void doctorRecommendationOnlyAssignsDoctors() {
        // given
        LocalDateTime slot = LocalDate.now().plusDays(1).atTime(10, 0);

        // when
        Optional<Specialist> found = slotService.findAvailableSpecialist(slot, "Doctor");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getType()).isEqualTo(SpecialistType.DOCTOR);
    }

    @Test
    void nurseRecommendationCanAssignDoctor() {
        // given: all nurses booked, doctors still free
        LocalDateTime slot = LocalDate.now().plusDays(1).atTime(10, 0);
        List<Booking> allNursesBooked = ALL_SPECIALISTS.stream()
                .filter(s -> s.getType() == SpecialistType.NURSE)
                .map(s -> new Booking(slot, "Nurse", s, new Patient("other")))
                .toList();
        when(bookingRepository.findAll()).thenReturn(allNursesBooked);
        slotService = new SlotService(specialistRepository, bookingRepository);

        // when
        Optional<Specialist> found = slotService.findAvailableSpecialist(slot, "Nurse");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getType()).isEqualTo(SpecialistType.DOCTOR);
    }
}

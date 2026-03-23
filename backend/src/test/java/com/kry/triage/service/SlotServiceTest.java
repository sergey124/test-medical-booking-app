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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SlotServiceTest {

    @Mock private SpecialistRepository specialistRepository;
    @Mock private BookingRepository bookingRepository;

    private SlotService slotService;

    // 8 specialists mirroring DataInitializer
    private static final LocalTime SHIFT_A = LocalTime.of(8, 0);
    private static final LocalTime SHIFT_B = LocalTime.of(9, 0);

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

    // ── isWorking ────────────────────────────────────────────────────────────

    @Test
    void shiftASpecialistIsWorkingAt08() {
        Specialist nurse = new Specialist("Nurse Anna", SpecialistType.NURSE, SHIFT_A);
        assertThat(slotService.isWorking(nurse, LocalTime.of(8, 0))).isTrue();
    }

    @Test
    void shiftASpecialistIsOnBreakAt12() {
        Specialist nurse = new Specialist("Nurse Anna", SpecialistType.NURSE, SHIFT_A);
        assertThat(slotService.isWorking(nurse, LocalTime.of(12, 0))).isFalse();
    }

    @Test
    void shiftASpecialistIsWorkingAt13AfterBreak() {
        Specialist nurse = new Specialist("Nurse Anna", SpecialistType.NURSE, SHIFT_A);
        assertThat(slotService.isWorking(nurse, LocalTime.of(13, 0))).isTrue();
    }

    @Test
    void shiftASpecialistIsNotWorkingAt1700() {
        // Shift A ends at 08:00+8h = 16:00
        Specialist doc = new Specialist("Dr. Carter", SpecialistType.DOCTOR, SHIFT_A);
        assertThat(slotService.isWorking(doc, LocalTime.of(17, 0))).isFalse();
    }

    @Test
    void shiftBSpecialistIsNotWorkingAt0800() {
        // Shift B starts at 09:00
        Specialist nurse = new Specialist("Nurse Elena", SpecialistType.NURSE, SHIFT_B);
        assertThat(slotService.isWorking(nurse, LocalTime.of(8, 0))).isFalse();
    }

    @Test
    void shiftBSpecialistIsWorkingAt1745() {
        // Shift B ends at 09:00+8h = 17:00, so 17:45 is outside
        Specialist nurse = new Specialist("Nurse Elena", SpecialistType.NURSE, SHIFT_B);
        assertThat(slotService.isWorking(nurse, LocalTime.of(17, 45))).isFalse();
    }

    // ── Slot availability ────────────────────────────────────────────────────

    @Test
    void slotsAreWithinClinicHours() {
        LocalDateTime now = LocalDate.now().atTime(8, 0);
        List<LocalDateTime> slots = slotService.getAvailableSlots(now);

        assertThat(slots).isNotEmpty();
        assertThat(slots).allSatisfy(slot -> {
            assertThat(slot.toLocalTime()).isAfterOrEqualTo(LocalTime.of(8, 0));
            assertThat(slot.toLocalTime()).isBefore(LocalTime.of(18, 0));
        });
    }

    @Test
    void noSlotsInThePast() {
        LocalDateTime now = LocalDateTime.now();
        List<LocalDateTime> slots = slotService.getAvailableSlots(now);
        assertThat(slots).allSatisfy(slot -> assertThat(slot).isAfterOrEqualTo(now));
    }

    @Test
    void slotsAre15MinuteIntervals() {
        LocalDateTime now = LocalDate.now().atTime(8, 0);
        List<LocalDateTime> slots = slotService.getAvailableSlots(now);
        assertThat(slots).isNotEmpty();
        assertThat(slots).allSatisfy(slot -> assertThat(slot.getMinute() % 15).isEqualTo(0));
    }

    @Test
    void slotsSpanUpToThreeDays() {
        LocalDateTime now = LocalDate.now().atTime(8, 0);
        List<LocalDateTime> slots = slotService.getAvailableSlots(now);
        LocalDate maxDate = now.toLocalDate().plusDays(3);
        assertThat(slots).allSatisfy(slot ->
                assertThat(slot.toLocalDate()).isBeforeOrEqualTo(maxDate));
    }

    // ── Capacity ─────────────────────────────────────────────────────────────

    @Test
    void slotRemainsAvailableAfterOneBookingBecauseOtherSpecialistsAreFree() {
        LocalDateTime slot = LocalDate.now().plusDays(1).atTime(10, 0);
        Specialist bookedSpec = ALL_SPECIALISTS.get(0);
        Patient patient = new Patient("patient");
        Booking existing = new Booking(slot, "Nurse", bookedSpec, patient);

        when(bookingRepository.findAll()).thenReturn(List.of(existing));
        slotService = new SlotService(specialistRepository, bookingRepository);

        Optional<Specialist> found = slotService.findAvailableSpecialist(slot, "Nurse");
        assertThat(found).isPresent();
        assertThat(found.get()).isNotSameAs(bookedSpec);
    }

    @Test
    void doctorRecommendationOnlyAssignsDoctors() {
        LocalDateTime slot = LocalDate.now().plusDays(1).atTime(10, 0);
        Optional<Specialist> found = slotService.findAvailableSpecialist(slot, "Doctor");
        assertThat(found).isPresent();
        assertThat(found.get().getType()).isEqualTo(SpecialistType.DOCTOR);
    }

    @Test
    void chatRecommendationCanAssignNurse() {
        LocalDateTime slot = LocalDate.now().plusDays(1).atTime(10, 0);
        Optional<Specialist> found = slotService.findAvailableSpecialist(slot, "Chat");
        assertThat(found).isPresent(); // any type is eligible
    }
}

package com.kry.triage.config;

import com.kry.triage.model.Patient;
import com.kry.triage.model.Specialist;
import com.kry.triage.model.SpecialistType;
import com.kry.triage.repository.PatientRepository;
import com.kry.triage.repository.SpecialistRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;

/**
 * Seeds initial data on startup.
 *
 * Specialists — 8 total (4 nurses + 4 doctors) across two shifts:
 *   Shift A (08:00): break 12:00–13:00, works until 16:00
 *   Shift B (09:00): break 13:00–14:00, works until 17:00
 *
 * Two shifts ensure exactly 4 clinicians are available at every
 * 15-minute slot within the clinic window (08:00–18:00).
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final LocalTime SHIFT_A = LocalTime.of(8, 0);
    private static final LocalTime SHIFT_B = LocalTime.of(9, 0);

    private final SpecialistRepository specialistRepository;
    private final PatientRepository patientRepository;

    public DataInitializer(SpecialistRepository specialistRepository, PatientRepository patientRepository) {
        this.specialistRepository = specialistRepository;
        this.patientRepository = patientRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (specialistRepository.count() == 0) {
            specialistRepository.saveAll(List.of(
                    // Shift A — 08:00
                    new Specialist("Nurse Anna",    SpecialistType.NURSE,   SHIFT_A),
                    new Specialist("Nurse Ben",     SpecialistType.NURSE,   SHIFT_A),
                    new Specialist("Dr. Carter",    SpecialistType.DOCTOR,  SHIFT_A),
                    new Specialist("Dr. Davis",     SpecialistType.DOCTOR,  SHIFT_A),
                    // Shift B — 09:00
                    new Specialist("Nurse Elena",   SpecialistType.NURSE,   SHIFT_B),
                    new Specialist("Nurse Frank",   SpecialistType.NURSE,   SHIFT_B),
                    new Specialist("Dr. Garcia",    SpecialistType.DOCTOR,  SHIFT_B),
                    new Specialist("Dr. Harris",    SpecialistType.DOCTOR,  SHIFT_B)
            ));
        }

        if (patientRepository.count() == 0) {
            patientRepository.save(new Patient("patient"));
        }
    }
}

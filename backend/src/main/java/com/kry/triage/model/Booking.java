package com.kry.triage.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private LocalDateTime slot;

    @Column(nullable = false)
    private String recommendation;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "specialist_id", nullable = false)
    private Specialist specialist;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    protected Booking() {}

    public Booking(LocalDateTime slot, String recommendation, Specialist specialist, Patient patient) {
        this.slot = slot;
        this.recommendation = recommendation;
        this.specialist = specialist;
        this.patient = patient;
    }

    public UUID getId() { return id; }
    public LocalDateTime getSlot() { return slot; }
    public String getRecommendation() { return recommendation; }
    public Specialist getSpecialist() { return specialist; }
    public Patient getPatient() { return patient; }
}

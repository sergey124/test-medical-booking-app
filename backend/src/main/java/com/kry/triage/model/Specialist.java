package com.kry.triage.model;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "specialists")
public class Specialist {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpecialistType type;

    @Column(nullable = false)
    private LocalTime shiftStart;

    protected Specialist() {}

    public Specialist(String name, SpecialistType type, LocalTime shiftStart) {
        this.name = name;
        this.type = type;
        this.shiftStart = shiftStart;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public SpecialistType getType() { return type; }
    public LocalTime getShiftStart() { return shiftStart; }
}

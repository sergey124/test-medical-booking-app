package com.kry.triage.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "patients")
public class Patient {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false, unique = true)
    private String username;

    protected Patient() {}

    public Patient(String username) {
        this.username = username;
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
}

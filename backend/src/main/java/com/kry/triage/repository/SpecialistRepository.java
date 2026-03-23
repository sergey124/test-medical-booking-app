package com.kry.triage.repository;

import com.kry.triage.model.Specialist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpecialistRepository extends JpaRepository<Specialist, UUID> {}

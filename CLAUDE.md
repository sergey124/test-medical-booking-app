# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A fullstack medical triage application. Patients answer a questionnaire, receive a triage recommendation (Chat/Nurse/Doctor), and book a slot with an available specialist.

## Commands

### Backend (Spring Boot, Java 21, Maven)
```bash
cd backend
./mvnw spring-boot:run     # Start dev server on port 8080
./mvnw test                # Run all tests
./mvnw test -Dtest=SlotServiceTest   # Run a single test class
./mvnw clean package       # Build JAR
```

### Frontend (React + TypeScript + Vite)
```bash
cd frontend
npm run dev      # Start dev server on port 5173
npm run build    # TypeScript check + Vite build
npm run lint     # ESLint
npm run preview  # Preview production build
```

## Architecture

**Request flow:** Frontend (`:5173`) → HTTP POST → Backend (`:8080`) → H2 in-memory DB

Two API endpoints:
- `POST /assessment` — accepts triage score (5–15), returns recommendation + available slots
- `POST /booking` — accepts recommendation type + slot, returns specialist assignment

**Backend layers:**
- `controller/` — AssessmentController, BookingController (Spring MVC REST)
- `service/` — `RecommendationService` (score → recommendation), `SlotService` (generates available 15-min slots across specialists)
- `model/` — `Specialist`, `Patient`, `Booking`, `SpecialistType` enum (NURSE/DOCTOR)
- `repository/` — JPA repositories backed by H2 in-memory DB (`triagedb`)
- `config/DataInitializer` — seeds 8 specialists (4 nurses + 4 doctors, Shift A 08-16 / Shift B 09-17, each with a 1-hour lunch break) and 1 hardcoded patient on startup

**Frontend screens** (managed via `screen` state in `App.tsx`): Login → LandingPage → Questionnaire → Results → BookingConfirmation → back to LandingPage

**Recommendation → specialist type mapping:**
- "Chat" or "Nurse" → NURSE or DOCTOR (any)
- "Doctor" → DOCTOR only

**Slot generation rules:** 15-min intervals, clinic hours 08:00–18:00, spans today + 3 days, excludes each specialist's shift break, excludes already-booked slots.

## Known Bugs (from README)

1. A patient can book multiple appointments with the same specialist at the same time — no overbooking guard implemented.
2. Slot generation date range logic needs review (see commit `19a0377`).

## H2 Console

Available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:triagedb`, user: `sa`, no password). Schema is reset on every backend restart (`ddl-auto=create-drop`).

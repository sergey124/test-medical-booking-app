# Medical Triage & Booking Application

A fullstack medical triage application where patients answer a short questionnaire to determine the level of care they need and are then presented with available appointment slots to book.

---

## Tech Stack

### Backend — Java 21 + Spring Boot 3 + Spring Data JPA + H2

- **Java 21** — latest LTS, supports records and modern language features
- **Spring Boot 3** — battle-tested framework with minimal boilerplate for REST APIs
- **Spring Data JPA + H2** — JPA repository pattern with an in-memory database for development; easy to swap to Postgres or MySQL via a different Spring profile
- **Maven** — standard build tool, reproducible builds via the wrapper (`./mvnw`)
- **Spring Validation** — declarative request validation with `@Valid`

### Frontend — React 18 + TypeScript + Vite + Tailwind CSS v4

- **React 18** — industry-standard UI library
- **TypeScript** — type safety across the entire frontend
- **Vite** — fast dev server and build tool
- **Tailwind CSS v4** — utility-first CSS, mobile-first responsive design without custom stylesheets

---

## Running the Application

### Prerequisites

Install [SDKMAN](https://sdkman.io/) to manage Java/Maven (or install Java 21 + Maven manually):

```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 21.0.6-tem
sdk install maven
```

Install Node.js 22 via [fnm](https://github.com/Schniz/fnm) or your preferred version manager:

```bash
curl -fsSL https://fnm.vercel.app/install | bash
source ~/.bashrc
fnm install 22 && fnm use 22
```

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

The API starts on **http://localhost:8080**.

H2 console available at **http://localhost:8080/h2-console**
(JDBC URL: `jdbc:h2:mem:triagedb`, username: `sa`, no password)

### Frontend

```bash
cd frontend
npm install
npm run dev
```

The app starts on **http://localhost:5173**.

---

## Running Tests

### Backend tests

```bash
cd backend
./mvnw test
```

Tests cover `SlotService`:
- Slots are within clinic hours (08:00–18:00)
- No past slots returned
- All slots are 15-minute intervals
- Slots span up to 3 calendar days
- Shift A/B specialist working hours and break enforcement
- Slot remains available after one booking (other specialists still free)
- Doctor recommendation only assigns DOCTOR-type specialists
- Chat recommendation accepts any specialist type

---

## API Reference

### `POST /assessment`

Submit triage score and receive a recommendation + available booking slots.

**Request:**
```json
{ "score": 11 }
```

**Response:**
```json
{
  "recommendation": "Nurse",
  "availableSlots": ["2026-03-22T09:00:00", "2026-03-22T09:15:00"]
}
```

Score → Recommendation mapping:
| Score | Recommendation |
|-------|----------------|
| 5–7   | Chat           |
| 8–11  | Nurse          |
| 12–15 | Doctor         |

### `POST /booking`

Book a selected slot. The backend automatically assigns the first available specialist matching the recommendation type.

**Request:**
```json
{ "slot": "2026-03-22T09:15:00", "recommendation": "Nurse" }
```

**Response:**
```json
{
  "confirmationId": "550e8400-e29b-41d4-a716-446655440000",
  "slot": "2026-03-22T09:15:00",
  "recommendation": "Nurse",
  "specialistName": "Nurse Anna",
  "specialistType": "NURSE"
}
```

Recommendation → eligible specialist types:
| Recommendation | Eligible types     |
|----------------|--------------------|
| Chat           | NURSE or DOCTOR    |
| Nurse          | NURSE or DOCTOR    |
| Doctor         | DOCTOR only        |

---

## Assumptions & Trade-offs

### Specialist Scheduling

8 specialists across two shifts ensure exactly 4 are available (not on break) at every moment within the clinic window (08:00–18:00):

| Shift | Start | Break       | Shift ends | Specialists          |
|-------|-------|-------------|------------|----------------------|
| A     | 08:00 | 12:00–13:00 | 16:00      | 2 nurses + 2 doctors |
| B     | 09:00 | 13:00–14:00 | 17:00      | 2 nurses + 2 doctors |

Coverage breakdown:
- 08:00–09:00 → Shift A only (4 available)
- 09:00–12:00 → Shift A + B (8 available)
- 12:00–13:00 → Shift B only, Shift A on break (4 available)
- 13:00–14:00 → Shift A only, Shift B on break (4 available)
- 14:00–16:00 → Shift A + B (8 available)
- 16:00–17:00 → Shift B only (4 available)

Each specialist works 4 hours, takes a 1-hour break (exactly 4 hours after shift start), then works 4 more hours.

A slot is **available** if at least one eligible specialist is working at that time and has no existing booking for that exact slot.

### Other Assumptions

- **Single mock patient** — bookings are linked to a `Patient` entity (`username = "patient"`) seeded at startup. The controller resolves this hardcoded username. When real auth is added, replace it with the authenticated principal.
- **Mock authentication** — login accepts hardcoded credentials (`patient` / `kry123`). No JWT or session management.
- **In-memory H2 database** — specialists, patients, and bookings are seeded/reset on each restart. Production would use a persistent database.
- **Specialist assignment** — first available eligible specialist is assigned automatically. No patient preference or load-balancing between specialists.
- **UTC / local time** — the backend uses `LocalDateTime` (no timezone). The frontend formats using the browser's local time. In production these should be aligned explicitly.

---

## What I Would Improve Given More Time

1. **Real authentication** — JWT-based auth; replace hardcoded `"patient"` username lookup with the token principal
2. **Persistent database** — PostgreSQL profile alongside H2 dev profile
3. **Specialist preference** — allow patients to request a specific specialist or factor in load-balancing
4. **Frontend tests** — unit tests for the questionnaire state machine and slot formatting utilities
5. **Countdown timer** — live countdown on the home screen (currently shows static time until appointment)
6. **Timezone handling** — explicit timezone support (IANA timezone in requests/responses)
7. **OpenAPI/Swagger** — auto-generated API docs via springdoc-openapi
8. **Error handling** — structured API error responses (RFC 7807 Problem Details)
9. **Accessibility audit** — full keyboard navigation testing and screen reader verification

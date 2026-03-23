# Medical Triage & Booking Application

A fullstack medical triage application where patients answer a short questionnaire to determine the level of care they need and are then presented with available appointment slots to book.

## Tech Stack

### Backend — Java 21 + Spring Boot 3 + Spring Data JPA + H2

- Java 21 — latest LTS
- Spring Boot 3 — production choice #1 framework - free open-source, large community, minimalistic & feature-rich
- Spring Data JPA + H2 — default ORM, easy to swap in-memory with Postgres or MySQL
- Maven — standard build tool

### Frontend — React 18 + TypeScript + Vite + Tailwind CSS v4

- React 18 — industry-standard UI library
- TypeScript — type safety gives faster feedback, more LLM-friendly
- Vite — fast dev server and build tool
- Tailwind CSS v4 — open-source utility CSS, responsive design

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

The API starts on http://localhost:8080.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

The app starts on http://localhost:5173.

---

## Running Tests

### Backend tests

```bash
cd backend
./mvnw test
```

## Assumptions & Trade-offs

- Staffing - 8 specialists across two shifts ensure 4+ are available at every moment (08:00–18:00):

| Shift | Start | Break       | Shift ends | Specialists          |
|-------|-------|-------------|------------|----------------------|
| A     | 08:00 | 12:00–13:00 | 17:00      | 2 nurses + 2 doctors |
| B     | 09:00 | 13:00–14:00 | 18:00      | 2 nurses + 2 doctors |

- mock patient — bookings are linked to a hardcoded `Patient` entity (`username = "patient"`) seeded at startup.
- mock auth at FE — login accepts hardcoded credentials (`patient` / `kry123`).
- 2 professions: Doctor, Nurse.
  - Doctor can handle: Chat, Nurse, Doctor.
  - Nurse can handle: Chat & Nurse.
- no prioritizing or balancing specialists/professions.
- backend uses `LocalDateTime` (no timezone). Frontend uses browser local time.

---

## What to improve given more time

- Authentication — JWT-based auth, social login (Google, Facebook, Microsoft...)
- Persistent DB — PostgreSQL profile
- Frontend tests — unit tests for state machine and slot formatting
- Competence choice — prioritize doctors for Doctor, nurses for Nurse, add chat operators.
- Scaling - handle 10m users/doctors. Cloud DB, containers auto-scaling, caching, indexing, CDN, microservices & frontends.
- Performance - prioritize Apdex.
- Usability - optimize click path, customer development via focus group, A/B testing
- Monitoring - Error logs, Correlation ID, Observability for frontend & backend.
- AI agents for chat.
- Show subset of slots to customer (buckets by time hash) - performance & avoid overbooking 
- Limit appointments per user/area - rebook, show list of appointments
- Error responses
- Handle double-booking
- Accessibility WCAG AAA
- Live timer till appointment
- Timezones
- OpenAPI/Swagger
- Rate limiting
- Fraud filter
- Clinicians backoffice, Admin console

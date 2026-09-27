# CivicPulse

A gamified civic engagement platform that lets citizens report hyper-local civic issues — potholes, garbage, damaged infrastructure, unauthorized construction, stray animals, and public safety hazards — with transparent, SLA-tracked resolution and a reward-based reputation system.

Built for **Smart India Hackathon — Problem Statement DT-8**, sponsored by **Ernst & Young (EY)**.

**Live API:** https://civicpulse-backend-oamw.onrender.com
**Swagger Docs:** https://civicpulse-backend-oamw.onrender.com/swagger-ui/index.html

---

## Problem Statement

Citizens lose confidence in municipal governance because complaint resolution lacks transparency, timely communication, and accountability. Reports submitted through existing grievance systems disappear into administrative workflows without feedback. CivicPulse solves this with geo-tagged reporting, real-time tracking, automated SLA monitoring, and a Civic Reputation System (points, badges, leaderboards) that rewards accurate reporting and active participation.

---

## Roles

| Role | Responsibilities |
|---|---|
| **Citizen** | Registers, reports complaints with photo + geo-location, upvotes duplicate issues, earns points and badges |
| **Authority** | Verifies/rejects complaints for their department, sets priority, moves complaints through progress to resolution |
| **Admin** | Creates authority accounts, manages categories and SLA rules, views platform-wide analytics |

---

## Core Features

### Complaint Lifecycle
```
PENDING_VERIFICATION → VERIFIED → IN_PROGRESS → RESOLVED → CLOSED
                     ↘ REJECTED
```
Citizens submit complaints with title, description, category, photo, and precise GPS coordinates. Authorities verify (setting priority) or reject with a reason. Once resolved with proof photo, the citizen confirms closure.

### Duplicate Detection & Upvoting
Before a new complaint is created, the system checks for existing active complaints in the same category within a 150-meter radius (Haversine distance formula). Instead of filing a duplicate, citizens can upvote an existing report. Reaching 5 upvotes grants the original reporter a bonus; 10 upvotes auto-escalates priority to HIGH.

### SLA Engine
Admins define resolution deadlines per category and priority (e.g., "Pothole + HIGH = 24 hours"). The moment an authority verifies a complaint, a deadline is calculated automatically. A background scheduler runs every 15 minutes, checking every active complaint:
- **6 hours before deadline** → warning email to the assigned authority and all admins
- **After deadline passes** → breach email, and the tracker is flagged

### Civic Reputation System (Gamification)
| Action | Points |
|---|---|
| Complaint verified | +10 |
| Complaint resolved | +15 |
| Upvoting another complaint | +2 |
| Complaint reaches 5 upvotes (owner bonus) | +20 |

Every point award is logged in a reward history for full transparency. Badges (Bronze/Silver/Gold Reporter, Trusted Citizen, Community Hero) are calculated automatically from activity. A public leaderboard ranks the top 10 citizens by reputation.

### Admin Analytics
A single endpoint surfaces total complaints, resolved/pending/in-progress/rejected counts, category-wise breakdown, and average resolution time — giving admins civic hotspot visibility.

---

## Tech Stack

- **Backend:** Spring Boot (Java), Spring Security with JWT
- **Database:** PostgreSQL
- **Scheduling:** Spring `@Scheduled` background jobs
- **Email:** Gmail SMTP (Spring Mail)
- **API Docs:** springdoc-openapi / Swagger UI
- **Deployment:** Render (backend + managed PostgreSQL)
- **Frontend:** Flutter (Android/iOS)

---

## Database Schema

| Table | Purpose |
|---|---|
| `users` | Citizens, authorities, admins — role, department, reputation points |
| `categories` | Issue types with a default department mapping |
| `complaints` | Core complaint record — location, status, priority, assignment, resolution |
| `sla_rules` | Admin-defined deadline duration per category + priority |
| `sla_trackers` | Per-complaint deadline, breach flag, and warning state |
| `complaint_upvotes` | One upvote per citizen per complaint |
| `reward_history` | Audit trail of every reputation point awarded |

---

## API Overview

| Group | Base Path | Access |
|---|---|---|
| Auth | `/api/auth/**` | Public |
| Categories | `/api/categories` | Any authenticated user |
| Complaints | `/api/complaints/**` | Any authenticated user (ownership checked per action) |
| Authority actions | `/api/authority/**` | AUTHORITY, ADMIN |
| Admin | `/api/admin/**` | ADMIN |
| Leaderboard | `/api/leaderboard` | Public |

Full interactive documentation is available via Swagger at the link above.

---

## Getting Started

### Prerequisites
- Java 21+, Maven
- PostgreSQL

### Environment Variables
```
JDBC_DATABASE_URL=jdbc:postgresql://localhost:5432/civic_platform
DB_USERNAME=postgres
DB_PASSWORD=your_db_password
JWT_SECRET=your_jwt_secret
MAIL_USERNAME=your_email@gmail.com
MAIL_APP_PASSWORD=your_gmail_app_password
PORT=8080
```

### Run
```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`, with Swagger UI at `http://localhost:8080/swagger-ui/index.html`.

---

## Roadmap / Future Scope

- Multi-city hierarchy (city-level admins under a platform-wide super admin)
- Volunteer participation and community cleanup drives
- Push notifications alongside email alerts
- Department-level performance ratings

# Todo List App (Backend)

Spring Boot + Java 25 + Gradle backend for a multi-user to-do list with
deadline-based sorting, email reminders, and per-user local-midnight cleanup.

## Stack
- Java 25, Gradle
- Spring Boot 4.1.1 (Web/MVC, Security, Data JPA, Mail, Validation)
- MySQL
- JWT auth (stateless, for a separate React frontend on Vercel)

## Version notes (read this first)

- **Spring Boot 4.1.1.** The project originally targeted Spring Boot 3.4.1,
  but that line (and the entire 3.x series) is past its open-source
  support window as of this writing. 4.1.1 is the current stable release
  and has first-class Java 25 support. This *is* a major-version jump with
  real breaking changes — the most visible one already applied here is
  `spring-boot-starter-web` → `spring-boot-starter-webmvc`. If you extend
  this project, check the
  [Spring Boot 4.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)
  before adding new starters — several were renamed or modularized.
- **No `io.spring.dependency-management` plugin.** That third-party
  plugin's own documentation only lists support up to Gradle 8.x, with no
  confirmed Gradle 9 compatibility. Dependency versions are managed
  instead via Gradle's native BOM import (`platform(...)` in
  `build.gradle`), which is what Spring's own tooling has been steering
  people toward and avoids that uncertainty entirely. One consequence of
  this: the `platform(...)` import only propagates to configurations that
  extend `implementation`. The standalone `annotationProcessor`
  configuration doesn't, so Lombok's version can't be left for the BOM to
  fill in — it's pinned explicitly in `build.gradle` instead
  (`1.18.46`, which has confirmed JDK 25 support).
- **Jackson 3 and Spring Security 7** ship as part of this Boot version.
  Nothing in this codebase uses custom Jackson configuration or unusual
  Spring Security APIs, so it should be unaffected — but since this
  environment has no network access to Maven Central, none of this has
  actually been compiled. Treat your first local `./gradlew build` as a
  real first run, not a formality.

## Before you run it

**1. Gradle wrapper**
This project doesn't ship a Gradle wrapper jar (this environment has no
network access to Gradle's distribution servers). Generate one locally,
once, with a system-installed Gradle:

```
gradle wrapper --gradle-version 9.3.0
```

Gradle 9.3.0 is what Spring's own project generator currently uses for
Spring Boot 4.x projects, and it's well past the 9.1.0 minimum Gradle
requires for genuine Java 25 support (both for running Gradle itself and
for toolchains) — anything Gradle 8.x will not reliably work here. That
creates `gradlew`, `gradlew.bat`, and `gradle/wrapper/`. After that, use
`./gradlew` as normal.

**2. MySQL database**
Create a local database (matching `application.yml`'s default):
```sql
CREATE DATABASE todo_list_app;
```
Hibernate is set to `ddl-auto: update`, so tables are created automatically
on first run — no manual schema/migration step needed for v1.

**3. Environment variables**
Set these (a `.env` + your IDE's env loader, or plain shell exports work
locally; on Render, set them in the service's Environment tab):

| Variable | Purpose |
|---|---|
| `DB_URL` | e.g. `jdbc:mysql://localhost:3306/todo_list_app` |
| `DB_USERNAME` / `DB_PASSWORD` | MySQL credentials |
| `JWT_SECRET` | Long random string — **do not use the default in production** |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | SMTP credentials (e.g. a Gmail app password) |
| `MAIL_HOST` / `MAIL_PORT` | SMTP server (defaults to Gmail's) |
| `ALLOWED_ORIGINS` | Your Vercel frontend URL, e.g. `https://your-app.vercel.app` |

## Running locally
```
./gradlew bootRun
```

## API overview

**Auth** (`/api/auth`, public)
- `POST /register` — `{ email, password, timezone }` → `{ token, email }`
- `POST /login` — `{ email, password }` → `{ token, email }`

**Tasks** (`/api/tasks`, require `Authorization: Bearer <token>`)
- `GET /` — list the caller's tasks, sorted soonest-deadline-first
- `POST /` — `{ title, deadline }` create a task
- `PUT /{id}` — update title/deadline
- `PATCH /{id}/complete` — mark completed
- `DELETE /{id}` — delete a task

Each task in a response includes an `expired` boolean — the frontend should
render `expired: true` tasks highlighted red + bold.

## Background jobs
- **ReminderScheduler** — polls every `REMINDER_POLL_INTERVAL_MS` (default
  30s). Sends 1 daily reminder while a task's deadline is >24h away (fired
  at the same local time-of-day the task was created), then switches to 5
  countdown reminders at 24h / 6h / 1.5h / 22.5min / 1min once inside the
  final 24 hours.
- **CleanupScheduler** — polls every `CLEANUP_POLL_INTERVAL_MS` (default
  15min) and, for any user whose local time is currently in the midnight
  hour, deletes their tasks that were completed or expired ≥1 day ago.

## Not yet built
- Frontend (React, to be deployed on Vercel) — not part of this backend repo
- Password reset / email verification flow
- Rate limiting on auth endpoints
- Automated tests

# KodBTW

KodBTW brings coding-platform profiles, persisted statistics, dashboard analytics, historical progress, and public developer profiles into one application.

## Architecture and stack

- **Frontend:** React 18, TypeScript, Vite
- **Backend:** Java 21, Spring Boot 3.3, Spring Security, Spring Data JPA, JWT
- **Database:** MySQL 8 with ordered Flyway migrations (`V1`–`V6`)
- **Request flow:** React application → Spring Boot REST API → MySQL

The backend has REAL adapters for LeetCode and Codeforces. CodeChef, GeeksForGeeks, and HackerRank remain MOCK sources. History and insights use persisted snapshots and do not fabricate missing observations.

## Repository layout

```text
backend/     Spring Boot application, Flyway migrations, and Maven tests
frontend/    React + TypeScript application and Vite configuration
docs/        Architecture and integration notes
```

## Prerequisites

- Java 21
- MySQL 8
- Node.js 18 or newer and npm

## Local setup

Create an empty local database with a development MySQL user. Do not use the MySQL root account for a deployed application.

```sql
CREATE DATABASE kodbtw CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
```

The backend requires `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `JWT_SECRET`. It intentionally has no committed database credential or JWT signing-key fallback. `backend/src/test/resources/application.yml` supplies test-only H2 configuration and a test-only signing key.

For a local PowerShell session, set values without putting them in the repository. Generate a fresh signing secret for the session:

```powershell
$env:DB_URL = "jdbc:mysql://127.0.0.1:3306/kodbtw?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME = Read-Host "Local MySQL username"
$secureDbPassword = Read-Host "Local MySQL password" -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new("", $secureDbPassword).Password
$secretBytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Fill($secretBytes)
$env:JWT_SECRET = [Convert]::ToBase64String($secretBytes)
$env:CORS_ALLOWED_ORIGINS = "http://localhost:5173"
```

`.env.example` lists backend variable names and placeholder values. Spring Boot does not automatically load that file; set the variables in the process environment or your secret manager. Never commit populated `.env` files.

Run the backend from `backend/`:

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
.\mvnw.cmd spring-boot:run
```

Flyway applies the versioned migrations in order. Migration validation is enabled, automatic baselining is disabled, and Flyway clean is disabled. The application validates the schema with Hibernate; it does not create or update tables through Hibernate.

Run the frontend from `frontend/`:

```powershell
npm ci
npm run dev
```

Vite serves the local app on port `5173` and proxies `/api` to `http://localhost:8080` for development only. `frontend/.env.example` documents `VITE_API_BASE_URL`. Set it to the public API origin at build time when frontend and API use different origins; leave it empty when a reverse proxy serves the API under the same origin. Vite variables are public build-time values and must never contain credentials or signing secrets.

## Runtime configuration

| Variable | Purpose | Default / requirement |
|---|---|---|
| `DB_URL` | JDBC URL for MySQL | Required; configure TLS for remote databases |
| `DB_USERNAME` | Database application user | Required; use a least-privilege account |
| `DB_PASSWORD` | Database password | Required; inject from a secret manager |
| `JWT_SECRET` | HMAC key used to sign JWTs | Required; generate at least 32 random bytes |
| `JWT_EXPIRATION_MS` | JWT lifetime in milliseconds | Defaults to 24 hours |
| `CORS_ALLOWED_ORIGINS` | Comma-separated exact browser origins | Local default is `http://localhost:5173`; required in `prod` profile |
| `PORT` | Backend HTTP port | Defaults to `8080` |
| `VITE_API_BASE_URL` | Public API origin compiled into frontend assets | Defaults to same-origin relative API paths |

For production, activate `SPRING_PROFILES_ACTIVE=prod` and provide exact HTTPS frontend origins in `CORS_ALLOWED_ORIGINS`. Wildcard origins are rejected. Authentication uses an Authorization bearer header, so cross-origin cookies are not enabled. Put secrets in the hosting platform’s secret manager, never in Vite variables.

## API overview

All routes are under `/api`.

| Method | Endpoint | Access |
|---|---|---|
| `GET` | `/api/health` | Public liveness response (`UP`) |
| `POST` | `/api/auth/register` | Public |
| `POST` | `/api/auth/login` | Public; returns a JWT |
| `GET` | `/api/auth/me` | Authenticated |
| `GET` | `/api/public/profiles/{username}` | Public developer profile |
| `GET` | `/api/dashboard/stats`, `/api/dashboard/analytics`, `/api/dashboard/history`, `/api/dashboard/insights` | Authenticated, owner-scoped |
| `GET` | `/api/dashboard/activity`, `/api/dashboard/contests` | Authenticated, persisted-data intelligence |
| `/api/platform-accounts/**` | Platform account operations and sync | Authenticated, account ownership enforced |
| `/api/leaderboard/**` | Leaderboard reads | Authenticated; manual sync is admin-only |

The health endpoint is a process liveness check and does not expose database diagnostics. Configure platform-specific readiness checks at the hosting layer if required.

## Deployment preparation

No deployment target or hosting provider is configured in this repository. Before deployment:

1. Provision MySQL 8 and a least-privilege application account; configure database TLS and backups.
2. Supply `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and a generated `JWT_SECRET` through the hosting secret manager.
3. Activate the `prod` Spring profile and set exact HTTPS `CORS_ALLOWED_ORIGINS`.
4. Build the frontend with the API origin in `VITE_API_BASE_URL`, or configure same-origin `/api` routing through a TLS-terminating reverse proxy.
5. Build and test the backend with `backend\mvnw.cmd test` and `backend\mvnw.cmd package`; build the frontend with `npm ci` and `npm run build`.
6. Confirm Flyway has applied and validated all migrations before directing traffic. Do not enable baseline-on-migrate to bypass missing migration history.
7. Configure the host’s liveness probe to call `/api/health`, then verify login, `/api/auth/me`, protected dashboard calls, sync ownership, and public profile access in the deployed environment.

This repository currently contains no Dockerfile, container orchestration, or hosting-provider deployment configuration.

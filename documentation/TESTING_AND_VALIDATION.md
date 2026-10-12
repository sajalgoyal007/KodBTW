# Testing and validation

This record distinguishes automated repository checks from live-system verification. Results below must be updated only from commands actually run against the current checkout.

## Automated test inventory

| Area | Location / command | Coverage represented in repository |
|---|---|---|
| Backend unit and service tests | `backend/src/test/java/`; `cd backend && .\mvnw.cmd test` | Auth, services, validation, platform clients/adapters, persistence behavior, failure handling |
| Backend controller/security tests | `backend/src/test/java/com/kodbtw/controller/`, `config/`, `security/` | REST behavior, authentication/authorization, safe error behavior |
| Database migration check | `FlywayV7MySqlMigrationTest` and related migration tests | Migration validation; MySQL/Testcontainers execution requires Docker availability |
| Frontend tests | `frontend/tests/`; `cd frontend && npm test` | Presentation utilities, auth/request behavior, accessibility markup, theme contrast, data-source labels |
| Frontend production build | `cd frontend && npm run build` | TypeScript project build and Vite production bundle |
| CI | `.github/workflows/backend-ci.yml` | Backend Maven test/package job; no frontend CI job is configured in this checkout |
| Browser E2E / production smoke tests | No suite configured | Not represented by automated results below |

## Current checkout verification

Validation was run on 12 October 2026 from the repository working tree, which contains pre-existing unstaged/untracked application changes. The commands did not stage or modify source files.

| Check | Result |
|---|---|
| Backend full suite (`.\mvnw.cmd test`) | **Environment-blocked:** 262 tests enumerated, 0 assertion failures, 1 error, 2 skipped. `FlywayV7MySqlMigrationTest` errored because Testcontainers could not find a Docker environment. |
| Backend suite excluding Docker-dependent migration class (`.\mvnw.cmd '-Dtest=!FlywayV7MySqlMigrationTest' test`) | **BUILD SUCCESS:** 261 tests, 0 failures, 0 errors, 2 skipped. The Flyway V7 MySQL migration test was excluded, so this does not validate that MySQL migration path. |
| Frontend tests (`npm test`) | **Passed:** 70 tests, 0 failures, 0 skipped. |
| Frontend production build (`npm run build`) | **Passed:** `tsc -b` and Vite production bundle completed successfully. |
| Backend package / CI workflow | **Not run for this documentation pass.** |
| Production API / browser E2E | **Not verified.** No claim of production health or live data accuracy is made. |

These results reflect the current working tree, including its pre-existing source modifications and untracked files. They do not represent only the documentation changes. Do not use earlier phase test totals as if they were results for this checkout.

## Interpretation and limitations

- Unit tests using mocks/fixtures verify application behavior against those inputs; they do not establish external platform uptime, authorization, rate limits, or current production data accuracy.
- Testcontainers database checks depend on Docker and may be skipped when Docker is unavailable. Report skipped tests explicitly.
- Frontend unit tests and a successful build do not establish cross-browser visual quality, mobile-device behavior, accessibility conformance, or page performance.
- No coverage percentage, production SLA, or performance metric is reported because no corresponding verified report was found.

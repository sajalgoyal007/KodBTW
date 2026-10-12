# KodBTW architecture notes

This document describes the current repository implementation rather than the original phased roadmap. For the editable end-to-end diagram, see [`documentation/diagrams/architecture/SYSTEM_ARCHITECTURE.md`](../documentation/diagrams/architecture/SYSTEM_ARCHITECTURE.md).

## Runtime structure

- **Frontend:** React 18, TypeScript, React Router, and Vite. The client serves public landing/profile routes and authenticated account/dashboard routes.
- **API:** Java 21 and Spring Boot 3.3 REST controllers delegate to services. Spring Security is stateless; requests use signed JWT bearer tokens. BCrypt hashes passwords.
- **Persistence:** Spring Data JPA repositories use MySQL. Flyway V1–V7 manage DDL; Hibernate validates the migrated schema.
- **Platform data:** `PlatformAdapter` and `PlatformAdapterRegistry` provide the adapter boundary. LeetCode, Codeforces, and CodeChef are enabled by `Platform.hasLiveStatsSource()`. CodeChef uses an unofficial third-party provider and is labelled `CODECHEF_THIRD_PARTY`. GFG/HackerRank adapter/client prototypes present in the current local working tree are excluded from normal sync and remain unavailable.
- **Analytics/history:** source-labelled snapshots are stored per user/platform/date; services create dashboard responses and the leaderboard cache is a denormalized projection.
- **Contest calendar:** Codeforces is the default provider; a local fixture provider can be explicitly selected for UI testing.

## Request path

The browser sends JSON requests to Spring controllers. Protected requests carry a JWT in the `Authorization` header. The JWT filter verifies signature/expiry and loads the subject user. Services enforce ownership before profile/account operations. Repositories load/persist entities. Public profile access uses a dedicated response DTO and a public API route; authenticated account data is not exposed through that route.

On sync, `PlatformSyncService` verifies ownership, applies the live-source gate, lock/cooldown and Codeforces spacing, marks status, resolves an adapter, then persists a validated result or a classified failure. A failed refresh retains prior successful stats. See [platform data acquisition](platform-data-acquisition.md) for source provenance and authorization cautions.

## Repository deployment artifacts

The frontend includes a Vite build and Vercel SPA rewrite configuration. The backend has a multi-stage Java 21 Dockerfile running as a non-root user. MySQL and secrets are external runtime requirements. These artifacts do not prove current hosting account configuration or production deployment status.

## Documentation map

- [System architecture diagram](../documentation/diagrams/architecture/SYSTEM_ARCHITECTURE.md)
- [Data flow, authentication, sync, deployment diagrams](../documentation/diagrams/system-design/README.md)
- [UML diagrams](../documentation/diagrams/uml/README.md)
- [Database schema and ER diagram](../documentation/diagrams/database/README.md)
- [Platform integration diagram](../documentation/diagrams/platform-integration/README.md)
- [Codeforces field mapping](CODEFORCES_INTEGRATION.md)
- [Contest fixture mode](contest-calendar-fixture-mode.md)

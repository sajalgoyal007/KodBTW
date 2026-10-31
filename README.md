# KodBTW

### Unified Coding Identity & Campus Coding Intelligence Platform

<p align="center">
  <a href="https://github.com/sajalgoyal007/KodBTW"><img src="https://img.shields.io/badge/Project-KodBTW-19866a?style=for-the-badge" alt="KodBTW project"></a>
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21">
  <img src="https://img.shields.io/badge/Spring_Boot-3.3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot 3.3">
  <img src="https://img.shields.io/badge/React-18-149ECA?style=for-the-badge&logo=react&logoColor=white" alt="React 18">
  <img src="https://img.shields.io/badge/MySQL-8-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL 8">
</p>

<p align="center">
  <strong>One place to connect coding profiles, understand available statistics, and follow recorded progress.</strong><br>
  <a href="https://kodbtw.vercel.app">Open KodBTW</a> ·
  <a href="https://github.com/sajalgoyal007/KodBTW">Source repository</a> ·
  <a href="#project-documents--deliverables">Project documents</a>
</p>

> **Data note:** each platform reports different fields and uses different rating scales. KodBTW keeps source and availability context visible; an unavailable value is not treated as zero. The landing-page preview uses sample data, and live platform coverage depends on the enabled adapter.

### Evaluator demo access

Temporary evaluator login (remove or rotate after grading):

- **Email:** `sajalgoyal2007@gmail.com`
- **Password:** `12345678`

---

## Contents

- [Project administration](#project-administration)
- [Team roster](#team-roster)
- [Abstract](#abstract)
- [Problem, goals, and scope](#problem-goals-and-scope)
- [User roles and access](#user-roles-and-access)
- [Requirements at a glance](#requirements-at-a-glance)
- [System architecture](#system-architecture)
- [Design diagrams](#design-diagrams)
- [Functional modules](#functional-modules)
- [Platform integration status](#platform-integration-status)
- [Database design](#database-design)
- [Security and non-functional considerations](#security-and-non-functional-considerations)
- [Team-wise role and contribution focus](#team-wise-role-and-contribution-focus)
- [Integration and validation](#integration-and-validation)
- [Screenshots](#screenshots)
- [Repository structure](#repository-structure)
- [Run locally](#run-locally)
- [API overview](#api-overview)
- [Project documents & deliverables](#project-documents--deliverables)
- [Limitations and future work](#limitations-and-future-work)
- [License](#license)

## Project administration

| Item | Details |
|---|---|
| Project title | **KodBTW — Unified Coding Identity & Campus Coding Intelligence Platform** |
| Project code | `PBL2627-CSE-C-064` |
| Course | Project Based Learning / Advanced Java Laboratory (Java Projects), 5th Semester |
| Department | Computer Science & Engineering |
| Institution | Arya College of Engineering & Information Technology, Jaipur, Rajasthan |
| Academic session | 2026–27 |
| Project guide | Er. Ram Babu Buri, Department of CSE |
| Project period | 6 July 2026 – 6 October 2026 |
| Application | [kodbtw.vercel.app](https://kodbtw.vercel.app) |
| Source code | [github.com/sajalgoyal007/KodBTW](https://github.com/sajalgoyal007/KodBTW) |

The academic details and enrollment IDs below are transcribed from supplied project documents. Confirm spelling, enrollment-ID format, and institutional details against college records before final submission.

## Team roster

| Member | Enrollment ID (as listed in report) | Team role focus |
|---|---|---|
| Sajal Goyal | `24EARCS142` | Team lead; API and authentication |
| Shri Krishna Chandra Vaishnav | `24EARCS157` | Frontend application and user experience |
| Yug Mittal | `24EARCS189` | Platform integrations and statistics synchronization |
| Ravi Shah | `24EARCS130` | Database, persistence, and analytics |
| Suryadev Singh | `24EARCS170` | Quality, contest calendar, and documentation |

## Abstract

Competitive programmers often maintain separate profiles on multiple online judges, each with its own rating scale, terminology, statistics, and history. KodBTW is a full-stack web application designed to bring supported coding identities into one account and provide a single place to review the statistics that each connected source actually reports. A developer can manage platform handles, request synchronization, inspect dashboard summaries and analytics, view recorded progress, review upcoming contests, browse leaderboard rankings, and optionally share a public developer profile.

The system is organized as a React and TypeScript single-page application communicating with a Java 21 / Spring Boot REST API. Spring Security protects account operations with stateless JWT authentication; BCrypt is used for password hashing. Spring Data JPA persists application records in MySQL 8, while Flyway applies versioned schema migrations. Platform-specific adapters and clients translate provider responses into a normalized statistics model. Successful observations are stored as dated snapshots with source labels, allowing the interface to distinguish provider data, unsupported fields, and missing observations.

A central design concern is preserving the meaning and provenance of the data while combining it. The same problem may be counted on more than one platform, ratings are not directly comparable across providers, and an unavailable metric must not appear as zero. KodBTW therefore retains nullable fields, exposes source and synchronization status, and presents historical values only for dates with stored observations. The live integration set is bounded: LeetCode and Codeforces use public sources, CodeChef currently relies on an unofficial third-party provider, and GeeksforGeeks and HackerRank prototypes are not enabled for normal synchronization.

The project demonstrates a modular frontend/backend design, authenticated and public API boundaries, schema evolution, provider adaptation, and data-aware dashboard views. It is an engineering artifact and a personal progress-review tool; the repository does not claim measured learning outcomes, results from a user study, production service guarantees, or third-party authorization beyond what is documented.

## Problem, goals, and scope

### Problem statement

Developers who practise on several coding platforms have to visit separate sites to review their profiles and progress. The main issues addressed by KodBTW are:

- Each coding service maintains a separate profile and presents only its own view of a developer.
- Similar-looking metrics may use different definitions and rating scales.
- Provider APIs may omit fields or become unavailable, so missing data cannot safely be represented as zero.
- A current profile view does not provide a consistent record of earlier observations.
- Aggregated figures can mislead when their source, freshness, or coverage is hidden.

### Project goals

- Provide registration, login, email verification, and owner-scoped profile management.
- Let users store their coding-platform handles and synchronize supported public statistics.
- Preserve successful observations as dated, source-labelled snapshots.
- Present available values through dashboard, analytics, history, leaderboard, and contest views.
- Offer a public profile through a dedicated, sanitized response model.
- Keep missing or unsupported metrics unavailable instead of inventing zeroes or estimated history.

### Scope boundaries

**Included in scope:**

- React/TypeScript frontend and Spring Boot REST API.
- Account authentication, profile management, platform-account ownership, and synchronization status.
- MySQL persistence with Flyway-managed schema evolution.
- Dashboard, analytics, history, contest calendar, leaderboard, and public profile views.
- Provider adapters for the sources enabled by the application.

**Outside the verified scope:**

- Guaranteed uptime or stability of third-party providers.
- Authorization to collect, store, or redistribute third-party data beyond the provider's documented terms.
- Campus-wide student monitoring, mentor dashboards, or learning-outcome measurement.
- Fabricated values for missing metrics or estimated history between synchronization dates.

## User roles and access

- **Visitor:** register or log in, check the public health endpoint, and view public profiles exposed through the public profile route.
- **Registered developer:** manage their own profile and platform accounts; view their dashboard, analytics, history, contest information, and leaderboard views. Account operations are scoped to the authenticated user.
- **Administrator:** has the `ADMIN` authority required for the manual leaderboard-cache synchronization endpoint.

- Public profile data is returned through a dedicated response DTO rather than by exposing a database user entity.
- Frontend route guards improve navigation flow; backend authorization remains the authority for protected API operations.

## Requirements at a glance

The [Software Requirements Specification](documentation/project-deliverables/KodBTW_SRS_Supplied.pdf) describes the implemented API surface and its constraints. The table below summarizes the user-visible requirements represented by the current project documentation.

| ID | Requirement | Implementation area |
|---|---|---|
| FR-01 | Register, verify email, and authenticate a user | Authentication API and frontend auth pages |
| FR-02 | Connect and manage platform handles for the signed-in user | Platform-account API and Coding Profiles page |
| FR-03 | Synchronize available statistics from enabled sources | Adapter registry, provider clients, sync service |
| FR-04 | Store dated observations with source and sync status | Snapshot persistence and Flyway-managed schema |
| FR-05 | Show dashboard, comparison, history, and insights views | Dashboard API and frontend analytics pages |
| FR-06 | Show upcoming contests and platform standings where available | Contest provider and contest calendar API |
| FR-07 | Provide paginated leaderboard views | Leaderboard API and cached projection |
| FR-08 | Share a public developer profile | Public profile endpoint and `/u/:username` route |
| FR-09 | Allow Google sign-in and account linking flows | Google authentication and challenge endpoints |

## System architecture

KodBTW follows a layered, browser-to-database request path:

- The React/Vite frontend serves public pages and authenticated application views.
- The Spring REST API validates requests and uses Spring Security to protect private operations.
- Services coordinate authentication, profiles, platform synchronization, analytics, and contest/leaderboard features.
- JPA repositories persist domain records in MySQL; Flyway controls schema changes.
- The platform adapter registry selects a provider-specific client; successful observations become source-labelled snapshots.
- Unsupported and missing fields remain unavailable instead of being filled with invented values.

```mermaid
flowchart LR
  U[Developer in browser] -->|HTTPS / React Router| FE[React 18 + TypeScript SPA]
  FE -->|JSON REST; bearer JWT on protected routes| API[Spring Boot REST API]
  API --> SEC[Spring Security + JWT filter]
  API --> C[Controllers]
  C --> S[Application services]
  S --> R[Spring Data JPA repositories]
  R --> DB[(MySQL 8)]
  V[Flyway migrations V1–V12] --> DB[(MySQL 8)]
  S --> AR[PlatformAdapterRegistry]
  AR --> LC[LeetCode adapter/client]
  AR --> CF[Codeforces adapter/client]
  AR --> CC[CodeChef adapter/client]
  LC -->|Undocumented public GraphQL query| LEET[LeetCode]
  CF -->|Public user.info API| CODEFORCES[Codeforces]
  CC -->|Unofficial third-party provider| CCP[CodeChef stats provider]
  S --> CAL[Contest calendar service]
  CAL --> CFP[Codeforces contest provider]
  CAL -. local configuration only .-> FIX[Fixture contest provider]
  AR -. excluded by source gate .-> GF[GeeksforGeeks prototype]
  AR -. excluded by source gate .-> HR[HackerRank prototype]
  S --> DTO[Sanitized public profile DTO]
```

### Architecture figures

The following images were extracted from the supplied final project report:

- Layered application architecture.
- Platform synchronization workflow.
- Database entity relationships.

Editable design sources are available in the [architecture](documentation/diagrams/architecture/SYSTEM_ARCHITECTURE.md), [system design](documentation/diagrams/system-design/README.md), and [UML](documentation/diagrams/uml/README.md) documentation.

![Layered KodBTW architecture](documentation/diagrams/assets/architecture-layered.png)

![Platform synchronization workflow](documentation/diagrams/assets/platform-synchronization-workflow.png)

![Database entity relationship overview](documentation/diagrams/assets/entity-relationship-overview.png)

## Design diagrams

The following Mermaid diagrams are embedded in this README so GitHub renders the architecture, data flow, synchronization, deployment, database, and UML views inline. The linked documentation files retain their editable source and supporting notes.

### Data-flow diagram

```mermaid
flowchart LR
  User[Developer] -->|credentials / profile handle| Web[React frontend]
  Web -->|HTTPS JSON; bearer token when authenticated| Auth[Spring Security and REST controllers]
  Auth -->|validated user context| Services[Application services]
  Services -->|owner-scoped records and snapshots| Store[(MySQL)]
  Services -->|public username lookup| PublicAPI[Public profile API consumer]
  Services -->|profile request| Platform[Platform and provider APIs]
  Platform -->|response or error| Services
  Services -->|DTO, values, and sync status| Web
  Web -->|rendered view| User
```

### Authentication sequence

```mermaid
sequenceDiagram
  actor Browser
  participant FE as React frontend
  participant Auth as AuthController / AuthService
  participant Users as UserRepository / MySQL
  participant JWT as JwtService / Security filter
  Browser->>FE: Submit email and password
  FE->>Auth: POST /api/auth/login
  Auth->>Users: Find normalized email
  Users-->>Auth: User record or no match
  Auth->>Auth: Verify password with BCrypt
  Auth->>JWT: Generate signed token with subject and expiry
  JWT-->>Auth: JWT
  Auth-->>FE: Login response and user details
  FE-->>Browser: Store session and navigate
  Browser->>FE: Request protected data
  FE->>JWT: API request with bearer token
  JWT->>JWT: Verify signature and expiration
  JWT->>Users: Resolve subject user id
  JWT-->>FE: Authenticated request or 401/403
```

### Platform synchronization activity

```mermaid
flowchart TD
  Start[Authenticated user requests sync] --> Own{Account belongs to user?}
  Own -->|No| Deny[Return not found or deny access]
  Own -->|Yes| Supported{Platform has live stats source?}
  Supported -->|No| Pending[Return pending or unavailable status]
  Supported -->|Yes| Gate{Sync gate acquired?}
  Gate -->|No| Current[Return current status]
  Gate -->|Yes| Cooldown{Outside cooldown?}
  Cooldown -->|No| Current
  Cooldown -->|Yes| Running[Record RUNNING attempt]
  Running --> Fetch[Adapter fetches and validates response]
  Fetch -->|Valid fields| Success[Persist source-labelled snapshot]
  Fetch -->|Provider or validation error| Failure[Store failure; preserve prior success]
  Success --> Done[Return sync response]
  Failure --> Done
  Pending --> Done
  Current --> Done
  Deny --> Done
```

### Deployment and configuration view

```mermaid
flowchart LR
  Dev[Developer or CI] -->|npm run build| Static[Frontend dist/]
  Static -. configured SPA hosting .-> Vercel[Vercel project configuration]
  Dev -->|Docker build| Image[Backend Java 21 image]
  Image -. requires separately provisioned host .-> Host[Java container host]
  Host -->|DB_URL and credentials injected outside image| DB[(MySQL 8)]
  Static -->|VITE_API_BASE_URL or same-origin /api| Host
```

This shows repository packaging capability; it does not assert that the hosting account, database service, or production deployment was verified.

### Platform integration design

```mermaid
flowchart LR
  Account[PlatformAccount] --> Sync[PlatformSyncService]
  Sync --> Gate{Platform.hasLiveStatsSource?}
  Gate -->|LeetCode, Codeforces, CodeChef| Registry[PlatformAdapterRegistry]
  Gate -->|GFG, HackerRank| Pending[Return pending / unavailable]
  Registry --> Interface[PlatformAdapter]
  Interface --> LC[LeetCodeAdapter]
  Interface --> CF[CodeforcesAdapter]
  Interface --> CC[CodeChefAdapter]
  Interface -. disabled prototypes .-> GFG[GeeksForGeeksAdapter]
  Interface -. disabled prototypes .-> HR[HackerRankAdapter]
  LC --> LCC[LeetCodeClient]
  CF --> CFC[CodeforcesClient]
  CC --> CCC[CodeChefClient]
  LCC --> API1[LeetCode GraphQL]
  CFC --> API2[Codeforces user.info]
  CCC --> API3[Unofficial CodeChef stats provider]
  LC --> Normalize[Normalize nullable PlatformStats]
  CF --> Normalize
  CC --> Normalize
  Normalize --> Persist[PlatformSyncPersistenceService]
  Persist --> Snapshot[(Snapshots and sync status)]
```

### Entity-relationship diagram

```mermaid
erDiagram
  USER ||--o| PROFILE : owns
  USER ||--o{ USER_AUTH_IDENTITY : links
  USER ||--o{ PLATFORM_ACCOUNT : connects
  USER ||--o{ PLATFORM_STAT_SNAPSHOT : records
  USER ||--o| LEADERBOARD_USER_CACHE : projects
  USER ||--o{ EMAIL_VERIFICATION_TOKEN : verifies
  USER o|--o{ GOOGLE_AUTH_CHALLENGE : may_bind
  USER {
    bigint id PK
    varchar email UK
    varchar password_hash "nullable"
    timestamp email_verified_at "nullable"
  }
  PROFILE {
    bigint id PK
    bigint user_id FK "unique"
    varchar username UK
    varchar display_name
    text bio
    boolean indexing_opt_in
  }
  USER_AUTH_IDENTITY {
    bigint id PK
    bigint user_id FK
    varchar provider
    varchar subject
  }
  PLATFORM_ACCOUNT {
    bigint id PK
    bigint user_id FK
    varchar platform
    varchar handle
    varchar sync_status
  }
  PLATFORM_STAT_SNAPSHOT {
    bigint id PK
    bigint user_id FK
    varchar platform
    date snapshot_date
    int total_solved
    int rating
    varchar source
  }
  LEADERBOARD_USER_CACHE {
    bigint user_id PK "also references USER"
    double weighted_score
    timestamp updated_at
  }
  EMAIL_VERIFICATION_TOKEN {
    bigint id PK
    bigint user_id FK
    varchar token_hash
    timestamp expires_at
  }
  GOOGLE_AUTH_CHALLENGE {
    bigint id PK
    bigint user_id FK "nullable"
    varchar operation
    varchar nonce_hash
    timestamp expires_at
  }
```

### UML use cases

```mermaid
flowchart LR
  Visitor([Visitor]) --> Landing[View landing page]
  Visitor --> Register[Register account]
  Visitor --> Login[Log in]
  Visitor --> PublicProfile[View public developer profile]
  User([Authenticated developer]) --> Profile[Read or update own profile]
  User --> Accounts[Manage own platform accounts]
  User --> Sync[Request supported platform sync]
  User --> Dashboard[View dashboard, analytics, history, insights]
  User --> Calendar[View contest calendar]
  User --> Board[View leaderboard and own rank]
  Admin([ADMIN authority]) --> Trigger[Trigger leaderboard cache sync]
```

### UML component and domain-class view

```mermaid
classDiagram
  class AuthController
  class ProfileController
  class PublicProfileController
  class PlatformAccountController
  class DashboardController
  class LeaderboardController
  class ContestCalendarController
  class AuthService
  class ProfileService
  class PublicProfileService
  class PlatformAccountService
  class PlatformSyncService
  class PlatformSyncPersistenceService
  class PlatformStatsService
  class PlatformAdapterRegistry
  class PlatformAdapter {
    <<interface>>
    +getPlatform() Platform
    +fetchStats(account) PlatformStats
  }
  class LeetCodeAdapter
  class CodeforcesAdapter
  class CodeChefAdapter
  class GeeksForGeeksAdapter
  class HackerRankAdapter
  class User {
    Long id
    String email
    String passwordHash
  }
  class Profile {
    Long id
    String username
    String displayName
    String bio
  }
  class PlatformAccount {
    Long id
    Platform platform
    String handle
    SyncStatus syncStatus
  }
  class PlatformStatSnapshot {
    Long id
    LocalDate snapshotDate
    Integer rating
    Integer maxRating
    String source
  }
  class LeaderboardUserCache {
    Long userId
    Double weightedScore
  }
  AuthController --> AuthService
  ProfileController --> ProfileService
  PublicProfileController --> PublicProfileService
  PlatformAccountController --> PlatformAccountService
  PlatformAccountService --> PlatformSyncService
  PlatformSyncService --> PlatformAdapterRegistry
  PlatformSyncService --> PlatformSyncPersistenceService
  PlatformSyncPersistenceService --> PlatformStatsService
  PlatformAdapterRegistry o--> PlatformAdapter
  PlatformAdapter <|.. LeetCodeAdapter
  PlatformAdapter <|.. CodeforcesAdapter
  PlatformAdapter <|.. CodeChefAdapter
  PlatformAdapter <|.. GeeksForGeeksAdapter
  PlatformAdapter <|.. HackerRankAdapter
  PlatformAdapter ..> PlatformStats
  Profile --> User
  PlatformAccount --> User
  PlatformStatSnapshot --> User
  LeaderboardUserCache --> User
```

GFG and HackerRank adapter classes in this overview are prototypes and are not enabled for normal statistics synchronization.

### Additional UML authentication sequence

```mermaid
sequenceDiagram
  actor User
  participant UI as LoginPage / AuthContext
  participant API as AuthController
  participant Service as AuthService
  participant DB as UserRepository
  participant JWT as JwtService
  User->>UI: Submit credentials
  UI->>API: POST /api/auth/login
  API->>Service: login(LoginRequest)
  Service->>DB: Find normalized email
  DB-->>Service: User or no match
  Service->>Service: Compare password with BCrypt
  Service->>JWT: Generate token
  JWT-->>Service: Signed JWT
  Service-->>API: LoginResponse
  API-->>UI: JWT and user response
  UI-->>User: Authenticated session
  User->>UI: Open protected page
  UI->>API: Request with bearer JWT
  API->>JWT: JwtAuthenticationFilter validates claims
  JWT->>DB: Load subject user id
  API-->>UI: Continue or return unauthorized
```

### UML platform synchronization sequence

```mermaid
sequenceDiagram
  actor User
  participant UI as Coding Profiles page
  participant Controller as PlatformAccountController
  participant Sync as PlatformSyncService
  participant Registry as PlatformAdapterRegistry
  participant Adapter as Enabled PlatformAdapter
  participant Provider as Provider API
  participant Store as Persistence service / MySQL
  User->>UI: Request account refresh
  UI->>Controller: POST /api/platform-accounts/{id}/sync
  Controller->>Sync: syncOwnedAccount(userId, accountId)
  Sync->>Store: Verify account ownership
  Sync->>Sync: Check source gate, semaphore, and cooldown
  Sync->>Store: Mark attempt RUNNING
  Sync->>Registry: Resolve adapter
  Registry->>Adapter: fetchStats(account)
  Adapter->>Provider: Request profile statistics
  Provider-->>Adapter: Response or upstream error
  Adapter-->>Sync: Normalized stats or exception
  alt Valid provider response
    Sync->>Store: Persist success and source-labelled snapshot
  else Provider or validation failure
    Sync->>Store: Persist failure and preserve prior success
  end
  Sync-->>Controller: PlatformSyncResponse
  Controller-->>UI: Updated values and status
```

For expanded notes and source references, see [system design](documentation/diagrams/system-design/README.md), [platform integration](documentation/diagrams/platform-integration/README.md), [database design](documentation/diagrams/database/README.md), and the [UML index](documentation/diagrams/uml/README.md).

## Functional modules

| Module | Main responsibilities | Key code areas |
|---|---|---|
| Authentication and identity | Registration, login, JWT validation, email verification, Google authentication/linking | `backend/src/main/java/com/kodbtw/controller/AuthController.java`, `service/`, `security/`, `config/SecurityConfig.java` |
| Developer profiles | Owner profile management, unique public username, safe public response | `ProfileController.java`, `PublicProfileController.java`, profile services and DTOs |
| Platform accounts and synchronization | Handle management, ownership checks, source selection, sync status, provider error handling | `PlatformAccountController.java`, `PlatformAccountService.java`, `PlatformSyncService.java`, `adapter/` |
| Dashboard and analytics | Summary statistics, difficulty/platform breakdown, history, insights, activity and contest intelligence | `DashboardController.java`, `service/Dashboard*Service.java`, analytics/history/insights DTOs |
| Leaderboard | Global, college, and current-user views; cached ranking projection | `LeaderboardController.java`, `LeaderboardService.java`, `LeaderboardSyncService.java` |
| Contest calendar | Upcoming contest data, local-time display, source and cache status | `ContestCalendarController.java`, `ContestCalendarService.java`, `adapter/contest/` |
| Frontend experience | Protected routes, profile pages, dashboards, responsive UI, theme, loading and error states | `frontend/src/App.tsx`, `frontend/src/pages/`, `frontend/src/components/` |

### Primary user journeys

1. **Create an account:** a visitor submits registration information. The API validates it, stores a BCrypt password hash, and supports the email-verification flow before account use.
2. **Sign in:** the user authenticates with the supported login flow and receives a JWT. Protected API requests carry the token in the authorization header.
3. **Connect and synchronize a handle:** an authenticated user adds a platform account. The API checks ownership, verifies that the source is enabled, applies synchronization controls, maps available provider fields, and records a source-labelled snapshot after a successful sync.
4. **Review progress:** dashboard and analytics endpoints return the user’s stored observations. Missing values remain unavailable, and history reflects recorded snapshots rather than interpolated values.
5. **Share a profile:** a visitor requests a public username. The API returns only fields in the public profile response model.

## Platform integration status

| Platform | Current repository status | Data boundary |
|---|---|---|
| LeetCode | Live profile-statistics adapter | Uses a reachable but undocumented GraphQL endpoint. Only fields present in the response are mapped; the external contract and terms require ongoing review. |
| Codeforces | Live profile adapter using public `user.info`; Codeforces contest provider | The user endpoint does not provide all activity or solved-count fields. Missing values remain null. |
| CodeChef | Adapter uses an unofficial third-party statistics provider | Not an official CodeChef API. Authorization, data rights, provider reliability, and upstream permissions are unresolved. |
| GeeksforGeeks | Adapter/client prototype files exist | Excluded by the normal live-source gate; not enabled for normal statistics synchronization. |
| HackerRank | Adapter/client prototype files exist | Excluded by the normal live-source gate; not enabled for normal statistics synchronization. |

The platform catalog therefore should not be interpreted as five live integrations. Source labels such as `LEETCODE_REAL`, `CODEFORCES_REAL`, and `CODECHEF_THIRD_PARTY` identify the origin path used by the application. See the [data-acquisition notes](docs/platform-data-acquisition.md) and [integration design](documentation/diagrams/platform-integration/README.md).

## Database design

MySQL 8 stores account, profile, platform-account, snapshot, and leaderboard-cache data. Flyway owns schema evolution; Hibernate validates the migrated schema rather than creating it. The [ER figure](documentation/diagrams/assets/entity-relationship-overview.png) gives an overview; migration SQL under `backend/src/main/resources/db/migration/` is the authoritative schema source.

- **`User`:** login identity and account metadata; email is unique, and a password hash may be absent for linked-identity flows.
- **`Profile`:** display name, bio, public username, and indexing preference; one profile per user, with a unique public username.
- **`UserAuthIdentity`:** external authentication identity; provider and subject identify a linked identity.
- **`PlatformAccount`:** a user’s handle on a coding platform; owner-scoped and unique per user/platform pair.
- **`PlatformStatSnapshot`:** dated values from successful synchronization; stores source, observation time, and nullable metrics.
- **`LeaderboardUserCache`:** derived ranking projection; optional one-to-one cache per user.
- **`GoogleAuthChallenge`:** short-lived Google login/link operation state, bound to the operation and, where appropriate, an account.
- **`EmailVerificationToken`:** hashed, expiring verification token associated with a user.

### Migration sequence

| Migration | Schema change |
|---|---|
| V1–V3 | Initial schema, users, profiles, and platform accounts |
| V4–V7 | Leaderboard snapshots, profile usernames, sync status, and separate maximum rating |
| V8–V9 | Opt-in profile search indexing and external authentication identity |
| V10–V11 | Google authentication challenges and operation/user binding |
| V12 | Email verification support |

See the [database design notes](documentation/diagrams/database/README.md) for constraints and the full ER diagram source.

## Security and non-functional considerations

- **Authentication:** stateless bearer JWTs, BCrypt password hashing, and server-side challenge state for Google flows.
- **Authorization:** protected API operations resolve the authenticated user and scope account actions to that owner. The admin-only leaderboard sync is separately guarded.
- **Data exposure:** public profiles use a dedicated response DTO; API errors avoid returning persistence entities.
- **Data integrity:** Flyway migrations define schema changes; nullable statistics preserve missing-field meaning.
- **Resilience:** sync status and provider errors are surfaced; a failed synchronization preserves earlier successful observations.
- **External data:** provider fields, freshness, and live-source availability vary; third-party terms and permissions need separate review.
- **Accessibility and layout:** responsive layouts, theme support, keyboard semantics, and automated markup/contrast checks are present. They do not replace a full assistive-technology audit.

## Team-wise role and contribution focus

The role allocation below is the team-provided focus for explaining the project. It maps each member to a subsystem and its relevant implementation areas; it is not a claim of verified commit-by-commit ownership. The supplied final report says individual module ownership should be confirmed with the team before formal submission.

### 1. Sajal Goyal — Team lead, API, and authentication

**Role focus:** authentication and the backend API boundary.

- Explain registration, login, email verification, and Google sign-in/linking routes.
- Walk through JWT issuance and validation, BCrypt password storage, and protected request flow.
- Describe how controllers delegate to services and how authenticated identity is resolved for owner-scoped actions.
- Relevant areas: `AuthController`, `AuthService`, `UserService`, `security/`, `SecurityConfig`, and authentication DTOs under `backend/src/main/java/com/kodbtw/`.

### 2. Shri Krishna Chandra Vaishnav — Frontend application and user experience

**Role focus:** React routes, pages, and the experience for logged-in users.

- Present the dashboard, analytics, contest, leaderboard, and coding-profile views.
- Explain auth-aware navigation, API client behavior, and loading, error, and empty states.
- Show responsive layout and theme handling across the application.
- Relevant areas: `frontend/src/App.tsx`, `frontend/src/pages/`, `frontend/src/components/`, `frontend/src/context/`, and `frontend/src/services/api/`.

### 3. Yug Mittal — Platform integrations and statistics synchronization

**Role focus:** adapting provider responses into KodBTW’s platform-statistics model.

- Explain the adapter registry and the distinction between enabled integrations and prototype adapters.
- Walk through handle lookup, provider response mapping, source labels, sync statuses, and cooldown behavior.
- Describe why unsupported or missing statistics stay unavailable instead of being filled with estimates.
- Relevant areas: `backend/src/main/java/com/kodbtw/adapter/`, `PlatformAdapterRegistry`, `PlatformSyncService`, and platform-statistics DTOs.

### 4. Ravi Shah — Database, persistence, and analytics

**Role focus:** schema evolution, JPA persistence, historical snapshots, and derived views.

- Explain Flyway migrations and the account/profile/snapshot relationships.
- Describe how dated observations support history and how nullable data is handled by analytics.
- Walk through leaderboard projection and the distinction between stored source observations and derived rankings.
- Relevant areas: `backend/src/main/resources/db/migration/`, `entity/`, `repository/`, analytics services, and leaderboard services.

### 5. Suryadev Singh — Quality, contest calendar, and documentation

**Role focus:** contest data behavior, reliability states, tests, and project documentation.

- Explain the Codeforces contest provider, cache behavior, and the optional local fixture mode.
- Present stale, unavailable, and provider-failure states and how they are communicated to users.
- Describe the backend/frontend validation inventory and the limits of the historical test record.
- Relevant areas: `adapter/contest/`, `ContestCalendarService`, `frontend/tests/`, and `documentation/`.

### Module distribution summary

| Member focus | Main modules | Demonstration artifact |
|---|---|---|
| Sajal Goyal | Identity, authentication, API security | Registration/login and protected API flow |
| Shri Krishna Chandra Vaishnav | Frontend routes, dashboard, and interaction states | Dashboard and analytics screenshots |
| Yug Mittal | Platform adapters and synchronization | Platform status and sync workflow diagrams |
| Ravi Shah | MySQL schema, snapshots, analytics, leaderboard projection | ER diagram and recorded-history views |
| Suryadev Singh | Contest calendar, quality records, and documentation | Contest screenshot, validation record, deliverable index |

Enrollment IDs are copied from the supplied final report; the exact format should be checked with the college before submission. Do not use commit counts alone to assign individual ownership.

## Integration and validation

The project combines frontend routes, authenticated API calls, provider adapters, persistence, and dashboard projections. Important integration boundaries include:

- The frontend and backend agree on authentication, DTO shapes, and error states.
- The sync service enforces account ownership and enabled-source checks before contacting a provider.
- Provider-specific nullable fields remain nullable when converted to normalized statistics and snapshots.
- Dashboard, analytics, public-profile, leaderboard, and contest responses use distinct access and data rules.

The repository contains backend unit/controller/security and migration checks, frontend utility/API tests, and a backend GitHub Actions workflow. See [Testing and Validation](documentation/TESTING_AND_VALIDATION.md) for dated historical results and environment limitations. **Tests were not rerun while updating this README.** Historical results are not a claim about the current deployed site or current working tree.

## Screenshots

These captures were supplied for the project README. Dashboard, analytics, contest, leaderboard, and profile values reflect a point in time and may change. The landing page labels its preview values as illustrative sample data.

### Landing page and product overview

![KodBTW landing page hero and sample overview](documentation/screenshots/landing-hero-and-overview.png)

![KodBTW illustrative product showcase](documentation/screenshots/landing-product-showcase.png)

![KodBTW profile preview and product modules](documentation/screenshots/landing-profile-and-product-modules.png)

### Developer dashboard

![KodBTW dashboard with connected profiles and statistics](documentation/screenshots/dashboard.png)

### Analytics and insights

![Analytics overview and platform share](documentation/screenshots/analytics-overview.png)

![Platform comparisons and insights](documentation/screenshots/analytics-comparisons.png)

![Progress history and activity availability](documentation/screenshots/analytics-history.png)

### Contest calendar

![KodBTW upcoming contests and platform contest standings](documentation/screenshots/contests.png)

### Leaderboard

![KodBTW leaderboard with filters and rankings](documentation/screenshots/leaderboard.png)

### Coding profiles

![Connected coding profiles and synchronization status](documentation/screenshots/coding-profiles.png)

See the [screenshot index](documentation/screenshots/INDEX.md) for the capture list and limitations.

## Repository structure

```text
KodBTW/
├── backend/
│   └── src/
│       ├── main/java/com/kodbtw/       # Controllers, services, adapters, entities, DTOs
│       ├── main/resources/db/migration/ # Flyway schema history, V1–V12
│       └── test/                       # Backend automated tests and fixtures
├── frontend/
│   ├── public/                         # Static assets and SPA metadata
│   ├── src/                            # React pages, components, API clients
│   └── tests/                          # Node.js test suite
├── documentation/
│   ├── diagrams/                       # Architecture, ER, UML, sync diagrams
│   ├── project-deliverables/           # Report, SRS, paper, and presentation
│   ├── screenshots/                    # README screenshots and index
│   └── weekly-reports/                 # Progress-report index
├── docs/                               # Technical notes and data-source research
└── .github/workflows/                  # Backend CI workflow
```

## Run locally

### Prerequisites

- Java 21
- MySQL 8
- Node.js 18+ and npm
- Docker for the Testcontainers-backed database tests

Create a local development database:

```sql
CREATE DATABASE kodbtw CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
```

Set backend values in the process environment or a secret manager. `.env.example` is a reference, not an automatically loaded Spring configuration. Use local development credentials and a fresh JWT secret; do not commit populated environment files.

Example PowerShell configuration for a local MySQL service:

```powershell
$env:DB_URL = "jdbc:mysql://127.0.0.1:3306/kodbtw?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME = Read-Host "Local MySQL username"
$secureDbPassword = Read-Host "Local MySQL password" -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new("", $secureDbPassword).Password
$secretBytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Fill($secretBytes)
$env:JWT_SECRET = [Convert]::ToBase64String($secretBytes)
$env:CORS_ALLOWED_ORIGINS = "http://localhost:5173,http://127.0.0.1:5173"
```

Start the backend from `backend/`:

```powershell
.\mvnw.cmd spring-boot:run
```

Start the frontend from `frontend/` in another terminal:

```powershell
npm ci
npm run dev
```

The frontend uses Vite on port `5173` and proxies `/api` to the backend on port `8080`. Flyway applies migrations V1–V12; Hibernate validates the migrated schema. The default contest provider is Codeforces. The local fixture option is documented in [`docs/contest-calendar-fixture-mode.md`](docs/contest-calendar-fixture-mode.md).

## API overview

All API routes use the `/api` prefix. Routes described as protected require a bearer token. Controller annotations are the definitive route source.

| Method and route | Access | Purpose |
|---|---|---|
| `POST /api/auth/register` | Public | Create a KodBTW account |
| `POST /api/auth/verify-email` | Public | Verify an email address with a token |
| `POST /api/auth/resend-verification` | Public | Request another verification message |
| `POST /api/auth/login` | Public | Authenticate with account credentials |
| `POST /api/auth/google` | Public | Google sign-in flow |
| `POST /api/auth/google/challenge` | Public | Begin Google sign-in challenge flow |
| `POST /api/auth/google/link/challenge` | Protected | Begin account-linking challenge |
| `POST /api/auth/google/link` | Protected | Link a Google identity |
| `GET /api/auth/google/identity` | Protected | Read linked identity status |
| `GET /api/auth/me` | Protected | Read current authenticated user |
| `GET /api/health` | Public | Liveness endpoint; not a database-readiness check |
| `GET /api/profile`, `PUT /api/profile` | Protected | Read or update the current user's profile |
| `GET /api/public/profiles/{username}` | Public | Read a sanitized public profile |
| `/api/platform-accounts/**` | Protected | Create, list, inspect, update, delete, and sync owned handles |
| `/api/dashboard/{stats,analytics,history,insights,activity,contests}` | Protected | Read dashboard and history data |
| `GET /api/contests` | Protected | Read upcoming contest information |
| `GET /api/leaderboard`, `/college`, `/me` | Protected | Read leaderboard views |
| `POST /api/leaderboard/sync` | Admin | Rebuild the cached leaderboard projection |

See `backend/src/main/java/com/kodbtw/controller/` for request and response details.

## Project documents & deliverables

| Deliverable | File or link |
|---|---|
| Software Requirements Specification | [Supplied SRS PDF](documentation/project-deliverables/KodBTW_SRS_Supplied.pdf) · [Repository SRS PDF](documentation/project-deliverables/KodBTW_SRS.pdf) |
| Final project report | [Final report PDF](documentation/project-deliverables/KodBTW_Final_Project_Report_Explained_Review.pdf) · [Editable report DOCX](documentation/project-deliverables/KodBTW_Project_Report_Source.docx) |
| Research paper | [Research paper PDF](documentation/project-deliverables/KodBTW_Research_Paper.pdf) |
| Literature review | [Literature review PDF](documentation/project-deliverables/KodBTW_Literature_Review.pdf) |
| Project presentation | [Presentation PDF](documentation/project-deliverables/KodBTW_Project_Presentation.pdf) · **PPT/Slides URL:** `PASTE_PRESENTATION_LINK_HERE` |
| Project demonstration | **Video URL:** `PASTE_VIDEO_LINK_HERE` |
| Weekly progress reports | [Weekly report index](documentation/weekly-reports/INDEX.md) |
| Architecture and UML | [Architecture](documentation/diagrams/architecture/SYSTEM_ARCHITECTURE.md) · [UML index](documentation/diagrams/uml/README.md) · [Database diagrams](documentation/diagrams/database/README.md) |
| Platform integration notes | [Integration diagrams and status](documentation/diagrams/platform-integration/README.md) · [Data acquisition record](docs/platform-data-acquisition.md) |
| Testing and validation | [Validation record](documentation/TESTING_AND_VALIDATION.md) |

## Limitations and future work

- Platform endpoints and response schemas can change. LeetCode uses an undocumented query; CodeChef uses an unofficial provider with unresolved permission and reliability questions.
- GeeksforGeeks and HackerRank prototype files are not enabled as normal live statistics sources.
- Available metrics differ across platforms and should not be compared as if they shared one rating scale. Missing observations are not zero.
- A successful build or automated test does not establish production availability, data accuracy, provider authorization, privacy compliance, or browser/device coverage.
- Contest fixtures are local illustrative data. They should not be presented as live contest listings.
- Future work may include approved provider interfaces, additional accessibility and browser testing, clearer operations documentation, and a separately evaluated campus analytics feature.

## License

No root-level license file is present in this checkout. Reuse and redistribution terms have not been stated; confirm the intended license with the project owners.

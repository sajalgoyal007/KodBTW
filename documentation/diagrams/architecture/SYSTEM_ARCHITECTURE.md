# System architecture

This editable Mermaid diagram follows the current repository code. Solid paths represent configured application relationships; dashed paths show link-only or optional/local behavior.

```mermaid
flowchart LR
  U[Developer in browser] -->|HTTPS / React Router| FE[React 18 + TypeScript SPA]
  FE -->|JSON REST; bearer JWT for protected routes| API[Spring Boot REST API]
  API --> SEC[Spring Security + JWT filter]
  API --> C[Controllers]
  C --> S[Application services]
  S --> R[Spring Data JPA repositories]
  R --> DB[(MySQL 8)]
  S --> AR[PlatformAdapterRegistry]
  AR --> LC[LeetCode adapter/client]
  AR --> CF[Codeforces adapter/client]
  AR --> CC[CodeChef adapter/client]
  AR --> AC[AtCoder adapter/client]
  AR --> GF[GeeksforGeeks adapter/client]
  AR --> C360[Code360 adapter/client]
  AR --> HR[HackerRank adapter/client]
  AR --> IB[InterviewBit adapter/client]
  AR --> HE[HackerEarth adapter/client]
  AR -. link-only .-> AM[AlgoMaster adapter]
  LC -->|Undocumented public GraphQL query| LEET[LeetCode]
  CF -->|Public user.info API| CODEFORCES[Codeforces]
  CC -->|Unofficial third-party stats provider| CCAPI[CodeChef stats provider]
  AC -->|Public history + Kenkoooo API| ATC[AtCoder]
  GF -->|Unofficial third-party stats provider| GFAPI[GFG stats provider]
  C360 -->|Public profile page| C360P[Code360]
  HR -->|Public REST endpoints| HRP[HackerRank]
  IB -->|Public profile page| IBP[InterviewBit]
  HE -->|Public profile page| HEP[HackerEarth]
  S --> CAL[Contest calendar service]
  CAL --> CFP[Codeforces contest provider]
  CAL -. local config only .-> FIX[Fixture contest provider]
  CFP --> CODEFORCES
```

## Responsibilities and data flow

- The React/Vite frontend renders public and authenticated routes and sends API requests. The development Vite proxy forwards `/api` to the local backend; production API origin is selected by build configuration or same-origin hosting.
- Spring controllers validate/map HTTP requests and delegate to services. Spring Security is stateless; the JWT filter validates bearer tokens, looks up the user, and protected services/controllers use that identity for owner-scoped operations.
- Services coordinate domain behavior, including profiles, platform account ownership, sync status, analytics, snapshots, public-profile DTOs, and leaderboard cache rebuilding.
- `PlatformAdapterRegistry` resolves adapter implementations. Supported sync results are normalized and persisted with source labels. Unsupported platform values are returned as pending/unavailable instead of being fabricated.
- Repositories persist data via JPA to MySQL. Flyway owns DDL and Hibernate uses `ddl-auto: validate`.
- CodeChef and GeeksforGeeks use an unofficial third-party provider and have unresolved permission/reliability concerns. AtCoder uses public AtCoder history plus Kenkoooo's public API; HackerRank uses public REST endpoints; Code360, InterviewBit, and HackerEarth parse public profile pages. AlgoMaster is link-only and fetches no statistics.
- Repository deployment artifacts include a Vite SPA build/Vercel rewrite and backend Dockerfile. They describe packaging/configuration; the repository does not verify a live hosting account or complete infrastructure topology.

## Source references

`frontend/src/App.tsx`; `backend/src/main/java/com/kodbtw/config/SecurityConfig.java`; `security/JwtAuthenticationFilter.java`; `controller/`; `service/`; `adapter/PlatformAdapter.java`; `adapter/PlatformAdapterRegistry.java`; `service/PlatformSyncService.java`; `entity/`; Flyway migrations under `backend/src/main/resources/db/migration/`.

# System-design diagrams

The diagrams are authored as Mermaid in Markdown so their source remains editable and GitHub can render them. No separately exported PNG/SVG is checked in; visual export can be added when a renderer is available and the diagrams are reviewed.

## Data-flow diagram

```mermaid
flowchart LR
  User[External entity: developer] -->|credentials / profile handle| Web[Process: React frontend]
  Web -->|HTTPS JSON; bearer token when authenticated| Auth[Process: Spring Security and REST controllers]
  Auth -->|validated user context| Services[Process: application services]
  Services -->|read/write owner-scoped records and snapshots| Store[(Data store: MySQL)]
  Services -->|public profile lookup| PublicAPI[External entity: public profile API consumer]
  Services -->|profile request| Platform[External entities: platform/provider APIs]
  Platform -->|response or error| Services
  Services -->|sanitized DTO / status| Web
  Web -->|rendered view| User
```

**Boundary:** profile API consumers can request only a public username route. Private dashboard/profile/account methods require authentication. The public response is assembled by `PublicProfileService`; it is not a raw `User` entity export.

## Authentication sequence

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
  Users-->>Auth: User record or absent
  Auth->>Auth: BCrypt password verification
  Auth->>JWT: Generate signed token with user id subject and expiry
  JWT-->>Auth: JWT
  Auth-->>FE: LoginResponse (JWT and UserResponse)
  FE-->>Browser: Store session and navigate
  Browser->>FE: Request protected data
  FE->>JWT: API request with Authorization: Bearer token
  JWT->>JWT: Verify signature and expiration
  JWT->>Users: Resolve subject user id
  JWT-->>FE: Authenticated request proceeds, or 401/403
```

Registration uses `POST /api/auth/register`, validates input, hashes the password, persists the user, and returns a user response. Profile data is managed separately through `/api/profile`. See `AuthController`, `UserService`, `AuthService`, `JwtService`, `JwtAuthenticationFilter`, and `SecurityConfig` for exact behavior.

## Platform sync activity

```mermaid
flowchart TD
  Start[Authenticated user requests sync] --> Own{Account belongs to user?}
  Own -->|No| NotFound[Return not found / deny access]
  Own -->|Yes| Supported{Platform has live stats source?}
  Supported -->|No| Pending[Return current pending/unavailable status]
  Supported -->|Yes| Gate{Sync gate acquired?}
  Gate -->|No| Current[Return current status]
  Gate -->|Yes| Cooldown{Outside cooldown or already running?}
  Cooldown -->|No| Current
  Cooldown -->|Yes| StartRecord[Persist RUNNING attempt]
  StartRecord --> Fetch[Adapter client fetches and validates response]
  Fetch -->|Valid supported fields| Success[Persist success status and source-labelled snapshot]
  Fetch -->|Not found, timeout, upstream or invalid response| Failure[Persist classified failure; preserve prior successful metrics]
  Success --> Done[Return sync response]
  Failure --> Done
  Pending --> Done
  Current --> Done
  NotFound --> Done
```

The normal source gate currently enables LeetCode, Codeforces, and CodeChef. CodeChef uses an unofficial provider. Local GFG/HackerRank adapter clients are not invoked by normal sync. A global sync semaphore, per-account cooldown, Codeforces call spacing, status persistence, and no-mock-fallback behavior are implemented in `PlatformSyncService` and `PlatformSyncPersistenceService`.

## Deployment/configuration view

```mermaid
flowchart LR
  Dev[Developer / CI] -->|npm run build| Static[Frontend dist/]
  Static -. configured SPA hosting example .-> Vercel[Vercel project configuration]
  Dev -->|Docker build| Image[Backend Java 21 container]
  Image -. requires separately provisioned host .-> Host[Java container host]
  Host -->|DB_URL and credentials injected outside image| MySQL[(Managed or local MySQL 8)]
  Static -->|VITE_API_BASE_URL or same-origin /api| Host
```

This is a deployment-capability diagram, not proof of current infrastructure. The Vercel rewrite config and Dockerfile are checked in; hosting accounts, database service configuration, secrets, and production deployment state are not defined by those files. See the root README deployment notes and `frontend/vercel.json` / `backend/Dockerfile`.

# UML diagrams

These source diagrams use Mermaid and are rendered by GitHub when viewed as Markdown. They model the checked-out code paths; third-party permission and deployment assumptions are not modeled as facts.

## Use cases

```mermaid
flowchart LR
  Visitor([Visitor]) --> ViewLanding[View product landing page]
  Visitor --> Register[Register account]
  Visitor --> Login[Log in]
  Visitor --> PublicProfile[View public developer profile]
  User([Authenticated developer]) --> EditProfile[Read or update own profile]
  User --> LinkAccount[Manage own platform accounts]
  User --> Sync[Request supported platform sync]
  User --> Dashboard[View dashboard, analytics, history, insights]
  User --> Calendar[View contest calendar]
  User --> Leaderboard[View leaderboard and own rank]
  Admin([ADMIN authority]) --> Trigger[Trigger leaderboard cache sync]
```

## Component and domain class view

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
    String name
    String email
    String password
  }
  class Profile {
    Long id
    String username
    String displayName
    String bio
    Integer graduationYear
  }
  class PlatformAccount {
    Long id
    Platform platform
    String username
    SyncStatus syncStatus
    +getOwnedAccount(userId, accountId)
  }
  class PlatformStatSnapshot {
    Long id
    String platform
    LocalDate snapshotDate
    Integer rating
    Integer maxRating
    Integer rank
    String source
  }
  class LeaderboardUserCache {
    Long userId
    Double weightedScore
    Boolean realDataOnly
    Boolean hasMockData
  }
  class AuthService {
    +login(LoginRequest) LoginResponse
  }
  class UserService {
    +register(RegisterRequest) UserResponse
  }
  class PlatformAccountService {
    +create(userId, request)
    +getAll(userId)
  }
  class PlatformSyncService {
    +syncOwnedAccount(userId, accountId) PlatformSyncResponse
    +syncAllAccounts()
  }
  class PublicProfileService {
    +getPublicProfile(username) PublicProfileResponse
  }
  class PlatformStats
  class Platform
  AuthController --> AuthService
  AuthController --> UserService
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
  PlatformAccount --> Platform
  Profile --> User
  PlatformAccount --> User
  PlatformStatSnapshot --> User
  LeaderboardUserCache --> User
```

Adapter class presence does not mean its platform is enabled for regular sync: GFG and HackerRank are excluded by `Platform.hasLiveStatsSource()` in the current working tree.

## Authentication sequence

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
  Service->>Service: BCrypt matches submitted password
  Service->>JWT: generateToken(user)
  JWT-->>Service: Signed JWT
  Service-->>API: LoginResponse
  API-->>UI: JWT and user response
  UI-->>User: Authenticated session
  User->>UI: Open protected page
  UI->>API: Request with Bearer JWT
  API->>JWT: JwtAuthenticationFilter validates claims
  JWT->>DB: Load subject user id
  API-->>UI: Continue or return unauthorized
```

## Platform sync sequence

```mermaid
sequenceDiagram
  actor User
  participant UI as CodingProfilesPage / hook
  participant Controller as PlatformAccountController
  participant Sync as PlatformSyncService
  participant Registry as PlatformAdapterRegistry
  participant Adapter as Enabled PlatformAdapter
  participant Provider as Platform or provider API
  participant Store as PlatformSyncPersistenceService / MySQL
  User->>UI: Request refresh for account
  UI->>Controller: POST /api/platform-accounts/{id}/sync
  Controller->>Sync: syncOwnedAccount(userId, accountId)
  Sync->>Store: Verify account ownership
  Sync->>Sync: Check live-source gate, semaphore and cooldown
  Sync->>Store: Mark attempt RUNNING
  Sync->>Registry: Resolve platform adapter
  Registry->>Adapter: fetchStats(account)
  Adapter->>Provider: Request profile statistics
  Provider-->>Adapter: Response or upstream error
  Adapter-->>Sync: Normalized PlatformStats or exception
  alt Valid response
    Sync->>Store: Persist successful status and source-labelled stats
  else Failure
    Sync->>Store: Persist classified failure; preserve prior success
  end
  Sync-->>Controller: PlatformSyncResponse
  Controller-->>UI: Updated status/statistics
```

The source gate can return pending status before registry lookup for disabled sources. Account ownership is checked before sync. The response/error paths are implemented in `PlatformAccountController`, `PlatformSyncService`, and `PlatformSyncPersistenceService`.

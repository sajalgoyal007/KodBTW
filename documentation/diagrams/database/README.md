# Database schema and ER diagram

MySQL is the configured runtime database. Flyway migrations V1–V7 define the schema; Hibernate is configured with `ddl-auto: validate`. Column details below summarize migrations and JPA entities; the SQL migration files are authoritative.

## Entity relationship diagram

```mermaid
erDiagram
  USERS ||--o| PROFILES : has
  USERS ||--o{ PLATFORM_ACCOUNTS : connects
  USERS ||--o{ PLATFORM_STAT_SNAPSHOTS : records
  USERS ||--o| LEADERBOARD_USER_CACHE : projects
  USERS {
    bigint id PK
    varchar name
    varchar email UK
    varchar password
    timestamp created_at
    timestamp updated_at
  }
  PROFILES {
    bigint id PK
    bigint user_id FK_UK
    varchar username UK
    varchar display_name
    text bio
    varchar avatar_url
    varchar college
    int graduation_year
    varchar location
    varchar github_url
    varchar linkedin_url
    varchar portfolio_url
    timestamp created_at
    timestamp updated_at
  }
  PLATFORM_ACCOUNTS {
    bigint id PK
    bigint user_id FK
    varchar platform
    varchar username
    varchar profile_url
    boolean verified
    varchar sync_status
    timestamp last_attempt_at
    timestamp last_success_at
    timestamp last_failure_at
    varchar last_sync_error_category
    varchar last_sync_error_message
    timestamp connected_at
    timestamp updated_at
  }
  PLATFORM_STAT_SNAPSHOTS {
    bigint id PK
    bigint user_id FK
    varchar platform
    date snapshot_date
    int total_solved
    int easy_solved
    int medium_solved
    int hard_solved
    int rating
    int max_rating
    int platform_rank
    int contests
    int current_streak
    int longest_streak
    varchar source
    timestamp stats_last_synced_at
    timestamp snapshotted_at
  }
  LEADERBOARD_USER_CACHE {
    bigint user_id PK_FK
    varchar display_name
    varchar college
    int total_solved
    double weighted_score
    int best_rating
    varchar best_rating_platform
    int total_contests
    boolean real_data_only
    boolean has_mock_data
    timestamp last_synced_at
  }
```

`schema_initialization_check` is a standalone bootstrap table created by V1; it has no foreign-key relationship to the application entities and is omitted from the relationship graph for readability.

## Keys, constraints, and migration notes

| Table | Keys / constraints | Relationship and notes |
|---|---|---|
| `users` | `id` primary key; `email` unique and non-null | Root account record; password value is produced through the configured password encoder |
| `profiles` | `id` primary key; `user_id` unique, non-null FK; `username` unique, non-null after V5 | One profile per user; FK cascades on user deletion |
| `platform_accounts` | `id` primary key; unique `(user_id, platform)`; FK on `user_id` | Many account rows per user; cascading deletion; V6 adds sync lifecycle fields |
| `platform_stat_snapshots` | `id` primary key; unique `(user_id, platform, snapshot_date)`; FK on `user_id` | Multiple date/platform rows per user; cascading deletion; V6/V7 add observation and max-rating fields |
| `leaderboard_user_cache` | `user_id` is both PK and FK to `users.id` | Optional one-to-one denormalized leaderboard projection; cascading deletion |
| `schema_initialization_check` | `id` primary key | V1 initialization marker table; no application entity relation |

Nullable metrics are intentionally retained when a provider does not report them; absent values are not equivalent to zero. `source` labels distinguish verified platform responses, third-party CodeChef data, and mock/test sources.

## Migration sequence

| Migration | Effect |
|---|---|
| V1 | Creates and seeds `schema_initialization_check` |
| V2 | Creates `users` |
| V3 | Creates `profiles` and `platform_accounts` |
| V4 | Creates `platform_stat_snapshots` and `leaderboard_user_cache` |
| V5 | Adds/backfills/enforces unique non-null profile username |
| V6 | Adds account sync lifecycle fields and snapshot rank/streak/observation fields; backfills only supported existing observations |
| V7 | Separates `max_rating` from numeric `platform_rank` for Codeforces snapshots |

## Source references

- DDL: `backend/src/main/resources/db/migration/`
- Entities: `backend/src/main/java/com/kodbtw/entity/`
- Repositories: `backend/src/main/java/com/kodbtw/repository/`
- Migration tests: `backend/src/test/java/com/kodbtw/migration/`

-- KodBTW Phase 8C: Leaderboard snapshot persistence and ranking cache

-- Table 1: Latest snapshot of platform stats per user per platform.
-- Supports INSERT ... ON DUPLICATE KEY UPDATE for upsert semantics.
-- source column gates real-only vs all-data filtering.
CREATE TABLE platform_stat_snapshots (
    id             BIGINT       AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT       NOT NULL,
    platform       VARCHAR(50)  NOT NULL,
    snapshot_date  DATE         NOT NULL,
    total_solved   INT          NULL,
    easy_solved    INT          NULL,
    medium_solved  INT          NULL,
    hard_solved    INT          NULL,
    rating         INT          NULL,
    contests       INT          NULL,
    current_streak INT          NULL,
    source         VARCHAR(30)  NOT NULL,
    snapshotted_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_snapshots_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_platform_snapshot_date (user_id, platform, snapshot_date),
    INDEX idx_snapshots_user     (user_id),
    INDEX idx_snapshots_platform (platform),
    INDEX idx_snapshots_date     (snapshot_date),
    INDEX idx_snapshots_source   (source)
);

-- Table 2: Denormalized per-user leaderboard cache.
-- Rebuilt by LeaderboardSyncService on each sync cycle.
-- Indexed for efficient paginated sorts across all users.
CREATE TABLE leaderboard_user_cache (
    user_id              BIGINT       NOT NULL PRIMARY KEY,
    display_name         VARCHAR(100) NULL,
    college              VARCHAR(255) NULL,
    total_solved         INT          NOT NULL DEFAULT 0,
    weighted_score       DOUBLE       NOT NULL DEFAULT 0.0,
    best_rating          INT          NULL,
    best_rating_platform VARCHAR(50)  NULL,
    total_contests       INT          NOT NULL DEFAULT 0,
    real_data_only       BOOLEAN      NOT NULL DEFAULT FALSE,
    has_mock_data        BOOLEAN      NOT NULL DEFAULT FALSE,
    last_synced_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_cache_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_cache_college       (college),
    INDEX idx_cache_total_solved  (total_solved DESC),
    INDEX idx_cache_score         (weighted_score DESC),
    INDEX idx_cache_rating        (best_rating DESC)
);

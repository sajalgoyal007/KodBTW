-- Phase 11A: per-account sync lifecycle and complete persisted current stats.
-- Existing snapshot rows are preserved; nullable columns are not backfilled.
ALTER TABLE platform_accounts
    ADD COLUMN sync_status VARCHAR(20) NOT NULL DEFAULT 'NEVER_SYNCED',
    ADD COLUMN last_attempt_at TIMESTAMP NULL,
    ADD COLUMN last_success_at TIMESTAMP NULL,
    ADD COLUMN last_failure_at TIMESTAMP NULL,
    ADD COLUMN last_sync_error_category VARCHAR(40) NULL,
    ADD COLUMN last_sync_error_message VARCHAR(160) NULL;

ALTER TABLE platform_stat_snapshots
    ADD COLUMN platform_rank INT NULL,
    ADD COLUMN longest_streak INT NULL,
    ADD COLUMN stats_last_synced_at TIMESTAMP NULL;

-- Mark an account as previously synced only when an actual earlier snapshot exists.
-- This reuses recorded sync timestamps and does not synthesize statistic history.
UPDATE platform_accounts pa
JOIN (
    SELECT user_id, platform, MAX(snapshot_date) AS snapshot_date
    FROM platform_stat_snapshots
    GROUP BY user_id, platform
) latest
    ON latest.user_id = pa.user_id AND latest.platform = pa.platform
JOIN platform_stat_snapshots ps
    ON ps.user_id = latest.user_id
   AND ps.platform = latest.platform
   AND ps.snapshot_date = latest.snapshot_date
SET pa.sync_status = 'SUCCEEDED',
    pa.last_attempt_at = ps.snapshotted_at,
    pa.last_success_at = ps.snapshotted_at;

-- REAL rows already carry their actual snapshot observation time. MOCK rows keep
-- lastSyncedAt null, matching their existing source semantics.
UPDATE platform_stat_snapshots
SET stats_last_synced_at = snapshotted_at
WHERE RIGHT(source, 5) = '_REAL';


ALTER TABLE platform_stat_snapshots
    ADD COLUMN max_rating INT NULL;

UPDATE platform_stat_snapshots
SET max_rating = platform_rank,
    platform_rank = NULL
WHERE platform = 'CODEFORCES'
  AND source = 'CODEFORCES_REAL'
  AND platform_rank IS NOT NULL;

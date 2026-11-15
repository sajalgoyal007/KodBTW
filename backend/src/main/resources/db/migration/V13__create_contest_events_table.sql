-- KodBTW Phase 5: Multi-Platform Contest Calendar
-- Stores normalized public contest events synced from clist.by (or the Codeforces
-- public API fallback) so the monthly calendar can be served without hitting
-- third-party APIs on every page load.

CREATE TABLE contest_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    external_id VARCHAR(128) NOT NULL,
    platform VARCHAR(40) NOT NULL,
    title VARCHAR(500) NOT NULL,
    start_time_utc TIMESTAMP NOT NULL,
    end_time_utc TIMESTAMP NOT NULL,
    duration_seconds BIGINT NOT NULL,
    contest_url VARCHAR(1000) NOT NULL,
    last_synced_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_contest_events_external_id UNIQUE (external_id)
);

CREATE INDEX idx_contest_events_start_time_platform ON contest_events (start_time_utc, platform);

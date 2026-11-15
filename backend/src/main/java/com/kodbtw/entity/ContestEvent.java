package com.kodbtw.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Normalized public contest event, stored in UTC wall-clock time.
 *
 * <p>Rows are synced from clist.by (all supported platforms) or the public
 * Codeforces API (fallback) and are idempotent by {@code externalId}.</p>
 */
@Entity
@Table(
        name = "contest_events",
        indexes = {
                @Index(name = "idx_contest_events_start_time_platform",
                        columnList = "start_time_utc, platform")
        }
)
public class ContestEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false, unique = true, length = 128)
    private String externalId;

    @Column(name = "platform", nullable = false, length = 40)
    private String platform;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "start_time_utc", nullable = false)
    private LocalDateTime startTimeUtc;

    @Column(name = "end_time_utc", nullable = false)
    private LocalDateTime endTimeUtc;

    @Column(name = "duration_seconds", nullable = false)
    private long durationSeconds;

    @Column(name = "contest_url", nullable = false, length = 1000)
    private String contestUrl;

    @Column(name = "last_synced_at", nullable = false)
    private LocalDateTime lastSyncedAt;

    public ContestEvent() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getStartTimeUtc() {
        return startTimeUtc;
    }

    public void setStartTimeUtc(LocalDateTime startTimeUtc) {
        this.startTimeUtc = startTimeUtc;
    }

    public LocalDateTime getEndTimeUtc() {
        return endTimeUtc;
    }

    public void setEndTimeUtc(LocalDateTime endTimeUtc) {
        this.endTimeUtc = endTimeUtc;
    }

    public long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getContestUrl() {
        return contestUrl;
    }

    public void setContestUrl(String contestUrl) {
        this.contestUrl = contestUrl;
    }

    public LocalDateTime getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(LocalDateTime lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }
}

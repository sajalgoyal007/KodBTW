package com.kodbtw.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Stores a historical snapshot of a user's platform statistics for one platform on a specific date.
 * Used as the canonical historical record for leaderboard computation and future time-series tracking.
 */
@Entity
@Table(
        name = "platform_stat_snapshots",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_platform_snapshot_date",
                columnNames = {"user_id", "platform", "snapshot_date"}
        )
)
public class PlatformStatSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String platform;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "total_solved")
    private Integer totalSolved;

    @Column(name = "easy_solved")
    private Integer easySolved;

    @Column(name = "medium_solved")
    private Integer mediumSolved;

    @Column(name = "hard_solved")
    private Integer hardSolved;

    private Integer rating;

    @Column(name = "platform_rank")
    private Integer rank;

    private Integer contests;

    @Column(name = "current_streak")
    private Integer currentStreak;

    @Column(name = "longest_streak")
    private Integer longestStreak;

    @Column(name = "stats_last_synced_at")
    private LocalDateTime statsLastSyncedAt;

    /**
     * Source tag from the platform adapter: e.g. "LEETCODE_REAL", "CODEFORCES_REAL", "MOCK".
     * This field is the gate for real-only vs all-data leaderboard filtering.
     */
    @Column(nullable = false, length = 30)
    private String source;

    @Column(name = "snapshotted_at", nullable = false)
    private LocalDateTime snapshottedAt;

    public PlatformStatSnapshot() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.snapshotDate == null) {
            this.snapshotDate = LocalDate.now();
        }
        this.snapshottedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.snapshottedAt = LocalDateTime.now();
    }

    // ── Getters and Setters ───────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public LocalDate getSnapshotDate() { return snapshotDate; }
    public void setSnapshotDate(LocalDate snapshotDate) { this.snapshotDate = snapshotDate; }

    public Integer getTotalSolved() { return totalSolved; }
    public void setTotalSolved(Integer totalSolved) { this.totalSolved = totalSolved; }

    public Integer getEasySolved() { return easySolved; }
    public void setEasySolved(Integer easySolved) { this.easySolved = easySolved; }

    public Integer getMediumSolved() { return mediumSolved; }
    public void setMediumSolved(Integer mediumSolved) { this.mediumSolved = mediumSolved; }

    public Integer getHardSolved() { return hardSolved; }
    public void setHardSolved(Integer hardSolved) { this.hardSolved = hardSolved; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }

    public Integer getContests() { return contests; }
    public void setContests(Integer contests) { this.contests = contests; }

    public Integer getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(Integer currentStreak) { this.currentStreak = currentStreak; }

    public Integer getLongestStreak() { return longestStreak; }
    public void setLongestStreak(Integer longestStreak) { this.longestStreak = longestStreak; }

    public LocalDateTime getStatsLastSyncedAt() { return statsLastSyncedAt; }
    public void setStatsLastSyncedAt(LocalDateTime statsLastSyncedAt) { this.statsLastSyncedAt = statsLastSyncedAt; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public LocalDateTime getSnapshottedAt() { return snapshottedAt; }
    public void setSnapshottedAt(LocalDateTime snapshottedAt) { this.snapshottedAt = snapshottedAt; }
}

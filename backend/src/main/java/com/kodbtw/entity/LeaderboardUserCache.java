package com.kodbtw.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

import org.springframework.data.domain.Persistable;
import jakarta.persistence.Transient;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;

/**
 * Denormalized per-user aggregate for fast leaderboard reads.
 * Rebuilt by LeaderboardSyncService on each sync cycle.
 *
 * <p>Uses user_id as the primary key (shared with users table via @MapsId).
 * Each sync cycle performs an upsert — this is a single-row-per-user projection
 * over the full platform_stat_snapshots for that user.
 *
 * <p>Indexed for paginated leaderboard sorting by weighted_score, total_solved, best_rating,
 * and college filtering.
 */
@Entity
@Table(name = "leaderboard_user_cache")
public class LeaderboardUserCache implements Persistable<Long> {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(length = 255)
    private String college;

    @Column(name = "total_solved", nullable = false)
    private Integer totalSolved = 0;

    @Column(name = "weighted_score", nullable = false)
    private Double weightedScore = 0.0;

    @Column(name = "best_rating")
    private Integer bestRating;

    @Column(name = "best_rating_platform", length = 50)
    private String bestRatingPlatform;

    @Column(name = "total_contests", nullable = false)
    private Integer totalContests = 0;

    /**
     * True if ALL of this user's platform snapshots have source != 'MOCK'.
     */
    @Column(name = "real_data_only", nullable = false)
    private Boolean realDataOnly = false;

    /**
     * True if ANY of this user's platform snapshots have source == 'MOCK'.
     */
    @Column(name = "has_mock_data", nullable = false)
    private Boolean hasMockData = false;

    @Column(name = "last_synced_at", nullable = false)
    private LocalDateTime lastSyncedAt;

    @Transient
    private boolean isNewEntity = true;

    public LeaderboardUserCache() {
    }

    @Override
    public Long getId() {
        return userId;
    }

    @Override
    public boolean isNew() {
        return isNewEntity;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNewEntity = false;
    }

    @PrePersist
    protected void onCreate() {
        this.lastSyncedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastSyncedAt = LocalDateTime.now();
    }

    // ── Getters and Setters ───────────────────────────────────────────────────

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public User getUser() { return user; }
    public void setUser(User user) {
        this.user = user;
        if (user != null && user.getId() != null) {
            this.userId = user.getId();
        }
    }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }

    public Integer getTotalSolved() { return totalSolved; }
    public void setTotalSolved(Integer totalSolved) { this.totalSolved = totalSolved; }

    public Double getWeightedScore() { return weightedScore; }
    public void setWeightedScore(Double weightedScore) { this.weightedScore = weightedScore; }

    public Integer getBestRating() { return bestRating; }
    public void setBestRating(Integer bestRating) { this.bestRating = bestRating; }

    public String getBestRatingPlatform() { return bestRatingPlatform; }
    public void setBestRatingPlatform(String bestRatingPlatform) { this.bestRatingPlatform = bestRatingPlatform; }

    public Integer getTotalContests() { return totalContests; }
    public void setTotalContests(Integer totalContests) { this.totalContests = totalContests; }

    public Boolean getRealDataOnly() { return realDataOnly; }
    public void setRealDataOnly(Boolean realDataOnly) { this.realDataOnly = realDataOnly; }

    public Boolean getHasMockData() { return hasMockData; }
    public void setHasMockData(Boolean hasMockData) { this.hasMockData = hasMockData; }

    public LocalDateTime getLastSyncedAt() { return lastSyncedAt; }
    public void setLastSyncedAt(LocalDateTime lastSyncedAt) { this.lastSyncedAt = lastSyncedAt; }
}

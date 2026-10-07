package com.kodbtw.dto.leaderboard;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Current user's personal leaderboard rank and metrics.
 */
public class MyRankResponse {

    private final Long userId;
    private final String displayName;

    /** Global rank among real-data-only users. Null if user has no real data or has not been synced. */
    private final Long globalRank;

    /** College rank. Null if no college is set or user has no real data. */
    private final Long collegeRank;

    private final String college;
    private final Integer totalSolved;
    private final Double weightedScore;
    private final Integer bestRating;
    private final String bestRatingPlatform;
    private final Boolean hasMockData;
    private final Boolean realDataOnly;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime lastSyncedAt;

    public MyRankResponse(Long userId, String displayName,
                          Long globalRank, Long collegeRank, String college,
                          Integer totalSolved, Double weightedScore,
                          Integer bestRating, String bestRatingPlatform,
                          Boolean hasMockData, Boolean realDataOnly,
                          LocalDateTime lastSyncedAt) {
        this.userId = userId;
        this.displayName = displayName;
        this.globalRank = globalRank;
        this.collegeRank = collegeRank;
        this.college = college;
        this.totalSolved = totalSolved;
        this.weightedScore = weightedScore;
        this.bestRating = bestRating;
        this.bestRatingPlatform = bestRatingPlatform;
        this.hasMockData = hasMockData;
        this.realDataOnly = realDataOnly;
        this.lastSyncedAt = lastSyncedAt;
    }

    /** Factory for users that have never been synced. */
    public static MyRankResponse notSynced(Long userId) {
        return new MyRankResponse(userId, null, null, null, null,
                0, 0.0, null, null, false, false, null);
    }

    public Long getUserId() { return userId; }
    public String getDisplayName() { return displayName; }
    public Long getGlobalRank() { return globalRank; }
    public Long getCollegeRank() { return collegeRank; }
    public String getCollege() { return college; }
    public Integer getTotalSolved() { return totalSolved; }
    public Double getWeightedScore() { return weightedScore; }
    public Integer getBestRating() { return bestRating; }
    public String getBestRatingPlatform() { return bestRatingPlatform; }
    public Boolean getHasMockData() { return hasMockData; }
    public Boolean getRealDataOnly() { return realDataOnly; }
    public LocalDateTime getLastSyncedAt() { return lastSyncedAt; }
}

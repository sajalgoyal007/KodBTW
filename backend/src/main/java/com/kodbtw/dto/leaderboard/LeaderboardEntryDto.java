package com.kodbtw.dto.leaderboard;

/**
 * A single ranked entry in the leaderboard response.
 */
public class LeaderboardEntryDto {

    private final long rank;
    private final Long userId;
    private final String displayName;
    private final String username;
    private final String college;
    private final Integer totalSolved;
    private final Double weightedScore;
    private final Integer bestRating;
    private final String bestRatingPlatform;
    private final Integer totalContests;
    private final Boolean hasMockData;
    private final Boolean realDataOnly;
    private final boolean isCurrentUser;

    public LeaderboardEntryDto(long rank, Long userId, String displayName, String username, String college,
                               Integer totalSolved, Double weightedScore,
                               Integer bestRating, String bestRatingPlatform,
                               Integer totalContests, Boolean hasMockData,
                               Boolean realDataOnly, boolean isCurrentUser) {
        this.rank = rank;
        this.userId = userId;
        this.displayName = displayName;
        this.username = username;
        this.college = college;
        this.totalSolved = totalSolved;
        this.weightedScore = weightedScore;
        this.bestRating = bestRating;
        this.bestRatingPlatform = bestRatingPlatform;
        this.totalContests = totalContests;
        this.hasMockData = hasMockData;
        this.realDataOnly = realDataOnly;
        this.isCurrentUser = isCurrentUser;
    }

    public long getRank() { return rank; }
    public Long getUserId() { return userId; }
    public String getDisplayName() { return displayName; }
    public String getUsername() { return username; }
    public String getCollege() { return college; }
    public Integer getTotalSolved() { return totalSolved; }
    public Double getWeightedScore() { return weightedScore; }
    public Integer getBestRating() { return bestRating; }
    public String getBestRatingPlatform() { return bestRatingPlatform; }
    public Integer getTotalContests() { return totalContests; }
    public Boolean getHasMockData() { return hasMockData; }
    public Boolean getRealDataOnly() { return realDataOnly; }

    @com.fasterxml.jackson.annotation.JsonProperty("isCurrentUser")
    public boolean isCurrentUser() { return isCurrentUser; }
}

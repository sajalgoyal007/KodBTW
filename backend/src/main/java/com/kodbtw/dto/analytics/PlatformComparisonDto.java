package com.kodbtw.dto.analytics;

import com.kodbtw.entity.Platform;

public class PlatformComparisonDto {

    private Platform platform;
    private String username;
    private Integer totalSolved;
    private Double sharePercentage;
    private Integer rating;
    private Integer rank;
    private Integer contestsParticipated;
    private Integer currentStreak;
    private Integer longestStreak;
    private String source;

    public PlatformComparisonDto() {
    }

    public PlatformComparisonDto(Platform platform, String username, Integer totalSolved,
                                 Double sharePercentage, Integer rating, Integer rank,
                                 Integer contestsParticipated, Integer currentStreak,
                                 Integer longestStreak, String source) {
        this.platform = platform;
        this.username = username;
        this.totalSolved = totalSolved;
        this.sharePercentage = sharePercentage;
        this.rating = rating;
        this.rank = rank;
        this.contestsParticipated = contestsParticipated;
        this.currentStreak = currentStreak;
        this.longestStreak = longestStreak;
        this.source = source;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getTotalSolved() {
        return totalSolved;
    }

    public void setTotalSolved(Integer totalSolved) {
        this.totalSolved = totalSolved;
    }

    public Double getSharePercentage() {
        return sharePercentage;
    }

    public void setSharePercentage(Double sharePercentage) {
        this.sharePercentage = sharePercentage;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Integer getContestsParticipated() {
        return contestsParticipated;
    }

    public void setContestsParticipated(Integer contestsParticipated) {
        this.contestsParticipated = contestsParticipated;
    }

    public Integer getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(Integer currentStreak) {
        this.currentStreak = currentStreak;
    }

    public Integer getLongestStreak() {
        return longestStreak;
    }

    public void setLongestStreak(Integer longestStreak) {
        this.longestStreak = longestStreak;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}

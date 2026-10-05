package com.kodbtw.dto;

import com.kodbtw.entity.Platform;

import java.time.LocalDateTime;

public class PlatformStats {

    private Platform platform;
    private String username;
    private String profileUrl;
    private Integer totalProblemsSolved;
    private Integer easySolved;
    private Integer mediumSolved;
    private Integer hardSolved;
    private Integer rating;
    private Integer rank;
    private Integer contestsParticipated;
    private Integer currentStreak;
    private Integer longestStreak;
    private LocalDateTime lastSyncedAt;
    private String source;

    public PlatformStats() {
    }

    public PlatformStats(Platform platform, String username, String profileUrl,
                         Integer totalProblemsSolved, Integer easySolved, Integer mediumSolved, Integer hardSolved,
                         Integer rating, Integer rank, Integer contestsParticipated,
                         Integer currentStreak, Integer longestStreak,
                         LocalDateTime lastSyncedAt, String source) {
        this.platform = platform;
        this.username = username;
        this.profileUrl = profileUrl;
        this.totalProblemsSolved = totalProblemsSolved;
        this.easySolved = easySolved;
        this.mediumSolved = mediumSolved;
        this.hardSolved = hardSolved;
        this.rating = rating;
        this.rank = rank;
        this.contestsParticipated = contestsParticipated;
        this.currentStreak = currentStreak;
        this.longestStreak = longestStreak;
        this.lastSyncedAt = lastSyncedAt;
        this.source = source;
    }

    public static Builder builder() {
        return new Builder();
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

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public Integer getTotalProblemsSolved() {
        return totalProblemsSolved;
    }

    public void setTotalProblemsSolved(Integer totalProblemsSolved) {
        this.totalProblemsSolved = totalProblemsSolved;
    }

    public Integer getEasySolved() {
        return easySolved;
    }

    public void setEasySolved(Integer easySolved) {
        this.easySolved = easySolved;
    }

    public Integer getMediumSolved() {
        return mediumSolved;
    }

    public void setMediumSolved(Integer mediumSolved) {
        this.mediumSolved = mediumSolved;
    }

    public Integer getHardSolved() {
        return hardSolved;
    }

    public void setHardSolved(Integer hardSolved) {
        this.hardSolved = hardSolved;
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

    public LocalDateTime getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(LocalDateTime lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public static class Builder {
        private Platform platform;
        private String username;
        private String profileUrl;
        private Integer totalProblemsSolved;
        private Integer easySolved;
        private Integer mediumSolved;
        private Integer hardSolved;
        private Integer rating;
        private Integer rank;
        private Integer contestsParticipated;
        private Integer currentStreak;
        private Integer longestStreak;
        private LocalDateTime lastSyncedAt;
        private String source = "MOCK";

        public Builder platform(Platform platform) {
            this.platform = platform;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder profileUrl(String profileUrl) {
            this.profileUrl = profileUrl;
            return this;
        }

        public Builder totalProblemsSolved(Integer totalProblemsSolved) {
            this.totalProblemsSolved = totalProblemsSolved;
            return this;
        }

        public Builder easySolved(Integer easySolved) {
            this.easySolved = easySolved;
            return this;
        }

        public Builder mediumSolved(Integer mediumSolved) {
            this.mediumSolved = mediumSolved;
            return this;
        }

        public Builder hardSolved(Integer hardSolved) {
            this.hardSolved = hardSolved;
            return this;
        }

        public Builder rating(Integer rating) {
            this.rating = rating;
            return this;
        }

        public Builder rank(Integer rank) {
            this.rank = rank;
            return this;
        }

        public Builder contestsParticipated(Integer contestsParticipated) {
            this.contestsParticipated = contestsParticipated;
            return this;
        }

        public Builder currentStreak(Integer currentStreak) {
            this.currentStreak = currentStreak;
            return this;
        }

        public Builder longestStreak(Integer longestStreak) {
            this.longestStreak = longestStreak;
            return this;
        }

        public Builder lastSyncedAt(LocalDateTime lastSyncedAt) {
            this.lastSyncedAt = lastSyncedAt;
            return this;
        }

        public Builder source(String source) {
            this.source = source;
            return this;
        }

        public PlatformStats build() {
            return new PlatformStats(
                    platform, username, profileUrl,
                    totalProblemsSolved, easySolved, mediumSolved, hardSolved,
                    rating, rank, contestsParticipated,
                    currentStreak, longestStreak,
                    lastSyncedAt, source
            );
        }
    }
}

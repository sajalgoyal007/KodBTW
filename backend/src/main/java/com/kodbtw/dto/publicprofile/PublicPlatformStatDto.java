package com.kodbtw.dto.publicprofile;

import com.kodbtw.entity.Platform;

import java.time.LocalDateTime;

public class PublicPlatformStatDto {

    private Platform platform;
    private String username;
    private String profileUrl;
    private boolean verified;
    private Integer totalSolved;
    private Integer easySolved;
    private Integer mediumSolved;
    private Integer hardSolved;
    private Integer rating;
    private Integer rank;
    private Integer contests;
    private Integer currentStreak;
    private Integer longestStreak;
    private String source;
    private LocalDateTime lastSyncedAt;

    public PublicPlatformStatDto() {
    }

    public PublicPlatformStatDto(Platform platform, String username, String profileUrl, boolean verified,
                                 Integer totalSolved, Integer easySolved, Integer mediumSolved, Integer hardSolved,
                                 Integer rating, Integer rank, Integer contests,
                                 Integer currentStreak, Integer longestStreak,
                                 String source, LocalDateTime lastSyncedAt) {
        this.platform = platform;
        this.username = username;
        this.profileUrl = profileUrl;
        this.verified = verified;
        this.totalSolved = totalSolved;
        this.easySolved = easySolved;
        this.mediumSolved = mediumSolved;
        this.hardSolved = hardSolved;
        this.rating = rating;
        this.rank = rank;
        this.contests = contests;
        this.currentStreak = currentStreak;
        this.longestStreak = longestStreak;
        this.source = source;
        this.lastSyncedAt = lastSyncedAt;
    }

    public Platform getPlatform() { return platform; }
    public void setPlatform(Platform platform) { this.platform = platform; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getProfileUrl() { return profileUrl; }
    public void setProfileUrl(String profileUrl) { this.profileUrl = profileUrl; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

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

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public LocalDateTime getLastSyncedAt() { return lastSyncedAt; }
    public void setLastSyncedAt(LocalDateTime lastSyncedAt) { this.lastSyncedAt = lastSyncedAt; }
}

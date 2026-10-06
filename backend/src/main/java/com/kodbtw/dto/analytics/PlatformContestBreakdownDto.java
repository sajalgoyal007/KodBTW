package com.kodbtw.dto.analytics;

import com.kodbtw.entity.Platform;

public class PlatformContestBreakdownDto {

    private Platform platform;
    private Integer contests;
    private Integer rating;
    private Integer rank;

    public PlatformContestBreakdownDto() {
    }

    public PlatformContestBreakdownDto(Platform platform, Integer contests, Integer rating, Integer rank) {
        this.platform = platform;
        this.contests = contests;
        this.rating = rating;
        this.rank = rank;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public Integer getContests() {
        return contests;
    }

    public void setContests(Integer contests) {
        this.contests = contests;
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
}

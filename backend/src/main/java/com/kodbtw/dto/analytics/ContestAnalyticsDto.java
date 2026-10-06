package com.kodbtw.dto.analytics;

import java.util.ArrayList;
import java.util.List;

public class ContestAnalyticsDto {

    private Integer totalContests;
    private List<PlatformContestBreakdownDto> platformBreakdown;

    public ContestAnalyticsDto() {
        this.platformBreakdown = new ArrayList<>();
    }

    public ContestAnalyticsDto(Integer totalContests, List<PlatformContestBreakdownDto> platformBreakdown) {
        this.totalContests = totalContests;
        this.platformBreakdown = platformBreakdown != null ? platformBreakdown : new ArrayList<>();
    }

    public Integer getTotalContests() {
        return totalContests;
    }

    public void setTotalContests(Integer totalContests) {
        this.totalContests = totalContests;
    }

    public List<PlatformContestBreakdownDto> getPlatformBreakdown() {
        return platformBreakdown;
    }

    public void setPlatformBreakdown(List<PlatformContestBreakdownDto> platformBreakdown) {
        this.platformBreakdown = platformBreakdown != null ? platformBreakdown : new ArrayList<>();
    }
}

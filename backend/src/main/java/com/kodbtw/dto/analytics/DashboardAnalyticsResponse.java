package com.kodbtw.dto.analytics;

import java.util.ArrayList;
import java.util.List;

public class DashboardAnalyticsResponse {

    private DifficultyAnalyticsDto difficulty;
    private List<PlatformComparisonDto> platformComparison;
    private ContestAnalyticsDto contests;

    public DashboardAnalyticsResponse() {
        this.difficulty = new DifficultyAnalyticsDto();
        this.platformComparison = new ArrayList<>();
        this.contests = new ContestAnalyticsDto();
    }

    public DashboardAnalyticsResponse(DifficultyAnalyticsDto difficulty,
                                      List<PlatformComparisonDto> platformComparison,
                                      ContestAnalyticsDto contests) {
        this.difficulty = difficulty;
        this.platformComparison = platformComparison != null ? platformComparison : new ArrayList<>();
        this.contests = contests;
    }

    public DifficultyAnalyticsDto getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(DifficultyAnalyticsDto difficulty) {
        this.difficulty = difficulty;
    }

    public List<PlatformComparisonDto> getPlatformComparison() {
        return platformComparison;
    }

    public void setPlatformComparison(List<PlatformComparisonDto> platformComparison) {
        this.platformComparison = platformComparison != null ? platformComparison : new ArrayList<>();
    }

    public ContestAnalyticsDto getContests() {
        return contests;
    }

    public void setContests(ContestAnalyticsDto contests) {
        this.contests = contests;
    }
}

package com.kodbtw.dto.publicprofile;

import com.kodbtw.dto.analytics.ContestAnalyticsDto;
import com.kodbtw.dto.analytics.DifficultyAnalyticsDto;
import com.kodbtw.dto.analytics.PlatformComparisonDto;

import java.util.Collections;
import java.util.List;

public class PublicAnalyticsDto {

    private DifficultyAnalyticsDto difficulty;
    private List<PlatformComparisonDto> platformComparison;
    private ContestAnalyticsDto contests;

    public PublicAnalyticsDto() {
        this.platformComparison = Collections.emptyList();
    }

    public PublicAnalyticsDto(DifficultyAnalyticsDto difficulty,
                              List<PlatformComparisonDto> platformComparison,
                              ContestAnalyticsDto contests) {
        this.difficulty = difficulty;
        this.platformComparison = platformComparison != null ? platformComparison : Collections.emptyList();
        this.contests = contests;
    }

    public DifficultyAnalyticsDto getDifficulty() { return difficulty; }
    public void setDifficulty(DifficultyAnalyticsDto difficulty) { this.difficulty = difficulty; }

    public List<PlatformComparisonDto> getPlatformComparison() { return platformComparison; }
    public void setPlatformComparison(List<PlatformComparisonDto> platformComparison) {
        this.platformComparison = platformComparison != null ? platformComparison : Collections.emptyList();
    }

    public ContestAnalyticsDto getContests() { return contests; }
    public void setContests(ContestAnalyticsDto contests) { this.contests = contests; }
}

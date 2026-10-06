package com.kodbtw.dto.analytics;

import java.util.ArrayList;
import java.util.List;

public class DifficultyAnalyticsDto {

    private Integer totalProblemsSolved;
    private DifficultyMetricDto easy;
    private DifficultyMetricDto medium;
    private DifficultyMetricDto hard;
    private List<PlatformDifficultyBreakdownDto> platformBreakdown;

    public DifficultyAnalyticsDto() {
        this.platformBreakdown = new ArrayList<>();
    }

    public DifficultyAnalyticsDto(Integer totalProblemsSolved,
                                  DifficultyMetricDto easy,
                                  DifficultyMetricDto medium,
                                  DifficultyMetricDto hard,
                                  List<PlatformDifficultyBreakdownDto> platformBreakdown) {
        this.totalProblemsSolved = totalProblemsSolved;
        this.easy = easy;
        this.medium = medium;
        this.hard = hard;
        this.platformBreakdown = platformBreakdown != null ? platformBreakdown : new ArrayList<>();
    }

    public Integer getTotalProblemsSolved() {
        return totalProblemsSolved;
    }

    public void setTotalProblemsSolved(Integer totalProblemsSolved) {
        this.totalProblemsSolved = totalProblemsSolved;
    }

    public DifficultyMetricDto getEasy() {
        return easy;
    }

    public void setEasy(DifficultyMetricDto easy) {
        this.easy = easy;
    }

    public DifficultyMetricDto getMedium() {
        return medium;
    }

    public void setMedium(DifficultyMetricDto medium) {
        this.medium = medium;
    }

    public DifficultyMetricDto getHard() {
        return hard;
    }

    public void setHard(DifficultyMetricDto hard) {
        this.hard = hard;
    }

    public List<PlatformDifficultyBreakdownDto> getPlatformBreakdown() {
        return platformBreakdown;
    }

    public void setPlatformBreakdown(List<PlatformDifficultyBreakdownDto> platformBreakdown) {
        this.platformBreakdown = platformBreakdown != null ? platformBreakdown : new ArrayList<>();
    }
}

package com.kodbtw.service;

import com.kodbtw.dto.DashboardOverview;
import com.kodbtw.dto.DashboardStatsResponse;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.dto.analytics.ContestAnalyticsDto;
import com.kodbtw.dto.analytics.DashboardAnalyticsResponse;
import com.kodbtw.dto.analytics.DifficultyAnalyticsDto;
import com.kodbtw.dto.analytics.DifficultyMetricDto;
import com.kodbtw.dto.analytics.PlatformComparisonDto;
import com.kodbtw.dto.analytics.PlatformContestBreakdownDto;
import com.kodbtw.dto.analytics.PlatformDifficultyBreakdownDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class DashboardAnalyticsService {

    private final PlatformStatsService platformStatsService;

    public DashboardAnalyticsService(PlatformStatsService platformStatsService) {
        this.platformStatsService = platformStatsService;
    }

    @Transactional(readOnly = true)
    public DashboardAnalyticsResponse getAnalytics(Long userId) {
        DashboardStatsResponse dashboardStats = platformStatsService.getDashboardStats(userId);
        List<PlatformStats> platforms = dashboardStats.getPlatforms();

        if (platforms == null || platforms.isEmpty()) {
            DifficultyAnalyticsDto emptyDifficulty = new DifficultyAnalyticsDto(
                    0,
                    new DifficultyMetricDto(0, 0.0),
                    new DifficultyMetricDto(0, 0.0),
                    new DifficultyMetricDto(0, 0.0),
                    Collections.emptyList()
            );
            ContestAnalyticsDto emptyContests = new ContestAnalyticsDto(0, Collections.emptyList());
            return new DashboardAnalyticsResponse(emptyDifficulty, Collections.emptyList(), emptyContests);
        }

        DashboardOverview overview = dashboardStats.getOverview();
        Integer totalSolved = overview.getTotalProblemsSolved();
        Integer easySolved = overview.getEasySolved();
        Integer mediumSolved = overview.getMediumSolved();
        Integer hardSolved = overview.getHardSolved();
        Integer totalContests = overview.getContestsParticipated();

        DifficultyMetricDto easyMetric = new DifficultyMetricDto(easySolved, calculatePercentage(easySolved, totalSolved));
        DifficultyMetricDto mediumMetric = new DifficultyMetricDto(mediumSolved, calculatePercentage(mediumSolved, totalSolved));
        DifficultyMetricDto hardMetric = new DifficultyMetricDto(hardSolved, calculatePercentage(hardSolved, totalSolved));

        List<PlatformDifficultyBreakdownDto> difficultyBreakdowns = new ArrayList<>(platforms.size());
        List<PlatformComparisonDto> comparisonList = new ArrayList<>(platforms.size());
        List<PlatformContestBreakdownDto> contestBreakdowns = new ArrayList<>(platforms.size());

        for (PlatformStats stats : platforms) {
            // Difficulty breakdown per platform - preserves nulls for unsupported metrics
            difficultyBreakdowns.add(new PlatformDifficultyBreakdownDto(
                    stats.getPlatform(),
                    stats.getEasySolved(),
                    stats.getMediumSolved(),
                    stats.getHardSolved(),
                    stats.getTotalProblemsSolved()
            ));

            // Platform problem share calculation
            double sharePercentage = 0.0;
            if (stats.getTotalProblemsSolved() != null && totalSolved > 0) {
                sharePercentage = calculatePercentage(stats.getTotalProblemsSolved(), totalSolved);
            }

            comparisonList.add(new PlatformComparisonDto(
                    stats.getPlatform(),
                    stats.getUsername(),
                    stats.getTotalProblemsSolved(),
                    sharePercentage,
                    stats.getRating(),
                    stats.getRank(),
                    stats.getContestsParticipated(),
                    stats.getCurrentStreak(),
                    stats.getLongestStreak(),
                    stats.getSource()
            ));

            // Contests breakdown per platform
            contestBreakdowns.add(new PlatformContestBreakdownDto(
                    stats.getPlatform(),
                    stats.getContestsParticipated(),
                    stats.getRating(),
                    stats.getRank()
            ));
        }

        DifficultyAnalyticsDto difficulty = new DifficultyAnalyticsDto(
                totalSolved,
                easyMetric,
                mediumMetric,
                hardMetric,
                difficultyBreakdowns
        );

        ContestAnalyticsDto contests = new ContestAnalyticsDto(totalContests, contestBreakdowns);

        return new DashboardAnalyticsResponse(difficulty, comparisonList, contests);
    }

    private Double calculatePercentage(Integer count, Integer total) {
        if (count == null || total == null) return null;
        if (total == 0) {
            return 0.0;
        }
        double raw = ((double) count / total) * 100.0;
        return BigDecimal.valueOf(raw).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}

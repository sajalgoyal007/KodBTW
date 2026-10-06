package com.kodbtw.service;

import com.kodbtw.dto.DashboardOverview;
import com.kodbtw.dto.DashboardStatsResponse;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.dto.analytics.DashboardAnalyticsResponse;
import com.kodbtw.dto.analytics.PlatformComparisonDto;
import com.kodbtw.dto.analytics.PlatformDifficultyBreakdownDto;
import com.kodbtw.entity.Platform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardAnalyticsServiceTest {

    @Mock
    private PlatformStatsService platformStatsService;

    private DashboardAnalyticsService dashboardAnalyticsService;

    @BeforeEach
    void setUp() {
        dashboardAnalyticsService = new DashboardAnalyticsService(platformStatsService);
    }

    @Test
    void getAnalytics_whenNoAccounts_returnsEmptyState() {
        Long userId = 1L;
        DashboardStatsResponse emptyResponse = new DashboardStatsResponse(
                new DashboardOverview(0, 0, 0, 0, 0, null, null, 0),
                Collections.emptyList()
        );

        when(platformStatsService.getDashboardStats(userId)).thenReturn(emptyResponse);

        DashboardAnalyticsResponse response = dashboardAnalyticsService.getAnalytics(userId);

        assertNotNull(response);
        assertEquals(0, response.getDifficulty().getTotalProblemsSolved());
        assertEquals(0, response.getDifficulty().getEasy().getCount());
        assertEquals(0.0, response.getDifficulty().getEasy().getPercentage());
        assertEquals(0, response.getDifficulty().getMedium().getCount());
        assertEquals(0.0, response.getDifficulty().getMedium().getPercentage());
        assertEquals(0, response.getDifficulty().getHard().getCount());
        assertEquals(0.0, response.getDifficulty().getHard().getPercentage());
        assertTrue(response.getDifficulty().getPlatformBreakdown().isEmpty());

        assertTrue(response.getPlatformComparison().isEmpty());
        assertEquals(0, response.getContests().getTotalContests());
        assertTrue(response.getContests().getPlatformBreakdown().isEmpty());
    }

    @Test
    void getAnalytics_difficultyPercentageCalculation() {
        Long userId = 1L;

        PlatformStats stats = PlatformStats.builder()
                .platform(Platform.LEETCODE)
                .username("testuser")
                .totalProblemsSolved(200)
                .easySolved(100)
                .mediumSolved(60)
                .hardSolved(40)
                .contestsParticipated(5)
                .build();

        DashboardOverview overview = new DashboardOverview(200, 100, 60, 40, 5, 10, 20, 1);
        DashboardStatsResponse statsResponse = new DashboardStatsResponse(overview, List.of(stats));

        when(platformStatsService.getDashboardStats(userId)).thenReturn(statsResponse);

        DashboardAnalyticsResponse response = dashboardAnalyticsService.getAnalytics(userId);

        assertEquals(200, response.getDifficulty().getTotalProblemsSolved());
        assertEquals(100, response.getDifficulty().getEasy().getCount());
        assertEquals(50.0, response.getDifficulty().getEasy().getPercentage());
        assertEquals(60, response.getDifficulty().getMedium().getCount());
        assertEquals(30.0, response.getDifficulty().getMedium().getPercentage());
        assertEquals(40, response.getDifficulty().getHard().getCount());
        assertEquals(20.0, response.getDifficulty().getHard().getPercentage());
    }

    @Test
    void getAnalytics_divisionByZeroProtection() {
        Long userId = 1L;

        PlatformStats stats = PlatformStats.builder()
                .platform(Platform.CODEFORCES)
                .username("testuser")
                .totalProblemsSolved(0)
                .easySolved(0)
                .mediumSolved(0)
                .hardSolved(0)
                .build();

        DashboardOverview overview = new DashboardOverview(0, 0, 0, 0, 0, null, null, 1);
        DashboardStatsResponse statsResponse = new DashboardStatsResponse(overview, List.of(stats));

        when(platformStatsService.getDashboardStats(userId)).thenReturn(statsResponse);

        DashboardAnalyticsResponse response = dashboardAnalyticsService.getAnalytics(userId);

        assertEquals(0, response.getDifficulty().getTotalProblemsSolved());
        assertEquals(0.0, response.getDifficulty().getEasy().getPercentage());
        assertEquals(0.0, response.getDifficulty().getMedium().getPercentage());
        assertEquals(0.0, response.getDifficulty().getHard().getPercentage());
        assertEquals(0.0, response.getPlatformComparison().get(0).getSharePercentage());
    }

    @Test
    void getAnalytics_platformShareCalculation() {
        Long userId = 1L;

        PlatformStats lcStats = PlatformStats.builder()
                .platform(Platform.LEETCODE)
                .username("lc")
                .totalProblemsSolved(300)
                .easySolved(150)
                .mediumSolved(100)
                .hardSolved(50)
                .build();

        PlatformStats ccStats = PlatformStats.builder()
                .platform(Platform.CODECHEF)
                .username("cc")
                .totalProblemsSolved(100)
                .easySolved(50)
                .mediumSolved(30)
                .hardSolved(20)
                .build();

        DashboardOverview overview = new DashboardOverview(400, 200, 130, 70, 0, null, null, 2);
        DashboardStatsResponse statsResponse = new DashboardStatsResponse(overview, List.of(lcStats, ccStats));

        when(platformStatsService.getDashboardStats(userId)).thenReturn(statsResponse);

        DashboardAnalyticsResponse response = dashboardAnalyticsService.getAnalytics(userId);

        List<PlatformComparisonDto> comparisons = response.getPlatformComparison();
        assertEquals(2, comparisons.size());
        assertEquals(75.0, comparisons.get(0).getSharePercentage());
        assertEquals(25.0, comparisons.get(1).getSharePercentage());
    }

    @Test
    void getAnalytics_nullMetricHandling() {
        Long userId = 1L;

        PlatformStats lcStats = PlatformStats.builder()
                .platform(Platform.LEETCODE)
                .username("lc")
                .totalProblemsSolved(300)
                .easySolved(150)
                .mediumSolved(100)
                .hardSolved(50)
                .build();

        PlatformStats cfStats = PlatformStats.builder()
                .platform(Platform.CODEFORCES)
                .username("cf")
                .totalProblemsSolved(null)
                .easySolved(null)
                .mediumSolved(null)
                .hardSolved(null)
                .rating(1500)
                .rank(1600)
                .build();

        DashboardOverview overview = new DashboardOverview(300, 150, 100, 50, 0, null, null, 2);
        DashboardStatsResponse statsResponse = new DashboardStatsResponse(overview, List.of(lcStats, cfStats));

        when(platformStatsService.getDashboardStats(userId)).thenReturn(statsResponse);

        DashboardAnalyticsResponse response = dashboardAnalyticsService.getAnalytics(userId);

        List<PlatformDifficultyBreakdownDto> breakdown = response.getDifficulty().getPlatformBreakdown();
        assertEquals(2, breakdown.size());

        PlatformDifficultyBreakdownDto cfBreakdown = breakdown.get(1);
        assertEquals(Platform.CODEFORCES, cfBreakdown.getPlatform());
        assertNull(cfBreakdown.getEasy());
        assertNull(cfBreakdown.getMedium());
        assertNull(cfBreakdown.getHard());
        assertNull(cfBreakdown.getTotal());

        PlatformComparisonDto cfComparison = response.getPlatformComparison().get(1);
        assertNull(cfComparison.getTotalSolved());
        assertEquals(0.0, cfComparison.getSharePercentage());
        assertEquals(1500, cfComparison.getRating());
        assertEquals(1600, cfComparison.getRank());
    }

    @Test
    void getAnalytics_contestAggregation() {
        Long userId = 1L;

        PlatformStats lcStats = PlatformStats.builder()
                .platform(Platform.LEETCODE)
                .username("lc")
                .contestsParticipated(10)
                .rating(1800)
                .rank(40000)
                .build();

        PlatformStats ccStats = PlatformStats.builder()
                .platform(Platform.CODECHEF)
                .username("cc")
                .contestsParticipated(25)
                .rating(1750)
                .rank(4500)
                .build();

        PlatformStats cfStats = PlatformStats.builder()
                .platform(Platform.CODEFORCES)
                .username("cf")
                .contestsParticipated(null)
                .rating(1600)
                .rank(1650)
                .build();

        DashboardOverview overview = new DashboardOverview(0, 0, 0, 0, 35, null, null, 3);
        DashboardStatsResponse statsResponse = new DashboardStatsResponse(overview, List.of(lcStats, ccStats, cfStats));

        when(platformStatsService.getDashboardStats(userId)).thenReturn(statsResponse);

        DashboardAnalyticsResponse response = dashboardAnalyticsService.getAnalytics(userId);

        assertEquals(35, response.getContests().getTotalContests());
        assertEquals(3, response.getContests().getPlatformBreakdown().size());
        assertEquals(10, response.getContests().getPlatformBreakdown().get(0).getContests());
        assertEquals(25, response.getContests().getPlatformBreakdown().get(1).getContests());
        assertNull(response.getContests().getPlatformBreakdown().get(2).getContests());
    }

    @Test
    void getAnalytics_streakPreservation() {
        Long userId = 1L;

        PlatformStats lcStats = PlatformStats.builder()
                .platform(Platform.LEETCODE)
                .username("lc")
                .currentStreak(14)
                .longestStreak(30)
                .build();

        PlatformStats ccStats = PlatformStats.builder()
                .platform(Platform.CODECHEF)
                .username("cc")
                .currentStreak(null)
                .longestStreak(null)
                .build();

        DashboardOverview overview = new DashboardOverview(0, 0, 0, 0, 0, 14, 30, 2);
        DashboardStatsResponse statsResponse = new DashboardStatsResponse(overview, List.of(lcStats, ccStats));

        when(platformStatsService.getDashboardStats(userId)).thenReturn(statsResponse);

        DashboardAnalyticsResponse response = dashboardAnalyticsService.getAnalytics(userId);

        List<PlatformComparisonDto> comparisons = response.getPlatformComparison();
        assertEquals(14, comparisons.get(0).getCurrentStreak());
        assertEquals(30, comparisons.get(0).getLongestStreak());
        assertNull(comparisons.get(1).getCurrentStreak());
        assertNull(comparisons.get(1).getLongestStreak());
    }
}

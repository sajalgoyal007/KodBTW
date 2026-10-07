package com.kodbtw.service;

import com.kodbtw.dto.insights.DashboardInsightsResponse;
import com.kodbtw.entity.PlatformStatSnapshot;
import com.kodbtw.repository.PlatformStatSnapshotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardInsightsServiceTest {
    @Mock private PlatformStatSnapshotRepository snapshots;
    private DashboardInsightsService service;

    @BeforeEach
    void setUp() { service = new DashboardInsightsService(snapshots); }

    @Test
    void noSnapshotsReturnsUnavailableStateAndNoInsights() {
        when(snapshots.findAllByUserId(1L)).thenReturn(List.of());

        DashboardInsightsResponse response = service.getInsights(1L);

        assertEquals("No verified platform snapshots are available yet.", response.summary());
        assertEquals(0, response.dataAvailability().snapshotCount());
        assertEquals(0, response.dataAvailability().platformCount());
        assertNull(response.dataAvailability().latestSnapshotDate());
        assertFalse(response.dataAvailability().hasHistoricalComparison());
        assertTrue(response.dataAvailability().realDataOnly());
        assertTrue(response.platformInsights().isEmpty());
        assertTrue(response.progressInsights().isEmpty());
        assertTrue(response.difficultyInsights().isEmpty());
    }

    @Test
    void oneRealSnapshotProducesCurrentBaselinesWithoutTrendClaims() {
        PlatformStatSnapshot row = snapshot(1L, "LEETCODE", LocalDate.of(2026, 10, 7), 291,
                124, 129, 38, 1352, 58, 534894, "LEETCODE_REAL");
        when(snapshots.findAllByUserId(2L)).thenReturn(List.of(row));

        DashboardInsightsResponse response = service.getInsights(2L);

        assertEquals("Current verified baseline: 291 problems solved across 1 platform(s). Values reflect each platform's latest recorded snapshot.", response.summary());
        assertEquals(1, response.dataAvailability().snapshotCount());
        assertEquals(1, response.dataAvailability().platformCount());
        assertFalse(response.dataAvailability().hasHistoricalComparison());
        assertEquals("PROGRESS_BASELINE", response.progressInsights().get(0).type());
        assertTrue(response.progressInsights().stream().noneMatch(item -> item.type().equals("PROGRESS_INCREASE")));
        assertEquals("RATING_BASELINE", response.ratingInsights().get(0).type());
        var difficulty = response.difficultyInsights().stream()
                .filter(item -> item.type().equals("DIFFICULTY_DISTRIBUTION")).findFirst().orElseThrow();
        assertEquals("42.6", difficulty.supportingValues().get("easyPercentage").toPlainString());
        assertEquals("44.3", difficulty.supportingValues().get("mediumPercentage").toPlainString());
        assertEquals("13.1", difficulty.supportingValues().get("hardPercentage").toPlainString());
        assertTrue(response.streakInsights().stream().anyMatch(item -> item.type().equals("STREAK_CURRENT")));
        assertTrue(response.platformInsights().stream().noneMatch(item -> item.title().contains("strongest")));
    }

    @Test
    void twoRealObservationsReportActualIncrease() {
        LocalDate previousDate = LocalDate.of(2026, 10, 1);
        LocalDate currentDate = LocalDate.of(2026, 10, 7);
        when(snapshots.findAllByUserId(3L)).thenReturn(List.of(
                snapshot(1L, "LEETCODE", previousDate, 280, 120, 125, 35, 1300, 52, 500000, "LEETCODE_REAL"),
                snapshot(2L, "LEETCODE", currentDate, 291, 124, 129, 38, 1352, 58, 534894, "LEETCODE_REAL")));

        DashboardInsightsResponse response = service.getInsights(3L);

        var progress = response.progressInsights().stream().filter(item -> item.metric().equals("TOTAL_SOLVED")).findFirst().orElseThrow();
        assertEquals("PROGRESS_INCREASE", progress.type());
        assertEquals(11, progress.change().intValue());
        assertEquals(previousDate, progress.comparisonDate());
        assertEquals(currentDate, progress.date());
        assertTrue(response.dataAvailability().hasHistoricalComparison());
    }

    @Test
    void twoRealObservationsPreserveNegativeCorrections() {
        when(snapshots.findAllByUserId(4L)).thenReturn(List.of(
                snapshot(1L, "LEETCODE", LocalDate.of(2026, 9, 1), 301, 130, 130, 41, 1400, 60, null, "LEETCODE_REAL"),
                snapshot(2L, "LEETCODE", LocalDate.of(2026, 10, 1), 291, 124, 129, 38, 1352, 58, null, "LEETCODE_REAL")));

        var response = service.getInsights(4L);

        var progress = response.progressInsights().stream().filter(item -> item.metric().equals("TOTAL_SOLVED")).findFirst().orElseThrow();
        assertEquals("PROGRESS_CORRECTION", progress.type());
        assertEquals(-10, progress.change().intValue());
        assertTrue(progress.message().contains("preserved as reported"));
        var rating = response.ratingInsights().get(0);
        assertEquals("RATING_DECREASE", rating.type());
        assertEquals(-48, rating.change().intValue());
    }

    @Test
    void multipleRealPlatformsAreReportedWithoutComparingUnrelatedRatings() {
        when(snapshots.findAllByUserId(5L)).thenReturn(List.of(
                snapshot(1L, "LEETCODE", LocalDate.of(2026, 10, 6), 291, 124, 129, 38, 1352, null, null, "LEETCODE_REAL"),
                snapshot(2L, "CODEFORCES", LocalDate.of(2026, 10, 7), 600, null, null, null, 1850, null, null, "CODEFORCES_REAL")));

        var response = service.getInsights(5L);

        assertEquals(2, response.dataAvailability().platformCount());
        assertEquals(2, response.platformInsights().size());
        assertTrue(response.strengths().stream().anyMatch(item -> item.type().equals("PLATFORM_SOLVED_COMPARISON")
                && item.message().contains("solved counts only")));
        assertFalse(response.strengths().stream().anyMatch(item -> item.metric() != null && item.metric().equals("RATING")));
    }

    @Test
    void mockAndNonRealRowsAreExcluded() {
        when(snapshots.findAllByUserId(6L)).thenReturn(List.of(
                snapshot(1L, "CODECHEF", LocalDate.of(2026, 10, 7), 500, 200, 200, 100, 1700, 10, null, "CODECHEF_MOCK"),
                snapshot(2L, "LEETCODE", LocalDate.of(2026, 10, 7), 999, 500, 300, 199, 1700, 10, null, "MOCK"),
                snapshot(3L, "CODEFORCES", LocalDate.of(2026, 10, 7), 888, 400, 300, 188, 1800, 12, null, "CODEFORCES_MOCK_REAL")));

        var response = service.getInsights(6L);

        assertEquals(0, response.dataAvailability().snapshotCount());
        assertTrue(response.platformInsights().isEmpty());
        assertTrue(response.strengths().isEmpty());
    }

    @Test
    void missingDifficultyRatingStreakAndRankStayAbsent() {
        when(snapshots.findAllByUserId(7L)).thenReturn(List.of(
                snapshot(1L, "CODEFORCES", LocalDate.of(2026, 10, 7), 0, null, null, null, null, null, null, "CODEFORCES_REAL")));

        var response = service.getInsights(7L);

        assertTrue(response.ratingInsights().isEmpty());
        assertTrue(response.streakInsights().isEmpty());
        assertTrue(response.areasToImprove().isEmpty());
        assertTrue(response.difficultyInsights().isEmpty());
        assertEquals(0, response.platformInsights().get(0).value().intValue());
        assertFalse(response.platformInsights().get(0).message().contains("rating"));
        assertFalse(response.platformInsights().get(0).message().contains("rank"));
    }

    @Test
    void latestNullMetricsDoNotCarryOlderRatingOrStreakIntoCurrentInsights() {
        when(snapshots.findAllByUserId(12L)).thenReturn(List.of(
                snapshot(1L, "LEETCODE", LocalDate.of(2026, 10, 1), 280, 120, 125, 35, 1300, 52, 500000, "LEETCODE_REAL"),
                snapshot(2L, "LEETCODE", LocalDate.of(2026, 10, 7), 291, 124, 129, 38, null, null, null, "LEETCODE_REAL")));

        var response = service.getInsights(12L);

        assertTrue(response.ratingInsights().isEmpty());
        assertTrue(response.streakInsights().isEmpty());
        assertFalse(response.platformInsights().get(0).message().contains("rating"));
        assertFalse(response.platformInsights().get(0).message().contains("streak"));
    }

    @Test
    void incompleteDifficultyIsNotConvertedToZeroOrPercentage() {
        when(snapshots.findAllByUserId(8L)).thenReturn(List.of(
                snapshot(1L, "LEETCODE", LocalDate.of(2026, 10, 7), 10, 4, null, 2, null, null, null, "LEETCODE_REAL")));

        var response = service.getInsights(8L);

        var difficulty = response.difficultyInsights().get(0);
        assertEquals("DIFFICULTY_INCOMPLETE", difficulty.type());
        assertNull(difficulty.value());
        assertTrue(difficulty.message().contains("percentages"));
    }

    @Test
    void zeroTotalSolvedDoesNotProduceNonFinitePercentages() {
        when(snapshots.findAllByUserId(9L)).thenReturn(List.of(
                snapshot(1L, "LEETCODE", LocalDate.of(2026, 10, 7), 0, 0, 0, 0, null, null, null, "LEETCODE_REAL")));

        var response = service.getInsights(9L);

        assertEquals("DIFFICULTY_NO_DENOMINATOR", response.difficultyInsights().get(0).type());
        assertNull(response.difficultyInsights().get(0).value());
        assertTrue(response.areasToImprove().isEmpty());
    }

    @Test
    void sparseDatesCompareOnlyRecordedObservations() {
        LocalDate oldDate = LocalDate.of(2024, 1, 1);
        LocalDate recentDate = LocalDate.of(2026, 10, 7);
        when(snapshots.findAllByUserId(10L)).thenReturn(List.of(
                snapshot(1L, "LEETCODE", oldDate, 200, null, null, null, null, null, null, "LEETCODE_REAL"),
                snapshot(2L, "LEETCODE", recentDate, 291, null, null, null, null, null, null, "LEETCODE_REAL")));

        var response = service.getInsights(10L);

        var progress = response.progressInsights().get(0);
        assertEquals(oldDate, progress.comparisonDate());
        assertEquals(recentDate, progress.date());
        assertEquals(91, progress.change().intValue());
        assertEquals(List.of(oldDate, recentDate), List.of(response.dataAvailability().oldestSnapshotDate(),
                response.dataAvailability().latestSnapshotDate()));
    }

    @Test
    void duplicatePlatformDateUsesLatestActualObservation() {
        LocalDate date = LocalDate.of(2026, 10, 7);
        PlatformStatSnapshot first = snapshot(1L, "LEETCODE", date, 280, 120, 125, 35, 1300, null, null, "LEETCODE_REAL");
        first.setSnapshottedAt(date.atTime(10, 0));
        PlatformStatSnapshot second = snapshot(2L, "LEETCODE", date, 291, 124, 129, 38, 1352, null, null, "LEETCODE_REAL");
        second.setSnapshottedAt(date.atTime(11, 0));
        when(snapshots.findAllByUserId(11L)).thenReturn(List.of(first, second));

        var response = service.getInsights(11L);

        assertEquals(2, response.dataAvailability().snapshotCount());
        assertEquals(291, response.platformInsights().get(0).value().intValue());
        assertEquals("PROGRESS_BASELINE", response.progressInsights().get(0).type());
    }

    @Test
    void snapshotQueryIsAlwaysScopedToAuthenticatedUserId() {
        when(snapshots.findAllByUserId(42L)).thenReturn(List.of());

        service.getInsights(42L);

        verify(snapshots).findAllByUserId(eq(42L));
    }

    private PlatformStatSnapshot snapshot(Long id, String platform, LocalDate date, Integer total,
                                          Integer easy, Integer medium, Integer hard, Integer rating,
                                          Integer currentStreak, Integer rank, String source) {
        PlatformStatSnapshot row = new PlatformStatSnapshot();
        row.setId(id);
        row.setPlatform(platform);
        row.setSnapshotDate(date);
        row.setTotalSolved(total);
        row.setEasySolved(easy);
        row.setMediumSolved(medium);
        row.setHardSolved(hard);
        row.setRating(rating);
        row.setCurrentStreak(currentStreak);
        row.setRank(rank);
        row.setSource(source);
        row.setSnapshottedAt(date.atTime(12, 0));
        return row;
    }
}

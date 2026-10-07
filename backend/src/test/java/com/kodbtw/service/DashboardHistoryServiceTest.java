package com.kodbtw.service;

import com.kodbtw.dto.history.DashboardHistoryResponse;
import com.kodbtw.entity.PlatformStatSnapshot;
import com.kodbtw.repository.PlatformStatSnapshotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardHistoryServiceTest {
    @Mock private PlatformStatSnapshotRepository snapshots;
    private DashboardHistoryService service;

    @BeforeEach
    void setUp() { service = new DashboardHistoryService(snapshots); }

    @Test
    void rejectsUnsupportedRanges() {
        assertThrows(IllegalArgumentException.class, () -> service.getHistory(7L, "all"));
    }

    @Test
    void queriesOnlyTheAuthenticatedUsersRealSnapshotsWithinRequestedRange() {
        when(snapshots.findAllByUserIdAndSnapshotDateGreaterThanEqualAndSnapshotDateLessThanEqualAndSourceEndingWithOrderBySnapshotDateAscPlatformAscSnapshottedAtAsc(
                42L, LocalDate.now(ZoneOffset.UTC).minusDays(29), LocalDate.now(ZoneOffset.UTC), "_REAL")).thenReturn(List.of());

        DashboardHistoryResponse response = service.getHistory(42L, "30d");

        verify(snapshots).findAllByUserIdAndSnapshotDateGreaterThanEqualAndSnapshotDateLessThanEqualAndSourceEndingWithOrderBySnapshotDateAscPlatformAscSnapshottedAtAsc(
                42L, LocalDate.now(ZoneOffset.UTC).minusDays(29), LocalDate.now(ZoneOffset.UTC), "_REAL");
        assertEquals("30d", response.range());
        assertTrue(response.snapshotDates().isEmpty());
        assertTrue(response.overallSolved().isEmpty());
        assertTrue(response.platforms().isEmpty());
        assertEquals(0, response.freshness().snapshotCount());
        assertNull(response.freshness().lastUpdatedAt());
    }

    @Test
    void singleSnapshotHasNoInventedChangesOrDifficultyValues() {
        LocalDate date = LocalDate.now(ZoneOffset.UTC).minusDays(1);
        PlatformStatSnapshot only = snapshot(1L, "LEETCODE", date, 88, null, null, null, 1500, "LEETCODE_REAL");
        stubSnapshots(9L, List.of(only));

        DashboardHistoryResponse response = service.getHistory(9L, "7d");

        assertEquals(List.of(date), response.snapshotDates());
        var point = response.platforms().get(0).points().get(0);
        assertNull(point.totalSolvedChange());
        assertNull(point.easySolved());
        assertNull(point.easySolvedChange());
        assertEquals(1500, point.rating());
        assertEquals(1, response.freshness().snapshotCount());
    }

    @Test
    void aggregatesPlatformsDeduplicatesSameDateAndPreservesNegativeCorrections() {
        LocalDate earlier = LocalDate.now(ZoneOffset.UTC).minusDays(2);
        LocalDate common = earlier.plusDays(1);
        PlatformStatSnapshot lcOld = snapshot(1L, "LEETCODE", common, 110, 55, 35, 20, 1500, "LEETCODE_REAL");
        lcOld.setSnapshottedAt(LocalDateTime.of(common, java.time.LocalTime.NOON));
        PlatformStatSnapshot lcLatest = snapshot(2L, "LEETCODE", common, 95, 50, 30, 15, 1400, "LEETCODE_REAL");
        lcLatest.setSnapshottedAt(LocalDateTime.of(common, java.time.LocalTime.NOON).plusHours(1));
        PlatformStatSnapshot lcPrevious = snapshot(3L, "LEETCODE", earlier, 100, null, null, null, 1500, "LEETCODE_REAL");
        PlatformStatSnapshot cf = snapshot(4L, "CODEFORCES", common, 200, 120, 80, null, null, "CODEFORCES_REAL");
        PlatformStatSnapshot mock = snapshot(5L, "CODECHEF", common, 999, 500, 300, 199, 1700, "MOCK");
        stubSnapshots(5L, List.of(lcPrevious, lcOld, lcLatest, cf, mock));

        DashboardHistoryResponse response = service.getHistory(5L, "90d");

        assertEquals(List.of(earlier, common), response.snapshotDates());
        assertEquals(2, response.platforms().size());
        var lc = response.platforms().stream().filter(series -> series.platform().equals("LEETCODE")).findFirst().orElseThrow();
        assertEquals(2, lc.points().size());
        assertEquals(95, lc.points().get(1).totalSolved());
        assertEquals(-5, lc.points().get(1).totalSolvedChange());
        assertEquals(1400, lc.points().get(1).rating());
        var overall = response.overallSolved().get(1);
        assertEquals(295, overall.totalSolved());
        assertEquals(2, overall.observedPlatforms());
        assertEquals(2, overall.platformsWithTotalSolved());
        assertEquals(170, overall.easySolved());
        assertEquals(4, response.freshness().snapshotCount()); // persisted real rows, including the duplicate observation
    }

    @Test
    void duplicateDateUsesLatestObservationEvenWhenDifficultyAndRatingAreMissing() {
        LocalDate date = LocalDate.now(ZoneOffset.UTC);
        PlatformStatSnapshot withData = snapshot(1L, "CODEFORCES", date, 10, null, null, null, 1200, "CODEFORCES_REAL");
        withData.setSnapshottedAt(LocalDateTime.of(date, java.time.LocalTime.NOON));
        PlatformStatSnapshot laterNulls = snapshot(2L, "CODEFORCES", date, 11, null, null, null, null, "CODEFORCES_REAL");
        laterNulls.setSnapshottedAt(LocalDateTime.of(date, java.time.LocalTime.NOON).plusHours(2));
        stubSnapshots(3L, List.of(withData, laterNulls));

        var response = service.getHistory(3L, "1y");

        var point = response.platforms().get(0).points().get(0);
        assertEquals(11, point.totalSolved());
        assertNull(point.rating());
        assertNull(point.easySolved());
        assertEquals(1, response.overallSolved().get(0).observedPlatforms());
    }

    private void stubSnapshots(Long userId, List<PlatformStatSnapshot> rows) {
        when(snapshots.findAllByUserIdAndSnapshotDateGreaterThanEqualAndSnapshotDateLessThanEqualAndSourceEndingWithOrderBySnapshotDateAscPlatformAscSnapshottedAtAsc(
                org.mockito.ArgumentMatchers.eq(userId), org.mockito.ArgumentMatchers.any(LocalDate.class),
                org.mockito.ArgumentMatchers.any(LocalDate.class), org.mockito.ArgumentMatchers.eq("_REAL"))).thenReturn(rows);
    }

    private PlatformStatSnapshot snapshot(Long id, String platform, LocalDate date, Integer total,
                                          Integer easy, Integer medium, Integer hard, Integer rating, String source) {
        PlatformStatSnapshot row = new PlatformStatSnapshot();
        row.setId(id);
        row.setPlatform(platform);
        row.setSnapshotDate(date);
        row.setTotalSolved(total);
        row.setEasySolved(easy);
        row.setMediumSolved(medium);
        row.setHardSolved(hard);
        row.setRating(rating);
        row.setSource(source);
        row.setSnapshottedAt(LocalDateTime.of(date, java.time.LocalTime.NOON));
        row.setStatsLastSyncedAt(row.getSnapshottedAt());
        return row;
    }
}

package com.kodbtw.service;

import com.kodbtw.dto.activity.DashboardActivityResponse;
import com.kodbtw.dto.contests.DashboardContestIntelligenceResponse;
import com.kodbtw.entity.PlatformStatSnapshot;
import com.kodbtw.repository.PlatformStatSnapshotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardActivityContestServiceTest {
    @Mock private PlatformStatSnapshotRepository snapshots;
    private DashboardActivityContestService service;

    @BeforeEach
    void setUp() { service = new DashboardActivityContestService(snapshots); }

    @Test
    void noSnapshotsReportsActivityAndContestEventsUnavailable() {
        when(snapshots.findAllByUserId(1L)).thenReturn(List.of());

        DashboardActivityResponse activity = service.getActivity(1L);
        DashboardContestIntelligenceResponse contests = service.getContestIntelligence(1L);

        assertFalse(activity.available());
        assertEquals("Activity history is not available from the connected platform yet.", activity.message());
        assertTrue(activity.platformsWithRealSnapshots().isEmpty());
        assertFalse(contests.contestHistoryAvailable());
        assertEquals("Contest history is not available from the connected platform yet.", contests.contestHistoryMessage());
        assertTrue(contests.platforms().isEmpty());
        assertEquals(0, contests.ratingObservationCount());
        verify(snapshots, org.mockito.Mockito.times(2)).findAllByUserId(1L);
    }

    @Test
    void datedSnapshotsDoNotBecomeActivityEvents() {
        when(snapshots.findAllByUserId(2L)).thenReturn(List.of(
                snapshot(1L, "LEETCODE", "LEETCODE_REAL", LocalDate.of(2026, 10, 1), 20, 4)));

        DashboardActivityResponse response = service.getActivity(2L);

        assertFalse(response.available());
        assertEquals(List.of("LEETCODE"), response.platformsWithRealSnapshots());
        assertTrue(response.message().contains("not available"));
    }

    @Test
    void activityEvidenceIsRealOnlyAndMissingDatesAreIgnored() {
        when(snapshots.findAllByUserId(3L)).thenReturn(List.of(
                snapshot(1L, "LEETCODE", "LEETCODE_REAL", LocalDate.of(2026, 10, 1), 20, 4),
                snapshot(2L, "CODECHEF", "CODECHEF_MOCK", LocalDate.of(2026, 10, 1), 30, 2),
                snapshot(3L, "CODEFORCES", "CODEFORCES_REAL", null, 100, 3)));

        DashboardActivityResponse response = service.getActivity(3L);

        assertTrue(response.realDataOnly());
        assertEquals(List.of("LEETCODE"), response.platformsWithRealSnapshots());
    }

    @Test
    void singleRealRatingObservationIsBaselineWithoutChange() {
        when(snapshots.findAllByUserId(4L)).thenReturn(List.of(
                snapshot(1L, "CODEFORCES", "CODEFORCES_REAL", LocalDate.of(2026, 10, 1), 1450, 1)));

        DashboardContestIntelligenceResponse response = service.getContestIntelligence(4L);

        var platform = response.platforms().get(0);
        assertEquals(1, platform.observationCount());
        assertEquals(1450, platform.currentRating());
        assertEquals(1450, platform.highestObservedRating());
        assertEquals(1450, platform.lowestObservedRating());
        assertNull(platform.change());
        assertFalse(response.contestHistoryAvailable());
        assertEquals(1, response.ratingObservationCount());
    }

    @Test
    void multipleRatingsCompareActualObservationsAndPreserveDecrease() {
        when(snapshots.findAllByUserId(5L)).thenReturn(List.of(
                snapshot(1L, "CODEFORCES", "CODEFORCES_REAL", LocalDate.of(2026, 1, 1), 1700, 1),
                snapshot(2L, "CODEFORCES", "CODEFORCES_REAL", LocalDate.of(2026, 4, 1), 1800, 2),
                snapshot(3L, "CODEFORCES", "CODEFORCES_REAL", LocalDate.of(2026, 8, 1), 1600, 3)));

        var response = service.getContestIntelligence(5L);
        var platform = response.platforms().get(0);

        assertEquals(3, platform.observationCount());
        assertEquals(1600, platform.currentRating());
        assertEquals(1800, platform.highestObservedRating());
        assertEquals(1600, platform.lowestObservedRating());
        assertEquals(-100, platform.change());
        assertEquals(List.of(1700, 1800, 1600), platform.observations().stream()
                .map(DashboardContestIntelligenceResponse.RatingObservation::rating).toList());
        assertFalse(response.contestHistoryAvailable());
    }

    @Test
    void mockRowsNeverContributeToContestRatings() {
        when(snapshots.findAllByUserId(6L)).thenReturn(List.of(
                snapshot(1L, "LEETCODE", "LEETCODE_MOCK", LocalDate.of(2026, 10, 1), 9999, 1),
                snapshot(2L, "CODEFORCES", "CODEFORCES_MOCK_REAL", LocalDate.of(2026, 10, 1), 9999, 2)));

        var response = service.getContestIntelligence(6L);

        assertTrue(response.platforms().isEmpty());
        assertEquals(0, response.ratingObservationCount());
    }

    @Test
    void multiplePlatformsRemainIndependentAndSparseDatesAreNotFilled() {
        when(snapshots.findAllByUserId(7L)).thenReturn(List.of(
                snapshot(1L, "CODEFORCES", "CODEFORCES_REAL", LocalDate.of(2025, 1, 1), 1300, 1),
                snapshot(2L, "CODEFORCES", "CODEFORCES_REAL", LocalDate.of(2026, 1, 1), 1450, 2),
                snapshot(3L, "LEETCODE", "LEETCODE_REAL", LocalDate.of(2026, 2, 1), 1900, 3),
                snapshot(4L, "LEETCODE", "LEETCODE_REAL", LocalDate.of(2026, 9, 1), null, 4)));

        var response = service.getContestIntelligence(7L);

        assertEquals(2, response.platforms().size());
        var codeforces = response.platforms().stream().filter(p -> p.platform().equals("CODEFORCES")).findFirst().orElseThrow();
        var leetcode = response.platforms().stream().filter(p -> p.platform().equals("LEETCODE")).findFirst().orElseThrow();
        assertEquals(150, codeforces.change());
        assertNull(leetcode.currentRating());
        assertEquals(LocalDate.of(2026, 9, 1), leetcode.latestSnapshotDate());
        assertNull(leetcode.change());
        assertEquals(1, leetcode.observationCount());
        assertEquals(List.of(LocalDate.of(2026, 2, 1)), leetcode.observations().stream()
                .map(DashboardContestIntelligenceResponse.RatingObservation::date).toList());
    }

    @Test
    void duplicatePlatformDateKeepsLatestSnapshotAndNullRatingDoesNotCreateObservation() {
        PlatformStatSnapshot earlier = snapshot(1L, "CODEFORCES", "CODEFORCES_REAL", LocalDate.of(2026, 10, 1), 1200, 1);
        earlier.setSnapshottedAt(LocalDateTime.of(2026, 10, 1, 10, 0));
        PlatformStatSnapshot later = snapshot(2L, "CODEFORCES", "CODEFORCES_REAL", LocalDate.of(2026, 10, 1), 1250, 2);
        later.setSnapshottedAt(LocalDateTime.of(2026, 10, 1, 11, 0));
        PlatformStatSnapshot missing = snapshot(3L, "CODEFORCES", "CODEFORCES_REAL", LocalDate.of(2026, 10, 2), null, 3);
        when(snapshots.findAllByUserId(8L)).thenReturn(List.of(earlier, later, missing));

        var response = service.getContestIntelligence(8L);

        assertEquals(1, response.ratingObservationCount());
        assertEquals(1, response.platforms().get(0).observationCount());
        assertNull(response.platforms().get(0).currentRating());
        assertEquals(1250, response.platforms().get(0).observations().get(0).rating());
        assertNull(response.platforms().get(0).change());
    }

    @Test
    void missingLatestRatingStaysUnknownInsteadOfUsingOlderValueAsCurrent() {
        when(snapshots.findAllByUserId(9L)).thenReturn(List.of(
                snapshot(1L, "CODEFORCES", "CODEFORCES_REAL", LocalDate.of(2026, 10, 1), 1500, 1),
                snapshot(2L, "CODEFORCES", "CODEFORCES_REAL", LocalDate.of(2026, 10, 2), null, 2)));

        var response = service.getContestIntelligence(9L);
        var platform = response.platforms().get(0);

        assertNull(platform.currentRating());
        assertEquals(LocalDate.of(2026, 10, 2), platform.latestSnapshotDate());
        assertEquals(1500, platform.observations().get(0).rating());
        assertEquals(1, platform.observationCount());
        assertNull(platform.change());
    }

    @Test
    void everyReadIsScopedToTheAuthenticatedUser() {
        when(snapshots.findAllByUserId(42L)).thenReturn(List.of());

        service.getActivity(42L);
        service.getContestIntelligence(42L);

        verify(snapshots, org.mockito.Mockito.times(2)).findAllByUserId(42L);
    }

    private static PlatformStatSnapshot snapshot(Long id, String platform, String source,
                                                  LocalDate date, Integer rating, long hour) {
        PlatformStatSnapshot row = new PlatformStatSnapshot();
        row.setId(id);
        row.setPlatform(platform);
        row.setSource(source);
        row.setSnapshotDate(date);
        row.setRating(rating);
        if (date != null) row.setSnapshottedAt(date.atTime((int) hour, 0));
        return row;
    }
}

package com.kodbtw.service;

import com.kodbtw.dto.DashboardStatsResponse;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;
import com.kodbtw.entity.PlatformStatSnapshot;
import com.kodbtw.entity.User;
import com.kodbtw.exception.ResourceNotFoundException;
import com.kodbtw.repository.PlatformAccountRepository;
import com.kodbtw.repository.PlatformStatSnapshotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlatformStatsServiceTest {
    @Mock PlatformAccountRepository accounts;
    @Mock PlatformStatSnapshotRepository snapshots;
    private PlatformStatsService service;

    @BeforeEach void setUp() { service = new PlatformStatsService(accounts, snapshots); }

    @Test void statsAreReadFromPersistedSnapshotAndPreserveNullsAndSource() {
        User user = new User(); user.setId(4L);
        PlatformAccount account = new PlatformAccount();
        account.setId(8L); account.setUser(user); account.setPlatform(Platform.CODEFORCES); account.setUsername("handle");
        PlatformStatSnapshot snapshot = new PlatformStatSnapshot();
        snapshot.setPlatform("CODEFORCES"); snapshot.setSource("CODEFORCES_REAL");
        snapshot.setRating(1900); snapshot.setTotalSolved(null); snapshot.setRank(2200);
        when(accounts.findByIdAndUserId(8L, 4L)).thenReturn(Optional.of(account));
        when(snapshots.findFirstByUserIdAndPlatformOrderBySnapshotDateDesc(4L, "CODEFORCES"))
                .thenReturn(Optional.of(snapshot));
        PlatformStats result = service.getStats(4L, 8L);
        assertEquals(1900, result.getRating()); assertEquals(2200, result.getRank());
        assertNull(result.getTotalProblemsSolved()); assertEquals("CODEFORCES_REAL", result.getSource());
    }

    @Test void codeChefThirdPartySnapshotIsReturnedWithoutInventingUnsupportedMetrics() {
        User user = new User(); user.setId(4L);
        PlatformAccount account = new PlatformAccount();
        account.setId(8L); account.setUser(user); account.setPlatform(Platform.CODECHEF);
        account.setUsername("sajalgoyal2007");
        LocalDateTime syncedAt = LocalDateTime.of(2026, 10, 10, 12, 30);
        PlatformStatSnapshot snapshot = new PlatformStatSnapshot();
        snapshot.setPlatform("CODECHEF"); snapshot.setSource("CODECHEF_THIRD_PARTY");
        snapshot.setTotalSolved(656); snapshot.setRating(1247); snapshot.setMaxRating(1519);
        snapshot.setRank(76970); snapshot.setStatsLastSyncedAt(syncedAt);
        when(accounts.findByIdAndUserId(8L, 4L)).thenReturn(Optional.of(account));
        when(snapshots.findFirstByUserIdAndPlatformOrderBySnapshotDateDesc(4L, "CODECHEF"))
                .thenReturn(Optional.of(snapshot));

        PlatformStats result = service.getStats(4L, 8L);

        assertEquals(656, result.getTotalProblemsSolved());
        assertEquals(1247, result.getRating());
        assertEquals(1519, result.getMaxRating());
        assertEquals(76970, result.getRank());
        assertEquals("CODECHEF_THIRD_PARTY", result.getSource());
        assertEquals(syncedAt, result.getLastSyncedAt());
        assertNull(result.getEasySolved());
        assertNull(result.getMediumSolved());
        assertNull(result.getHardSolved());
        assertNull(result.getContestsParticipated());
        assertNull(result.getCurrentStreak());
        assertNull(result.getLongestStreak());
    }

    @Test void ownershipIsRequired() {
        when(accounts.findByIdAndUserId(8L, 4L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getStats(4L, 8L));
        verifyNoInteractions(snapshots);
    }

    @Test void dashboardReadsPersistedCurrentStats() {
        User user = new User(); user.setId(4L);
        PlatformAccount account = new PlatformAccount();
        account.setUser(user); account.setPlatform(Platform.LEETCODE); account.setUsername("lc");
        PlatformStatSnapshot snapshot = new PlatformStatSnapshot();
        snapshot.setPlatform("LEETCODE"); snapshot.setSource("LEETCODE_REAL");
        snapshot.setTotalSolved(123); snapshot.setEasySolved(40); snapshot.setRating(1500);
        when(accounts.findAllByUserId(4L)).thenReturn(List.of(account));
        when(snapshots.findFirstByUserIdAndPlatformOrderBySnapshotDateDesc(4L, "LEETCODE"))
                .thenReturn(Optional.of(snapshot));
        DashboardStatsResponse result = service.getDashboardStats(4L);
        assertEquals(123, result.getOverview().getTotalProblemsSolved());
        assertEquals(40, result.getOverview().getEasySolved());
        assertEquals("LEETCODE_REAL", result.getPlatforms().get(0).getSource());
    }

    @Test void missingSnapshotIsUnsyncedAndDoesNotFabricateMetrics() {
        User user = new User(); user.setId(4L);
        PlatformAccount account = new PlatformAccount();
        account.setUser(user); account.setPlatform(Platform.GEEKSFORGEEKS); account.setUsername("gfg");
        when(accounts.findAllByUserId(4L)).thenReturn(List.of(account));
        when(snapshots.findFirstByUserIdAndPlatformOrderBySnapshotDateDesc(4L, "GEEKSFORGEEKS"))
                .thenReturn(Optional.empty());
        PlatformStats result = service.getDashboardStats(4L).getPlatforms().get(0);
        assertEquals("SOURCE_PENDING", result.getSource()); assertNull(result.getRating());
        assertNull(result.getLastSyncedAt());
        assertNull(service.getDashboardStats(4L).getOverview().getTotalProblemsSolved());
    }

    @Test void dashboardAggregatesPersistedPlatformsAndUsesMaximumStreaks() {
        User user = new User(); user.setId(4L);
        PlatformAccount leetCode = new PlatformAccount();
        leetCode.setUser(user); leetCode.setPlatform(Platform.LEETCODE); leetCode.setUsername("lc");
        PlatformAccount geeksForGeeks = new PlatformAccount();
        geeksForGeeks.setUser(user); geeksForGeeks.setPlatform(Platform.GEEKSFORGEEKS); geeksForGeeks.setUsername("gfg");
        PlatformStatSnapshot real = snapshot("LEETCODE", "LEETCODE_REAL");
        real.setTotalSolved(120); real.setEasySolved(30); real.setMediumSolved(50); real.setHardSolved(40);
        real.setContests(4); real.setCurrentStreak(8); real.setLongestStreak(18);
        PlatformStatSnapshot mock = snapshot("GEEKSFORGEEKS", "MOCK");
        mock.setTotalSolved(80); mock.setEasySolved(20); mock.setMediumSolved(40); mock.setHardSolved(20);
        mock.setContests(6); mock.setCurrentStreak(3); mock.setLongestStreak(25);
        when(accounts.findAllByUserId(4L)).thenReturn(List.of(leetCode, geeksForGeeks));
        when(snapshots.findFirstByUserIdAndPlatformOrderBySnapshotDateDesc(4L, "LEETCODE"))
                .thenReturn(Optional.of(real));
        when(snapshots.findFirstByUserIdAndPlatformOrderBySnapshotDateDesc(4L, "GEEKSFORGEEKS"))
                .thenReturn(Optional.of(mock));

        DashboardStatsResponse result = service.getDashboardStats(4L);
        assertEquals(120, result.getOverview().getTotalProblemsSolved());
        assertEquals(30, result.getOverview().getEasySolved());
        assertEquals(4, result.getOverview().getContestsParticipated());
        assertEquals(8, result.getOverview().getCurrentStreak());
        assertEquals(18, result.getOverview().getLongestStreak());
        assertEquals("SOURCE_PENDING", result.getPlatforms().get(1).getSource());
        assertNull(result.getPlatforms().get(1).getTotalProblemsSolved());
    }

    @Test void dashboardWithNoAccountsReturnsEmptyOverview() {
        when(accounts.findAllByUserId(4L)).thenReturn(List.of());
        DashboardStatsResponse result = service.getDashboardStats(4L);
        assertEquals(0, result.getOverview().getConnectedPlatformsCount());
        assertEquals(0, result.getOverview().getTotalProblemsSolved());
        assertNull(result.getOverview().getCurrentStreak());
        assertTrue(result.getPlatforms().isEmpty());
    }

    private PlatformStatSnapshot snapshot(String platform, String source) {
        PlatformStatSnapshot snapshot = new PlatformStatSnapshot();
        snapshot.setPlatform(platform);
        snapshot.setSource(source);
        return snapshot;
    }
}

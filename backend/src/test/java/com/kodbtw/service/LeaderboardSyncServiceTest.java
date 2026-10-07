package com.kodbtw.service;

import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.LeaderboardUserCache;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;
import com.kodbtw.entity.Profile;
import com.kodbtw.entity.User;
import com.kodbtw.repository.LeaderboardUserCacheRepository;
import com.kodbtw.repository.PlatformAccountRepository;
import com.kodbtw.repository.PlatformStatSnapshotRepository;
import com.kodbtw.repository.ProfileRepository;
import com.kodbtw.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaderboardSyncServiceTest {

    @Mock
    private PlatformAccountRepository platformAccountRepository;

    @Mock
    private PlatformStatSnapshotRepository snapshotRepository;

    @Mock
    private LeaderboardUserCacheRepository cacheRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PlatformStatsService platformStatsService;

    private LeaderboardSyncService syncService;

    @BeforeEach
    void setUp() {
        syncService = new LeaderboardSyncService(
                platformAccountRepository,
                snapshotRepository,
                cacheRepository,
                profileRepository,
                userRepository,
                platformStatsService
        );
    }

    @Test
    void testComputeWds() {
        assertEquals(100.0, LeaderboardSyncService.computeWds(10, 20, 5));
        assertEquals(0.0, LeaderboardSyncService.computeWds(null, null, null));
        assertEquals(15.0, LeaderboardSyncService.computeWds(null, 5, null));
        assertEquals(6.0, LeaderboardSyncService.computeWds(null, null, 1));
    }

    @Test
    void testSyncUser_RealPlatformsOnly() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setName("Test User");

        Profile profile = new Profile();
        profile.setUser(user);
        profile.setDisplayName("Test Dev");
        profile.setCollege("IIT Delhi");

        PlatformAccount lcAccount = new PlatformAccount();
        lcAccount.setId(10L);
        lcAccount.setUser(user);
        lcAccount.setPlatform(Platform.LEETCODE);
        lcAccount.setUsername("alice");

        PlatformStats lcStats = new PlatformStats();
        lcStats.setPlatform(Platform.LEETCODE);
        lcStats.setUsername("alice");
        lcStats.setTotalProblemsSolved(150);
        lcStats.setEasySolved(50);
        lcStats.setMediumSolved(80);
        lcStats.setHardSolved(20);
        lcStats.setRating(1650);
        lcStats.setContestsParticipated(12);
        lcStats.setCurrentStreak(5);
        lcStats.setSource("LEETCODE_REAL");

        when(platformAccountRepository.findAllByUserId(userId)).thenReturn(List.of(lcAccount));
        when(platformStatsService.fetchStats(lcAccount)).thenReturn(lcStats);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(cacheRepository.findById(userId)).thenReturn(Optional.empty());

        syncService.syncUser(userId);

        // Verify snapshot upserted
        verify(snapshotRepository).upsert(
                eq(userId),
                eq("LEETCODE"),
                eq(150),
                eq(50),
                eq(80),
                eq(20),
                eq(1650),
                eq(12),
                eq(5),
                eq("LEETCODE_REAL")
        );

        // Verify cache saved with real metrics
        ArgumentCaptor<LeaderboardUserCache> cacheCaptor = ArgumentCaptor.forClass(LeaderboardUserCache.class);
        verify(cacheRepository).save(cacheCaptor.capture());

        LeaderboardUserCache saved = cacheCaptor.getValue();
        assertEquals("Test Dev", saved.getDisplayName());
        assertEquals("IIT Delhi", saved.getCollege());
        assertEquals(150, saved.getTotalSolved());
        // 50*1 + 80*3 + 20*6 = 50 + 240 + 120 = 410.0
        assertEquals(410.0, saved.getWeightedScore());
        assertEquals(1650, saved.getBestRating());
        assertEquals("LEETCODE", saved.getBestRatingPlatform());
        assertEquals(12, saved.getTotalContests());
        assertTrue(saved.getRealDataOnly());
        assertFalse(saved.getHasMockData());
    }

    @Test
    void testSyncUser_WithMockPlatform_ExcludedFromScore() {
        Long userId = 2L;
        User user = new User();
        user.setId(userId);
        user.setName("Mock User");

        PlatformAccount mockAccount = new PlatformAccount();
        mockAccount.setId(20L);
        mockAccount.setUser(user);
        mockAccount.setPlatform(Platform.CODECHEF);
        mockAccount.setUsername("bob");

        PlatformStats mockStats = new PlatformStats();
        mockStats.setPlatform(Platform.CODECHEF);
        mockStats.setUsername("bob");
        mockStats.setTotalProblemsSolved(210);
        mockStats.setEasySolved(100);
        mockStats.setMediumSolved(80);
        mockStats.setHardSolved(30);
        mockStats.setRating(1750);
        mockStats.setSource("MOCK");

        when(platformAccountRepository.findAllByUserId(userId)).thenReturn(List.of(mockAccount));
        when(platformStatsService.fetchStats(mockAccount)).thenReturn(mockStats);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(cacheRepository.findById(userId)).thenReturn(Optional.empty());

        syncService.syncUser(userId);

        ArgumentCaptor<LeaderboardUserCache> cacheCaptor = ArgumentCaptor.forClass(LeaderboardUserCache.class);
        verify(cacheRepository).save(cacheCaptor.capture());

        LeaderboardUserCache saved = cacheCaptor.getValue();
        assertEquals("Mock User", saved.getDisplayName()); // fallback to user.name
        assertEquals(0, saved.getTotalSolved()); // mock excluded
        assertEquals(0.0, saved.getWeightedScore()); // mock excluded
        assertNull(saved.getBestRating()); // mock excluded
        assertFalse(saved.getRealDataOnly());
        assertTrue(saved.getHasMockData());
    }

    @Test
    void testSyncUser_HandlesFetchExceptionGracefully() {
        Long userId = 3L;
        User user = new User();
        user.setId(userId);
        user.setName("Error User");

        PlatformAccount badAccount = new PlatformAccount();
        badAccount.setId(30L);
        badAccount.setUser(user);
        badAccount.setPlatform(Platform.CODEFORCES);
        badAccount.setUsername("nonexistent");

        when(platformAccountRepository.findAllByUserId(userId)).thenReturn(List.of(badAccount));
        when(platformStatsService.fetchStats(badAccount)).thenThrow(new RuntimeException("API rate limit exceeded"));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(cacheRepository.findById(userId)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> syncService.syncUser(userId));

        // Snapshot is NOT upserted when fetch fails
        verify(snapshotRepository, never()).upsert(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        verify(cacheRepository).save(any(LeaderboardUserCache.class));
    }

    @Test
    void testSyncUser_NoAccounts_EarlyReturn() {
        when(platformAccountRepository.findAllByUserId(4L)).thenReturn(Collections.emptyList());

        syncService.syncUser(4L);

        verify(snapshotRepository, never()).upsert(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        verify(cacheRepository, never()).save(any());
    }

    @Test
    void testSyncAllUsers() {
        when(platformAccountRepository.findAllDistinctUserIds()).thenReturn(List.of(1L, 2L));
        when(platformAccountRepository.findAllByUserId(anyLong())).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> syncService.syncAllUsers());

        verify(platformAccountRepository).findAllByUserId(1L);
        verify(platformAccountRepository).findAllByUserId(2L);
    }

    @Test
    void testIsRealPlatform() {
        PlatformStats realLc = PlatformStats.builder().source("LEETCODE_REAL").build();
        PlatformStats realCf = PlatformStats.builder().source("CODEFORCES_REAL").build();
        PlatformStats mockCc = PlatformStats.builder().source("MOCK").build();
        PlatformStats nullSrc = PlatformStats.builder().source(null).build();
        PlatformStats blankSrc = PlatformStats.builder().source("   ").build();
        PlatformStats unknownSrc = PlatformStats.builder().source("OTHER").build();

        assertTrue(LeaderboardSyncService.isRealPlatform(realLc));
        assertTrue(LeaderboardSyncService.isRealPlatform(realCf));
        assertFalse(LeaderboardSyncService.isRealPlatform(mockCc));
        assertFalse(LeaderboardSyncService.isRealPlatform(nullSrc));
        assertFalse(LeaderboardSyncService.isRealPlatform(blankSrc));
        assertFalse(LeaderboardSyncService.isRealPlatform(unknownSrc));
        assertFalse(LeaderboardSyncService.isRealPlatform(null));
    }

    @Test
    void testSyncUser_NullMetrics_RealPlatform_DoesNotFabricateData() {
        Long userId = 5L;
        User user = new User();
        user.setId(userId);
        user.setName("Codeforces Expert");

        PlatformAccount cfAccount = new PlatformAccount();
        cfAccount.setId(50L);
        cfAccount.setUser(user);
        cfAccount.setPlatform(Platform.CODEFORCES);
        cfAccount.setUsername("tourist");

        // Codeforces adapter returns null for problem counts and difficulties
        PlatformStats cfStats = PlatformStats.builder()
                .platform(Platform.CODEFORCES)
                .username("tourist")
                .totalProblemsSolved(null)
                .easySolved(null)
                .mediumSolved(null)
                .hardSolved(null)
                .rating(1950)
                .contestsParticipated(null)
                .currentStreak(null)
                .source("CODEFORCES_REAL")
                .build();

        when(platformAccountRepository.findAllByUserId(userId)).thenReturn(List.of(cfAccount));
        when(platformStatsService.fetchStats(cfAccount)).thenReturn(cfStats);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(cacheRepository.findById(userId)).thenReturn(Optional.empty());

        syncService.syncUser(userId);

        // Verify snapshot preserves nulls
        verify(snapshotRepository).upsert(
                eq(userId),
                eq("CODEFORCES"),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq(1950),
                isNull(),
                isNull(),
                eq("CODEFORCES_REAL")
        );

        // Verify cache row does NOT fabricate scores
        ArgumentCaptor<LeaderboardUserCache> cacheCaptor = ArgumentCaptor.forClass(LeaderboardUserCache.class);
        verify(cacheRepository).save(cacheCaptor.capture());

        LeaderboardUserCache saved = cacheCaptor.getValue();
        assertEquals(0, saved.getTotalSolved());
        assertEquals(0.0, saved.getWeightedScore());
        assertEquals(1950, saved.getBestRating());
        assertEquals("CODEFORCES", saved.getBestRatingPlatform());
        assertEquals(0, saved.getTotalContests());
        assertTrue(saved.getRealDataOnly());
        assertFalse(saved.getHasMockData());
    }

    @Test
    void testSyncAllUsers_ErrorIsolationAcrossUsers() {
        when(platformAccountRepository.findAllDistinctUserIds()).thenReturn(List.of(1L, 2L));

        User user2 = new User();
        user2.setId(2L);
        user2.setName("User Two");

        // User 1 throws an unhandled RuntimeException during account lookup
        when(platformAccountRepository.findAllByUserId(1L)).thenThrow(new RuntimeException("DB Connection timeout"));

        // User 2 has valid account and succeeds
        when(platformAccountRepository.findAllByUserId(2L)).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> syncService.syncAllUsers());

        // Verifies user 2 was still processed despite user 1 failure
        verify(platformAccountRepository).findAllByUserId(1L);
        verify(platformAccountRepository).findAllByUserId(2L);
    }
}

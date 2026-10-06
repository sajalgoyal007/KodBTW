package com.kodbtw.service;

import com.kodbtw.adapter.PlatformAdapter;
import com.kodbtw.adapter.PlatformAdapterRegistry;
import com.kodbtw.dto.DashboardStatsResponse;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;
import com.kodbtw.exception.ResourceNotFoundException;
import com.kodbtw.repository.PlatformAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatformStatsServiceTest {

    @Mock
    private PlatformAccountRepository platformAccountRepository;

    @Mock
    private PlatformAdapter leetCodeAdapter;

    @Mock
    private PlatformAdapter codeforcesAdapter;

    @Mock
    private PlatformAdapter codeChefAdapter;

    private PlatformAdapterRegistry platformAdapterRegistry;
    private PlatformStatsService platformStatsService;

    @BeforeEach
    void setUp() {
        org.mockito.Mockito.lenient().when(leetCodeAdapter.getPlatform()).thenReturn(Platform.LEETCODE);
        org.mockito.Mockito.lenient().when(codeforcesAdapter.getPlatform()).thenReturn(Platform.CODEFORCES);
        org.mockito.Mockito.lenient().when(codeChefAdapter.getPlatform()).thenReturn(Platform.CODECHEF);
        platformAdapterRegistry = new PlatformAdapterRegistry(List.of(leetCodeAdapter, codeforcesAdapter, codeChefAdapter));
        platformStatsService = new PlatformStatsService(platformAccountRepository, platformAdapterRegistry);
    }

    @Test
    void getStatsShouldDelegateToCorrectAdapterWhenAccountFound() {
        Long userId = 1L;
        Long accountId = 10L;

        PlatformAccount account = new PlatformAccount();
        account.setId(accountId);
        account.setPlatform(Platform.LEETCODE);
        account.setUsername("testuser");

        PlatformStats expectedStats = PlatformStats.builder()
                .platform(Platform.LEETCODE)
                .username("testuser")
                .totalProblemsSolved(350)
                .source("MOCK")
                .build();

        when(platformAccountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(account));
        when(leetCodeAdapter.fetchStats(account)).thenReturn(expectedStats);

        PlatformStats result = platformStatsService.getStats(userId, accountId);

        assertNotNull(result);
        assertEquals(Platform.LEETCODE, result.getPlatform());
        assertEquals("testuser", result.getUsername());
        assertEquals(350, result.getTotalProblemsSolved());
        verify(leetCodeAdapter).fetchStats(account);
    }

    @Test
    void getStatsShouldThrowResourceNotFoundWhenAccountNotOwnedOrNotFound() {
        Long userId = 1L;
        Long accountId = 99L;

        when(platformAccountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> platformStatsService.getStats(userId, accountId));
    }

    @Test
    void getDashboardStats_whenNoConnectedAccounts_returnsEmptyState() {
        Long userId = 1L;
        when(platformAccountRepository.findAllByUserId(userId)).thenReturn(Collections.emptyList());

        DashboardStatsResponse response = platformStatsService.getDashboardStats(userId);

        assertNotNull(response);
        assertNotNull(response.getOverview());
        assertEquals(0, response.getOverview().getConnectedPlatformsCount());
        assertEquals(0, response.getOverview().getTotalProblemsSolved());
        assertEquals(0, response.getOverview().getEasySolved());
        assertEquals(0, response.getOverview().getMediumSolved());
        assertEquals(0, response.getOverview().getHardSolved());
        assertEquals(0, response.getOverview().getContestsParticipated());
        assertNull(response.getOverview().getCurrentStreak());
        assertNull(response.getOverview().getLongestStreak());
        assertTrue(response.getPlatforms().isEmpty());
    }

    @Test
    void getDashboardStats_whenSingleAccount_returnsMatchingOverview() {
        Long userId = 1L;
        PlatformAccount account = new PlatformAccount();
        account.setId(10L);
        account.setPlatform(Platform.LEETCODE);
        account.setUsername("sajalgoyal");

        PlatformStats stats = PlatformStats.builder()
                .platform(Platform.LEETCODE)
                .username("sajalgoyal")
                .totalProblemsSolved(300)
                .easySolved(100)
                .mediumSolved(150)
                .hardSolved(50)
                .contestsParticipated(5)
                .currentStreak(10)
                .longestStreak(20)
                .rating(1600)
                .rank(50000)
                .source("LEETCODE_REAL")
                .build();

        when(platformAccountRepository.findAllByUserId(userId)).thenReturn(List.of(account));
        when(leetCodeAdapter.fetchStats(account)).thenReturn(stats);

        DashboardStatsResponse response = platformStatsService.getDashboardStats(userId);

        assertNotNull(response);
        assertEquals(1, response.getOverview().getConnectedPlatformsCount());
        assertEquals(300, response.getOverview().getTotalProblemsSolved());
        assertEquals(100, response.getOverview().getEasySolved());
        assertEquals(150, response.getOverview().getMediumSolved());
        assertEquals(50, response.getOverview().getHardSolved());
        assertEquals(5, response.getOverview().getContestsParticipated());
        assertEquals(10, response.getOverview().getCurrentStreak());
        assertEquals(20, response.getOverview().getLongestStreak());

        assertEquals(1, response.getPlatforms().size());
        PlatformStats platformEntry = response.getPlatforms().get(0);
        assertEquals(Platform.LEETCODE, platformEntry.getPlatform());
        assertEquals(1600, platformEntry.getRating());
        assertEquals(50000, platformEntry.getRank());
        assertEquals("LEETCODE_REAL", platformEntry.getSource());
    }

    @Test
    void getDashboardStats_whenMultipleAccounts_aggregatesCorrectly() {
        Long userId = 1L;

        PlatformAccount lcAccount = new PlatformAccount();
        lcAccount.setId(1L);
        lcAccount.setPlatform(Platform.LEETCODE);
        lcAccount.setUsername("lcuser");

        PlatformAccount ccAccount = new PlatformAccount();
        ccAccount.setId(2L);
        ccAccount.setPlatform(Platform.CODECHEF);
        ccAccount.setUsername("ccuser");

        PlatformStats lcStats = PlatformStats.builder()
                .platform(Platform.LEETCODE)
                .username("lcuser")
                .totalProblemsSolved(300)
                .easySolved(100)
                .mediumSolved(150)
                .hardSolved(50)
                .contestsParticipated(5)
                .currentStreak(8)
                .longestStreak(15)
                .rating(1800)
                .source("LEETCODE_REAL")
                .build();

        PlatformStats ccStats = PlatformStats.builder()
                .platform(Platform.CODECHEF)
                .username("ccuser")
                .totalProblemsSolved(200)
                .easySolved(100)
                .mediumSolved(80)
                .hardSolved(20)
                .contestsParticipated(10)
                .currentStreak(3)
                .longestStreak(25)
                .rating(1750)
                .source("MOCK")
                .build();

        when(platformAccountRepository.findAllByUserId(userId)).thenReturn(List.of(lcAccount, ccAccount));
        when(leetCodeAdapter.fetchStats(lcAccount)).thenReturn(lcStats);
        when(codeChefAdapter.fetchStats(ccAccount)).thenReturn(ccStats);

        DashboardStatsResponse response = platformStatsService.getDashboardStats(userId);

        assertEquals(2, response.getOverview().getConnectedPlatformsCount());
        assertEquals(500, response.getOverview().getTotalProblemsSolved());
        assertEquals(200, response.getOverview().getEasySolved());
        assertEquals(230, response.getOverview().getMediumSolved());
        assertEquals(70, response.getOverview().getHardSolved());
        assertEquals(15, response.getOverview().getContestsParticipated());
        assertEquals(8, response.getOverview().getCurrentStreak());
        assertEquals(25, response.getOverview().getLongestStreak());

        assertEquals(2, response.getPlatforms().size());
    }

    @Test
    void getDashboardStats_whenPlatformHasNullMetrics_handlesGracefully() {
        Long userId = 1L;

        PlatformAccount lcAccount = new PlatformAccount();
        lcAccount.setId(1L);
        lcAccount.setPlatform(Platform.LEETCODE);
        lcAccount.setUsername("lcuser");

        PlatformAccount cfAccount = new PlatformAccount();
        cfAccount.setId(2L);
        cfAccount.setPlatform(Platform.CODEFORCES);
        cfAccount.setUsername("cfuser");

        PlatformStats lcStats = PlatformStats.builder()
                .platform(Platform.LEETCODE)
                .username("lcuser")
                .totalProblemsSolved(300)
                .easySolved(100)
                .mediumSolved(150)
                .hardSolved(50)
                .contestsParticipated(5)
                .currentStreak(7)
                .longestStreak(null)
                .source("LEETCODE_REAL")
                .build();

        PlatformStats cfStats = PlatformStats.builder()
                .platform(Platform.CODEFORCES)
                .username("cfuser")
                .totalProblemsSolved(null)
                .easySolved(null)
                .mediumSolved(null)
                .hardSolved(null)
                .contestsParticipated(null)
                .currentStreak(null)
                .longestStreak(null)
                .rating(1600)
                .rank(1650)
                .source("CODEFORCES_REAL")
                .build();

        when(platformAccountRepository.findAllByUserId(userId)).thenReturn(List.of(lcAccount, cfAccount));
        when(leetCodeAdapter.fetchStats(lcAccount)).thenReturn(lcStats);
        when(codeforcesAdapter.fetchStats(cfAccount)).thenReturn(cfStats);

        DashboardStatsResponse response = platformStatsService.getDashboardStats(userId);

        assertEquals(2, response.getOverview().getConnectedPlatformsCount());
        assertEquals(300, response.getOverview().getTotalProblemsSolved());
        assertEquals(100, response.getOverview().getEasySolved());
        assertEquals(150, response.getOverview().getMediumSolved());
        assertEquals(50, response.getOverview().getHardSolved());
        assertEquals(5, response.getOverview().getContestsParticipated());
        assertEquals(7, response.getOverview().getCurrentStreak());
        assertNull(response.getOverview().getLongestStreak());
    }

    @Test
    void getDashboardStats_aggregatesCurrentStreakAsMax() {
        Long userId = 1L;

        PlatformAccount acc1 = new PlatformAccount();
        acc1.setId(1L);
        acc1.setPlatform(Platform.LEETCODE);

        PlatformAccount acc2 = new PlatformAccount();
        acc2.setId(2L);
        acc2.setPlatform(Platform.CODECHEF);

        PlatformStats stats1 = PlatformStats.builder().platform(Platform.LEETCODE).currentStreak(4).build();
        PlatformStats stats2 = PlatformStats.builder().platform(Platform.CODECHEF).currentStreak(12).build();

        when(platformAccountRepository.findAllByUserId(userId)).thenReturn(List.of(acc1, acc2));
        when(leetCodeAdapter.fetchStats(acc1)).thenReturn(stats1);
        when(codeChefAdapter.fetchStats(acc2)).thenReturn(stats2);

        DashboardStatsResponse response = platformStatsService.getDashboardStats(userId);

        assertEquals(12, response.getOverview().getCurrentStreak());
    }

    @Test
    void getDashboardStats_aggregatesLongestStreakAsMax() {
        Long userId = 1L;

        PlatformAccount acc1 = new PlatformAccount();
        acc1.setId(1L);
        acc1.setPlatform(Platform.LEETCODE);

        PlatformAccount acc2 = new PlatformAccount();
        acc2.setId(2L);
        acc2.setPlatform(Platform.CODECHEF);

        PlatformStats stats1 = PlatformStats.builder().platform(Platform.LEETCODE).longestStreak(25).build();
        PlatformStats stats2 = PlatformStats.builder().platform(Platform.CODECHEF).longestStreak(40).build();

        when(platformAccountRepository.findAllByUserId(userId)).thenReturn(List.of(acc1, acc2));
        when(leetCodeAdapter.fetchStats(acc1)).thenReturn(stats1);
        when(codeChefAdapter.fetchStats(acc2)).thenReturn(stats2);

        DashboardStatsResponse response = platformStatsService.getDashboardStats(userId);

        assertEquals(40, response.getOverview().getLongestStreak());
    }

    @Test
    void getDashboardStats_whenAllStreaksNull_returnsNullStreak() {
        Long userId = 1L;

        PlatformAccount acc1 = new PlatformAccount();
        acc1.setId(1L);
        acc1.setPlatform(Platform.LEETCODE);

        PlatformAccount acc2 = new PlatformAccount();
        acc2.setId(2L);
        acc2.setPlatform(Platform.CODEFORCES);

        PlatformStats stats1 = PlatformStats.builder().platform(Platform.LEETCODE).currentStreak(null).longestStreak(null).build();
        PlatformStats stats2 = PlatformStats.builder().platform(Platform.CODEFORCES).currentStreak(null).longestStreak(null).build();

        when(platformAccountRepository.findAllByUserId(userId)).thenReturn(List.of(acc1, acc2));
        when(leetCodeAdapter.fetchStats(acc1)).thenReturn(stats1);
        when(codeforcesAdapter.fetchStats(acc2)).thenReturn(stats2);

        DashboardStatsResponse response = platformStatsService.getDashboardStats(userId);

        assertNull(response.getOverview().getCurrentStreak());
        assertNull(response.getOverview().getLongestStreak());
    }
}

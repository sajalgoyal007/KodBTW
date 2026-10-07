package com.kodbtw.service;

import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.*;
import com.kodbtw.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaderboardSyncServiceTest {
    @Mock PlatformAccountRepository accounts;
    @Mock LeaderboardUserCacheRepository cacheRepository;
    @Mock ProfileRepository profiles;
    @Mock UserRepository users;
    @Mock PlatformStatsService statsService;
    @Mock PlatformSyncService platformSyncService;
    private LeaderboardSyncService service;

    @BeforeEach void setUp() {
        service = new LeaderboardSyncService(accounts, cacheRepository, profiles, users, statsService, platformSyncService);
    }

    @Test void wdsHandlesNullMetrics() {
        assertEquals(410.0, LeaderboardSyncService.computeWds(50, 80, 20));
        assertEquals(0.0, LeaderboardSyncService.computeWds(null, null, null));
    }

    @Test void cachePreservesRealMockSemanticsAndNullMetrics() {
        User user = new User(); user.setId(1L); user.setName("Dev");
        PlatformAccount real = new PlatformAccount(); real.setUser(user); real.setPlatform(Platform.CODEFORCES);
        PlatformAccount mock = new PlatformAccount(); mock.setUser(user); mock.setPlatform(Platform.CODECHEF);
        PlatformStats realStats = PlatformStats.builder().platform(Platform.CODEFORCES).source("CODEFORCES_REAL")
                .rating(1800).totalProblemsSolved(null).easySolved(null).mediumSolved(null).hardSolved(null).build();
        PlatformStats mockStats = PlatformStats.builder().platform(Platform.CODECHEF).source("MOCK")
                .totalProblemsSolved(210).easySolved(100).build();
        when(accounts.findAllByUserId(1L)).thenReturn(List.of(real, mock));
        when(statsService.getPersistedStats(real)).thenReturn(realStats);
        when(statsService.getPersistedStats(mock)).thenReturn(mockStats);
        when(users.findById(1L)).thenReturn(Optional.of(user));
        when(profiles.findByUserId(1L)).thenReturn(Optional.empty());
        when(cacheRepository.findById(1L)).thenReturn(Optional.empty());
        service.refreshUserCache(1L);
        ArgumentCaptor<LeaderboardUserCache> captor = ArgumentCaptor.forClass(LeaderboardUserCache.class);
        verify(cacheRepository).save(captor.capture());
        LeaderboardUserCache cache = captor.getValue();
        assertEquals(0, cache.getTotalSolved()); assertEquals(1800, cache.getBestRating());
        assertFalse(cache.getRealDataOnly()); assertTrue(cache.getHasMockData());
    }

    @Test void realSourceRequiresRealSuffix() {
        assertTrue(LeaderboardSyncService.isRealPlatform(PlatformStats.builder().source("CODEFORCES_REAL").build()));
        assertFalse(LeaderboardSyncService.isRealPlatform(PlatformStats.builder().source("MOCK").build()));
        assertFalse(LeaderboardSyncService.isRealPlatform(PlatformStats.builder().source("UNSYNCED").build()));
    }

    @Test void syncUserRefreshesEachOwnedAccountAndIsolatesOneAccountError() {
        when(accounts.findAccountIdsByUserId(1L)).thenReturn(List.of(10L, 11L));
        doThrow(new RuntimeException("upstream failure")).when(platformSyncService).syncScheduledAccount(10L);
        when(accounts.findAllByUserId(1L)).thenReturn(List.of());

        assertDoesNotThrow(() -> service.syncUser(1L));
        var order = inOrder(platformSyncService);
        order.verify(platformSyncService).syncScheduledAccount(10L);
        order.verify(platformSyncService).syncScheduledAccount(11L);
    }

    @Test void fullSyncDelegatesPlatformFetchesAndThenRebuildsExistingCaches() {
        when(accounts.findAllDistinctUserIds()).thenReturn(List.of());
        service.syncAllUsers();
        verify(platformSyncService).syncAllAccounts();
        verify(accounts).findAllDistinctUserIds();
    }
}

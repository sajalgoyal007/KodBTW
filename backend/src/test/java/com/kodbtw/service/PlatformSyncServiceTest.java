package com.kodbtw.service;

import com.kodbtw.adapter.PlatformAdapter;
import com.kodbtw.adapter.PlatformAdapterRegistry;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.dto.SyncFailureCategory;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;
import com.kodbtw.entity.SyncStatus;
import com.kodbtw.entity.User;
import com.kodbtw.exception.PlatformApiException;
import com.kodbtw.exception.PlatformSyncUnavailableException;
import com.kodbtw.exception.ResourceNotFoundException;
import com.kodbtw.repository.PlatformAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.net.SocketTimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlatformSyncServiceTest {
    @Mock PlatformAccountRepository accounts;
    @Mock PlatformAdapter adapter;
    @Mock PlatformSyncPersistenceService persistence;
    @Mock PlatformStatsService statsService;
    private PlatformSyncService service;
    private PlatformAccount account;

    @BeforeEach void setup() {
        when(adapter.getPlatform()).thenReturn(Platform.LEETCODE);
        service = new PlatformSyncService(accounts, new PlatformAdapterRegistry(List.of(adapter)), persistence, statsService);
        User user = new User(); user.setId(7L);
        account = new PlatformAccount(); account.setId(9L); account.setUser(user);
        account.setPlatform(Platform.LEETCODE); account.setUsername("coder");
    }

    @Test void successfulRefreshUsesCooldownToAvoidRepeatedProviderCalls() {
        PlatformStats stats = PlatformStats.builder().platform(Platform.LEETCODE).source("LEETCODE_REAL")
                .totalProblemsSolved(42).build();
        when(persistence.getOwnedAccount(7L, 9L)).thenReturn(account);
        when(persistence.markStarted(7L, 9L)).thenReturn(true);
        when(adapter.fetchStats(account)).thenReturn(stats);
        when(statsService.getStats(7L, 9L)).thenReturn(stats);
        doAnswer(invocation -> {
            account.setLastSuccessAt(LocalDateTime.now(ZoneOffset.UTC));
            account.setSyncStatus(SyncStatus.SUCCEEDED);
            return null;
        }).when(persistence).recordSuccess(9L, stats);

        var first = service.syncOwnedAccount(7L, 9L);
        var second = service.syncOwnedAccount(7L, 9L);

        assertFalse(first.cooldownApplied());
        assertTrue(second.cooldownApplied());
        verify(adapter, times(1)).fetchStats(account);
        verify(persistence, times(1)).recordSuccess(9L, stats);
        verify(persistence, times(1)).markStarted(7L, 9L);
        verify(persistence, never()).recordFailure(anyLong(), any());
    }

    @Test void unavailableLiveProviderGetsExplicitSafeFailureCategory() {
        when(persistence.getOwnedAccount(7L, 9L)).thenReturn(account);
        when(persistence.markStarted(7L, 9L)).thenReturn(true);
        when(adapter.fetchStats(account)).thenThrow(new PlatformSyncUnavailableException("CodeChef live sync unavailable"));
        when(statsService.getStats(7L, 9L)).thenReturn(PlatformStats.builder().source("UNSYNCED").build());
        doAnswer(invocation -> {
            account.setSyncStatus(SyncStatus.FAILED);
            account.setLastSyncErrorCategory(SyncFailureCategory.LIVE_SYNC_UNAVAILABLE.name());
            return null;
        }).when(persistence).recordFailure(9L, SyncFailureCategory.LIVE_SYNC_UNAVAILABLE);

        var response = service.syncOwnedAccount(7L, 9L);

        assertEquals(SyncStatus.FAILED, response.syncStatus().status());
        assertEquals(SyncFailureCategory.LIVE_SYNC_UNAVAILABLE.name(), response.syncStatus().failureCategory());
        verify(persistence).recordFailure(9L, SyncFailureCategory.LIVE_SYNC_UNAVAILABLE);
        verify(persistence, never()).recordSuccess(eq(9L), any());
    }

    @Test void sourcePendingPlatformReturnsUnavailableStateWithoutAdapterOrPersistenceCalls() {
        account.setPlatform(Platform.GEEKSFORGEEKS);
        when(persistence.getOwnedAccount(7L, 9L)).thenReturn(account);
        PlatformStats pending = PlatformStats.builder().platform(Platform.GEEKSFORGEEKS).source("SOURCE_PENDING").build();
        when(statsService.getStats(7L, 9L)).thenReturn(pending);

        var response = service.syncOwnedAccount(7L, 9L);

        assertEquals(SyncStatus.NEVER_SYNCED, response.syncStatus().status());
        assertEquals("SOURCE_PENDING", response.currentStats().getSource());
        verify(adapter, never()).fetchStats(any());
        verify(persistence, never()).markStarted(anyLong(), anyLong());
        verify(persistence, never()).recordSuccess(anyLong(), any());
        verify(persistence, never()).recordFailure(anyLong(), any());
    }

    @Test void rateLimitFailureIsCategorizedWithoutReplacingPersistedStats() {
        PlatformStats lastKnown = PlatformStats.builder().platform(Platform.LEETCODE).source("LEETCODE_REAL")
                .totalProblemsSolved(40).build();
        when(persistence.getOwnedAccount(7L, 9L)).thenReturn(account);
        when(persistence.markStarted(7L, 9L)).thenReturn(true);
        when(adapter.fetchStats(account)).thenThrow(new PlatformApiException("API rate limit exceeded"));
        when(statsService.getStats(7L, 9L)).thenReturn(lastKnown);

        var response = service.syncOwnedAccount(7L, 9L);
        assertEquals(lastKnown, response.currentStats());
        verify(persistence).recordFailure(9L, SyncFailureCategory.RATE_LIMITED);
        verify(persistence, never()).recordSuccess(eq(9L), any());
    }

    @Test void codeChefTimeoutFailureKeepsThirdPartySnapshotAndRecordsSafeFailure() {
        when(adapter.getPlatform()).thenReturn(Platform.CODECHEF);
        service = new PlatformSyncService(accounts, new PlatformAdapterRegistry(List.of(adapter)), persistence, statsService);
        account.setPlatform(Platform.CODECHEF);
        PlatformStats lastKnown = PlatformStats.builder().platform(Platform.CODECHEF)
                .source("CODECHEF_THIRD_PARTY").totalProblemsSolved(656).rating(1247).build();
        PlatformApiException timeout = new PlatformApiException("CodeChef provider request failed",
                new SocketTimeoutException("Read timed out"));
        when(persistence.getOwnedAccount(7L, 9L)).thenReturn(account);
        when(persistence.markStarted(7L, 9L)).thenReturn(true);
        when(adapter.fetchStats(account)).thenThrow(timeout);
        when(statsService.getStats(7L, 9L)).thenReturn(lastKnown);
        doAnswer(invocation -> {
            account.setSyncStatus(SyncStatus.FAILED);
            account.setLastSyncErrorCategory(SyncFailureCategory.TIMEOUT.name());
            return null;
        }).when(persistence).recordFailure(9L, SyncFailureCategory.TIMEOUT);

        var response = service.syncOwnedAccount(7L, 9L);

        assertEquals(SyncStatus.FAILED, response.syncStatus().status());
        assertEquals(SyncFailureCategory.TIMEOUT.name(), response.syncStatus().failureCategory());
        assertEquals(lastKnown, response.currentStats());
        verify(persistence).recordFailure(9L, SyncFailureCategory.TIMEOUT);
        verify(persistence, never()).recordSuccess(eq(9L), any());
    }

    @Test void strictOwnershipStopsBeforeProviderCall() {
        when(persistence.getOwnedAccount(7L, 9L)).thenThrow(new ResourceNotFoundException("Platform account not found"));
        assertThrows(ResourceNotFoundException.class, () -> service.syncOwnedAccount(7L, 9L));
        verify(adapter, never()).fetchStats(any());
    }

    @Test void syncStatusIncludesSafeStoredFailureAndFreshness() {
        account.setSyncStatus(SyncStatus.FAILED);
        account.setLastSyncErrorCategory("TIMEOUT");
        account.setLastSyncErrorMessage("The platform did not respond in time. Try again later.");
        when(persistence.getOwnedAccount(7L, 9L)).thenReturn(account);
        var status = service.getOwnedStatus(7L, 9L);
        assertEquals(SyncStatus.FAILED, status.status());
        assertEquals("TIMEOUT", status.failureCategory());
        assertFalse(status.fresh());
    }

    @Test void scheduledFailureDoesNotPreventLaterAccountSync() {
        PlatformAccount broken = new PlatformAccount(); broken.setId(11L); broken.setUser(account.getUser());
        broken.setPlatform(Platform.LEETCODE); broken.setUsername("broken");
        PlatformAccount good = new PlatformAccount(); good.setId(12L); good.setUser(account.getUser());
        good.setPlatform(Platform.LEETCODE); good.setUsername("good");
        when(accounts.findAllAccountIds()).thenReturn(List.of(11L, 12L));
        when(persistence.getAccount(11L)).thenReturn(broken);
        when(persistence.getAccount(12L)).thenReturn(good);
        when(persistence.markStartedForScheduledSync(anyLong())).thenReturn(true);
        when(adapter.fetchStats(broken)).thenThrow(new PlatformApiException("unavailable"));
        when(adapter.fetchStats(good)).thenReturn(PlatformStats.builder().platform(Platform.LEETCODE).source("LEETCODE_REAL").build());
        when(accounts.findUserIdByAccountId(11L)).thenReturn(Optional.of(7L));
        when(accounts.findUserIdByAccountId(12L)).thenReturn(Optional.of(7L));
        when(statsService.getStats(7L, 11L)).thenReturn(PlatformStats.builder().source("UNSYNCED").build());
        when(statsService.getStats(7L, 12L)).thenReturn(PlatformStats.builder().source("LEETCODE_REAL").build());

        assertDoesNotThrow(() -> service.syncAllAccounts());
        verify(persistence).recordFailure(11L, SyncFailureCategory.UPSTREAM_UNAVAILABLE);
        verify(persistence).recordSuccess(eq(12L), any());
    }
}

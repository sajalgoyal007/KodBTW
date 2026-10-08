package com.kodbtw.service;

import com.kodbtw.adapter.PlatformAdapterRegistry;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.dto.PlatformSyncResponse;
import com.kodbtw.dto.PlatformSyncStatusResponse;
import com.kodbtw.dto.SyncFailureCategory;
import com.kodbtw.entity.PlatformAccount;
import com.kodbtw.entity.SyncStatus;
import com.kodbtw.exception.PlatformApiException;
import com.kodbtw.exception.PlatformSyncUnavailableException;
import com.kodbtw.exception.ResourceNotFoundException;
import com.kodbtw.repository.PlatformAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.LockSupport;

@Service
public class PlatformSyncService {
    private final PlatformAccountRepository accounts;
    private final PlatformAdapterRegistry adapters;
    private final PlatformSyncPersistenceService persistence;
    private final PlatformStatsService statsService;
    @Value("${platform.sync.cooldown:PT15M}")
    private Duration syncCooldown = Duration.ofMinutes(15);
    private final Semaphore syncGate = new Semaphore(1);
    private volatile long nextCodeforcesCallNanos;

    public PlatformSyncService(PlatformAccountRepository accounts,
                               PlatformAdapterRegistry adapters,
                               PlatformSyncPersistenceService persistence,
                               PlatformStatsService statsService) {
        this.accounts = accounts;
        this.adapters = adapters;
        this.persistence = persistence;
        this.statsService = statsService;
    }

    public PlatformSyncResponse syncOwnedAccount(Long userId, Long accountId) {
        persistence.getOwnedAccount(userId, accountId);
        return sync(accountId, userId);
    }

    public PlatformSyncStatusResponse getOwnedStatus(Long userId, Long accountId) {
        return status(persistence.getOwnedAccount(userId, accountId));
    }

    /** Sequential, account-isolated sync used by the nightly scheduler. */
    public void syncAllAccounts() {
        List<Long> ids = accounts.findAllAccountIds();
        for (Long id : ids) {
            try {
                sync(id, null);
            } catch (Exception ignored) {
                // Account-level failures are persisted and must not stop later accounts.
            }
        }
    }

    public PlatformSyncResponse syncScheduledAccount(Long accountId) {
        return sync(accountId, null);
    }

    private PlatformSyncResponse sync(Long accountId, Long ownerId) {
        if (!syncGate.tryAcquire()) {
            PlatformAccount current = ownerId == null ? persistence.getAccount(accountId)
                    : persistence.getOwnedAccount(ownerId, accountId);
            return response(current, ownerId);
        }
        try {
        PlatformAccount account = ownerId == null
                ? persistence.getAccount(accountId)
                : persistence.getOwnedAccount(ownerId, accountId);
        if (!account.getPlatform().hasLiveStatsSource()) {
            return response(account, ownerId);
        }
        if (account.getSyncStatus() != SyncStatus.RUNNING && isWithinCooldown(account)) {
            return response(account, ownerId, true);
        }
        boolean started = ownerId == null
                ? persistence.markStartedForScheduledSync(accountId)
                : persistence.markStarted(ownerId, accountId);
        if (!started) {
            PlatformAccount current = ownerId == null ? persistence.getAccount(accountId)
                    : persistence.getOwnedAccount(ownerId, accountId);
            return response(current, ownerId);
        }

        try {
            if (account.getPlatform().name().equals("CODEFORCES")) awaitCodeforcesPermit();
            PlatformStats fetched = adapters.getAdapter(account.getPlatform()).fetchStats(account);
            persistence.recordSuccess(accountId, fetched);
        } catch (Exception exception) {
            persistence.recordFailure(accountId, classify(exception));
        }

        PlatformAccount current = ownerId == null ? persistence.getAccount(accountId)
                : persistence.getOwnedAccount(ownerId, accountId);
        return response(current, ownerId);
        } finally {
            syncGate.release();
        }
    }

    private void awaitCodeforcesPermit() throws InterruptedException {
        long now = System.nanoTime();
        long waitNanos = nextCodeforcesCallNanos - now;
        if (waitNanos > 0) LockSupport.parkNanos(waitNanos);
        if (Thread.currentThread().isInterrupted()) throw new InterruptedException("Sync interrupted");
        nextCodeforcesCallNanos = System.nanoTime() + Duration.ofSeconds(2).toNanos();
    }

    private PlatformSyncResponse response(PlatformAccount account, Long ownerId) {
        return response(account, ownerId, false);
    }

    private PlatformSyncResponse response(PlatformAccount account, Long ownerId, boolean cooldownApplied) {
        Long userId = ownerId;
        if (userId == null) userId = accounts.findUserIdByAccountId(account.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Platform account not found"));
        return new PlatformSyncResponse(status(account), statsService.getStats(userId, account.getId()), cooldownApplied);
    }

    private boolean isWithinCooldown(PlatformAccount account) {
        LocalDateTime lastSuccess = account.getLastSuccessAt();
        return syncCooldown != null && !syncCooldown.isNegative() && !syncCooldown.isZero()
                && lastSuccess != null
                && Duration.between(lastSuccess, LocalDateTime.now(ZoneOffset.UTC)).compareTo(syncCooldown) < 0;
    }

    private PlatformSyncStatusResponse status(PlatformAccount account) {
        LocalDateTime lastSuccess = account.getLastSuccessAt();
        boolean fresh = lastSuccess != null
                && Duration.between(lastSuccess, LocalDateTime.now(ZoneOffset.UTC)).compareTo(Duration.ofHours(24)) <= 0;
        return new PlatformSyncStatusResponse(
                account.getSyncStatus(), account.getLastAttemptAt(), lastSuccess, account.getLastFailureAt(),
                account.getLastSyncErrorCategory(), account.getLastSyncErrorMessage(), fresh);
    }

    private SyncFailureCategory classify(Throwable error) {
        String safeToInspect = error.getMessage() == null ? "" : error.getMessage().toLowerCase();
        if (error instanceof PlatformSyncUnavailableException) return SyncFailureCategory.LIVE_SYNC_UNAVAILABLE;
        if (error instanceof ResourceNotFoundException) return SyncFailureCategory.ACCOUNT_NOT_FOUND;
        if (safeToInspect.contains("timeout") || safeToInspect.contains("timed out")) return SyncFailureCategory.TIMEOUT;
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof SocketTimeoutException || cause instanceof TimeoutException) return SyncFailureCategory.TIMEOUT;
            String message = cause.getMessage();
            if (message != null) safeToInspect += " " + message.toLowerCase();
        }
        if (safeToInspect.contains("rate limit") || safeToInspect.contains("call limit exceeded")
                || safeToInspect.contains("too many requests") || safeToInspect.contains("429")) {
            return SyncFailureCategory.RATE_LIMITED;
        }
        if (error instanceof PlatformApiException || safeToInspect.contains("connect") || safeToInspect.contains("unavailable")) {
            return SyncFailureCategory.UPSTREAM_UNAVAILABLE;
        }
        return SyncFailureCategory.UNKNOWN;
    }
}

package com.kodbtw.service;

import com.kodbtw.dto.PlatformStats;
import com.kodbtw.dto.SyncFailureCategory;
import com.kodbtw.entity.PlatformAccount;
import com.kodbtw.entity.PlatformStatSnapshot;
import com.kodbtw.entity.SyncStatus;
import com.kodbtw.exception.ResourceNotFoundException;
import com.kodbtw.repository.PlatformAccountRepository;
import com.kodbtw.repository.PlatformStatSnapshotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.Duration;

@Service
public class PlatformSyncPersistenceService {
    private final PlatformAccountRepository accounts;
    private final PlatformStatSnapshotRepository snapshots;

    public PlatformSyncPersistenceService(PlatformAccountRepository accounts,
                                          PlatformStatSnapshotRepository snapshots) {
        this.accounts = accounts;
        this.snapshots = snapshots;
    }

    @Transactional(readOnly = true)
    public PlatformAccount getOwnedAccount(Long userId, Long accountId) {
        return accounts.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Platform account not found"));
    }

    @Transactional(readOnly = true)
    public PlatformAccount getAccount(Long accountId) {
        return accounts.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Platform account not found"));
    }

    @Transactional
    public boolean markStarted(Long userId, Long accountId) {
        PlatformAccount account = getOwnedAccount(userId, accountId);
        if (isRunning(account)) return false;
        account.setSyncStatus(SyncStatus.RUNNING);
        account.setLastAttemptAt(LocalDateTime.now(ZoneOffset.UTC));
        account.setLastSyncErrorCategory(null);
        account.setLastSyncErrorMessage(null);
        return true;
    }

    @Transactional
    public boolean markStartedForScheduledSync(Long accountId) {
        PlatformAccount account = accounts.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Platform account not found"));
        if (isRunning(account)) return false;
        account.setSyncStatus(SyncStatus.RUNNING);
        account.setLastAttemptAt(LocalDateTime.now(ZoneOffset.UTC));
        account.setLastSyncErrorCategory(null);
        account.setLastSyncErrorMessage(null);
        return true;
    }

    @Transactional
    public void recordSuccess(Long accountId, PlatformStats stats) {
        PlatformAccount account = accounts.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Platform account not found"));
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        PlatformStatSnapshot snapshot = snapshots
                .findByUserIdAndPlatformAndSnapshotDate(account.getUser().getId(), account.getPlatform().name(), today)
                .orElseGet(() -> {
                    PlatformStatSnapshot created = new PlatformStatSnapshot();
                    created.setUser(account.getUser());
                    created.setPlatform(account.getPlatform().name());
                    created.setSnapshotDate(today);
                    return created;
                });
        snapshot.setTotalSolved(stats.getTotalProblemsSolved());
        snapshot.setEasySolved(stats.getEasySolved());
        snapshot.setMediumSolved(stats.getMediumSolved());
        snapshot.setHardSolved(stats.getHardSolved());
        snapshot.setRating(stats.getRating());
        snapshot.setMaxRating(stats.getMaxRating());
        snapshot.setRank(stats.getRank());
        snapshot.setContests(stats.getContestsParticipated());
        snapshot.setCurrentStreak(stats.getCurrentStreak());
        snapshot.setLongestStreak(stats.getLongestStreak());
        snapshot.setSource(stats.getSource());
        snapshot.setStatsLastSyncedAt(stats.getLastSyncedAt());
        snapshots.save(snapshot);

        account.setSyncStatus(SyncStatus.SUCCEEDED);
        account.setLastSuccessAt(LocalDateTime.now(ZoneOffset.UTC));
        account.setLastSyncErrorCategory(null);
        account.setLastSyncErrorMessage(null);
    }

    @Transactional
    public void recordFailure(Long accountId, SyncFailureCategory category) {
        accounts.findById(accountId).ifPresent(account -> {
            account.setSyncStatus(SyncStatus.FAILED);
            account.setLastFailureAt(LocalDateTime.now(ZoneOffset.UTC));
            account.setLastSyncErrorCategory(category.name());
            account.setLastSyncErrorMessage(safeMessage(category));
        });
    }

    private String safeMessage(SyncFailureCategory category) {
        return switch (category) {
            case TIMEOUT -> "The platform did not respond in time. Try again later.";
            case RATE_LIMITED -> "The platform temporarily limited requests. Try again later.";
            case ACCOUNT_NOT_FOUND -> "The linked platform account could not be found.";
            case UPSTREAM_UNAVAILABLE -> "The platform is temporarily unavailable. Try again later.";
            case LIVE_SYNC_UNAVAILABLE -> "Live sync is unavailable for this platform. No live statistics were fetched.";
            case UNKNOWN -> "Stats could not be refreshed. Try again later.";
        };
    }

    private boolean isRunning(PlatformAccount account) {
        if (account.getSyncStatus() != SyncStatus.RUNNING) return false;
        LocalDateTime attempt = account.getLastAttemptAt();
        return attempt != null && Duration.between(attempt, LocalDateTime.now(ZoneOffset.UTC)).toMinutes() < 15;
    }
}

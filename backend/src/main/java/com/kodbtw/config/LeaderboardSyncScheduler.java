package com.kodbtw.config;

import com.kodbtw.service.LeaderboardSyncService;
import com.kodbtw.service.PlatformSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task that triggers the nightly leaderboard sync.
 *
 * <p>Runs daily at 02:00 AM server time. This is intentionally off-peak to minimize
 * concurrent load while live LeetCode/Codeforces API calls are made.
 *
 * <p>The sync is resilient — individual platform/user failures are logged but do not
 * prevent other users from being synced (see {@link LeaderboardSyncService}).
 */
@Component
public class LeaderboardSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(LeaderboardSyncScheduler.class);

    private final LeaderboardSyncService leaderboardSyncService;
    private final PlatformSyncService platformSyncService;

    public LeaderboardSyncScheduler(LeaderboardSyncService leaderboardSyncService,
                                    PlatformSyncService platformSyncService) {
        this.leaderboardSyncService = leaderboardSyncService;
        this.platformSyncService = platformSyncService;
    }

    /**
     * Runs daily at 02:00 AM.
     * Cron: second=0, minute=0, hour=2, day=*, month=*, weekday=*
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void runDailySync() {
        log.info("LeaderboardSyncScheduler: starting scheduled nightly sync");
        try {
            platformSyncService.syncAllAccounts();
            leaderboardSyncService.rebuildAllCaches();
            log.info("LeaderboardSyncScheduler: nightly sync completed successfully");
        } catch (Exception e) {
            log.error("LeaderboardSyncScheduler: nightly sync failed with unexpected error — {}", e.getMessage(), e);
        }
    }
}

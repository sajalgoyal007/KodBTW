package com.kodbtw.service;

import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.LeaderboardUserCache;
import com.kodbtw.entity.PlatformAccount;
import com.kodbtw.entity.Profile;
import com.kodbtw.entity.User;
import com.kodbtw.repository.LeaderboardUserCacheRepository;
import com.kodbtw.repository.PlatformAccountRepository;
import com.kodbtw.repository.ProfileRepository;
import com.kodbtw.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Orchestrates the leaderboard sync pipeline:
 *
 * <ol>
 *   <li>Fetch all users with at least one platform account.</li>
 *   <li>For each user, fetch stats from each of their connected platforms.</li>
 *   <li>Upsert into {@code platform_stat_snapshots}.</li>
 *   <li>Aggregate into {@code leaderboard_user_cache}.</li>
 * </ol>
 *
 * <p>Resilient: if a single platform adapter call fails for a user, that platform is skipped and
 * the last known snapshot is preserved. The sync continues with other users and platforms.
 *
 * <p>Scoring model — Weighted Difficulty Score (WDS):
 * <pre>WDS = (easy × 1) + (medium × 3) + (hard × 6)</pre>
 * Only computed when difficulty fields are non-null. MOCK platform data is excluded from
 * real-data scoring (tracked separately via {@code realDataOnly} and {@code hasMockData} flags).
 */
@Service
public class LeaderboardSyncService {

    private static final Logger log = LoggerFactory.getLogger(LeaderboardSyncService.class);

    /** Source tags from adapters that represent real, externally verified data. */
    private static final String MOCK_SOURCE = "MOCK";

    private final PlatformAccountRepository platformAccountRepository;
    private final LeaderboardUserCacheRepository cacheRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final PlatformStatsService platformStatsService;
    private final PlatformSyncService platformSyncService;

    public LeaderboardSyncService(
            PlatformAccountRepository platformAccountRepository,
            LeaderboardUserCacheRepository cacheRepository,
            ProfileRepository profileRepository,
            UserRepository userRepository,
            PlatformStatsService platformStatsService,
            PlatformSyncService platformSyncService) {
        this.platformAccountRepository = platformAccountRepository;
        this.cacheRepository = cacheRepository;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.platformStatsService = platformStatsService;
        this.platformSyncService = platformSyncService;
    }

    /**
     * Main sync entry point. Called by the scheduler and optionally by the manual trigger endpoint.
     * Iterates all users with platform accounts and syncs them one by one.
     */
    public void syncAllUsers() {
        platformSyncService.syncAllAccounts();
        rebuildAllCaches();
    }

    public void rebuildAllCaches() {
        List<Long> userIds = platformAccountRepository.findAllDistinctUserIds();
        for (Long userId : userIds) {
            try {
                refreshUserCache(userId);
            } catch (Exception e) {
                log.error("LeaderboardSyncService: failed to rebuild cache for user {}", userId);
            }
        }
    }

    /**
     * Syncs a single user: fetches stats per platform, persists snapshots, updates cache.
     */
    public void syncUser(Long userId) {
        for (Long id : platformAccountRepository.findAccountIdsByUserId(userId)) {
            try { platformSyncService.syncScheduledAccount(id); }
            catch (Exception ex) { log.warn("Skipping a platform account during scheduled sync"); }
        }
        refreshUserCache(userId);
    }

    @Transactional
    public void refreshUserCache(Long userId) {
        List<PlatformAccount> accounts = platformAccountRepository.findAllByUserId(userId);
        if (accounts.isEmpty()) return;

        // --- Accumulator fields for cache row ---
        int totalSolvedReal = 0;
        double weightedScoreReal = 0.0;
        Integer bestRating = null;
        String bestRatingPlatform = null;
        int totalContestsReal = 0;
        boolean anyMock = false;
        boolean allReal = true;

        for (PlatformAccount account : accounts) {
            PlatformStats stats = platformStatsService.getPersistedStats(account);

            boolean isReal = isRealPlatform(stats);
            // Track mock/real flags.
            if ("MOCK".equalsIgnoreCase(stats.getSource())) {
                anyMock = true;
            }
            if (!isReal) {
                allReal = false;
            }

            // Accumulate real-only metrics for the cache (WDS + solved + contests).
            if (isReal) {
                if (stats.getTotalProblemsSolved() != null && stats.getTotalProblemsSolved() > 0) {
                    totalSolvedReal += stats.getTotalProblemsSolved();
                }
                // WDS: easy×1 + medium×3 + hard×6 — only when fields are available.
                weightedScoreReal += computeWds(stats.getEasySolved(), stats.getMediumSolved(), stats.getHardSolved());
                if (stats.getContestsParticipated() != null && stats.getContestsParticipated() > 0) {
                    totalContestsReal += stats.getContestsParticipated();
                }
            }

            // Best rating: track across all platforms (real-only for fair comparison is ideal,
            // but Codeforces real rating should always win over mock, so we filter here).
            if (isReal && stats.getRating() != null) {
                if (bestRating == null || stats.getRating() > bestRating) {
                    bestRating = stats.getRating();
                    bestRatingPlatform = stats.getPlatform() != null ? stats.getPlatform().name() : null;
                }
            }
        }

        // --- Build/update the cache row ---
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            log.warn("LeaderboardSyncService: user {} not found in users table, skipping cache update", userId);
            return;
        }
        User user = userOpt.get();
        Optional<Profile> profileOpt = profileRepository.findByUserId(userId);

        LeaderboardUserCache cache = cacheRepository.findById(userId)
                .orElseGet(() -> {
                    LeaderboardUserCache c = new LeaderboardUserCache();
                    c.setUserId(userId);
                    return c;
                });
        cache.setUser(user);
        cache.setUserId(userId);
        cache.setDisplayName(profileOpt.map(Profile::getDisplayName).orElse(user.getName()));
        cache.setCollege(profileOpt.map(Profile::getCollege).orElse(null));
        cache.setTotalSolved(totalSolvedReal);
        cache.setWeightedScore(weightedScoreReal);
        cache.setBestRating(bestRating);
        cache.setBestRatingPlatform(bestRatingPlatform);
        cache.setTotalContests(totalContestsReal);
        cache.setRealDataOnly(allReal && !anyMock);
        cache.setHasMockData(anyMock);
        cache.setLastSyncedAt(LocalDateTime.now());
        cacheRepository.save(cache);
    }

    /**
     * Determines whether a platform's stats originate from an authentic, verified live adapter.
     * Only adapters with source tags ending in "_REAL" and not "MOCK" qualify.
     */
    public static boolean isRealPlatform(PlatformStats stats) {
        if (stats == null || stats.getSource() == null || stats.getSource().isBlank()) {
            return false;
        }
        return !MOCK_SOURCE.equalsIgnoreCase(stats.getSource().trim())
                && stats.getSource().trim().toUpperCase().endsWith("_REAL");
    }

    /**
     * Weighted Difficulty Score: easy×1 + medium×3 + hard×6.
     * Returns 0.0 if all difficulty fields are null (avoids fabricating scores).
     */
    public static double computeWds(Integer easy, Integer medium, Integer hard) {
        double score = 0.0;
        if (easy != null && easy > 0)   score += easy   * 1.0;
        if (medium != null && medium > 0) score += medium  * 3.0;
        if (hard != null && hard > 0)   score += hard    * 6.0;
        return score;
    }
}

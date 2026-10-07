package com.kodbtw.service;

import com.kodbtw.dto.DashboardOverview;
import com.kodbtw.dto.DashboardStatsResponse;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.PlatformAccount;
import com.kodbtw.entity.PlatformStatSnapshot;
import com.kodbtw.exception.ResourceNotFoundException;
import com.kodbtw.repository.PlatformAccountRepository;
import com.kodbtw.repository.PlatformStatSnapshotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class PlatformStatsService {

    private final PlatformAccountRepository platformAccountRepository;
    private final PlatformStatSnapshotRepository snapshotRepository;

    public PlatformStatsService(PlatformAccountRepository platformAccountRepository,
                                PlatformStatSnapshotRepository snapshotRepository) {
        this.platformAccountRepository = platformAccountRepository;
        this.snapshotRepository = snapshotRepository;
    }

    @Transactional(readOnly = true)
    public PlatformStats getStats(Long userId, Long accountId) {
        PlatformAccount account = platformAccountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Platform account not found"));

        return getPersistedStats(account);
    }

    @Transactional(readOnly = true)
    public PlatformStats getPersistedStats(PlatformAccount account) {
        PlatformStatSnapshot snapshot = snapshotRepository
                .findFirstByUserIdAndPlatformOrderBySnapshotDateDesc(account.getUser().getId(), account.getPlatform().name())
                .orElse(null);
        if (snapshot == null) {
            return PlatformStats.builder()
                    .platform(account.getPlatform())
                    .username(account.getUsername())
                    .profileUrl(profileUrl(account))
                    .lastSyncedAt(null)
                    .source("UNSYNCED")
                    .build();
        }
        return PlatformStats.builder()
                .platform(account.getPlatform())
                .username(account.getUsername())
                .profileUrl(profileUrl(account))
                .totalProblemsSolved(snapshot.getTotalSolved())
                .easySolved(snapshot.getEasySolved())
                .mediumSolved(snapshot.getMediumSolved())
                .hardSolved(snapshot.getHardSolved())
                .rating(snapshot.getRating())
                .rank(snapshot.getRank())
                .contestsParticipated(snapshot.getContests())
                .currentStreak(snapshot.getCurrentStreak())
                .longestStreak(snapshot.getLongestStreak())
                .lastSyncedAt(snapshot.getStatsLastSyncedAt())
                .source(snapshot.getSource())
                .build();
    }

    private String profileUrl(PlatformAccount account) {
        if (account.getProfileUrl() != null && !account.getProfileUrl().isBlank()) return account.getProfileUrl();
        return switch (account.getPlatform()) {
            case LEETCODE -> "https://leetcode.com/u/" + account.getUsername() + "/";
            case CODEFORCES -> "https://codeforces.com/profile/" + account.getUsername();
            case CODECHEF -> "https://www.codechef.com/users/" + account.getUsername();
            case GEEKSFORGEEKS -> "https://auth.geeksforgeeks.org/user/" + account.getUsername();
            case HACKERRANK -> "https://www.hackerrank.com/profile/" + account.getUsername();
        };
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats(Long userId) {
        List<PlatformAccount> accounts = platformAccountRepository.findAllByUserId(userId);

        if (accounts.isEmpty()) {
            DashboardOverview emptyOverview = new DashboardOverview(0, 0, 0, 0, 0, null, null, 0);
            return new DashboardStatsResponse(emptyOverview, Collections.emptyList());
        }

        int totalSolved = 0;
        int easySolved = 0;
        int mediumSolved = 0;
        int hardSolved = 0;
        int contests = 0;
        Integer maxCurrentStreak = null;
        Integer maxLongestStreak = null;

        List<PlatformStats> platformStatsList = new ArrayList<>(accounts.size());

        for (PlatformAccount account : accounts) {
            PlatformStats stats = getPersistedStats(account);
            platformStatsList.add(stats);

            if (stats.getTotalProblemsSolved() != null) {
                totalSolved += stats.getTotalProblemsSolved();
            }
            if (stats.getEasySolved() != null) {
                easySolved += stats.getEasySolved();
            }
            if (stats.getMediumSolved() != null) {
                mediumSolved += stats.getMediumSolved();
            }
            if (stats.getHardSolved() != null) {
                hardSolved += stats.getHardSolved();
            }
            if (stats.getContestsParticipated() != null) {
                contests += stats.getContestsParticipated();
            }

            if (stats.getCurrentStreak() != null) {
                maxCurrentStreak = (maxCurrentStreak == null)
                        ? stats.getCurrentStreak()
                        : Math.max(maxCurrentStreak, stats.getCurrentStreak());
            }

            if (stats.getLongestStreak() != null) {
                maxLongestStreak = (maxLongestStreak == null)
                        ? stats.getLongestStreak()
                        : Math.max(maxLongestStreak, stats.getLongestStreak());
            }
        }

        DashboardOverview overview = new DashboardOverview(
                totalSolved,
                easySolved,
                mediumSolved,
                hardSolved,
                contests,
                maxCurrentStreak,
                maxLongestStreak,
                accounts.size()
        );

        return new DashboardStatsResponse(overview, platformStatsList);
    }
}


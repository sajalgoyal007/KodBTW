package com.kodbtw.service;

import com.kodbtw.adapter.PlatformAdapter;
import com.kodbtw.adapter.PlatformAdapterRegistry;
import com.kodbtw.dto.DashboardOverview;
import com.kodbtw.dto.DashboardStatsResponse;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.PlatformAccount;
import com.kodbtw.exception.ResourceNotFoundException;
import com.kodbtw.repository.PlatformAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class PlatformStatsService {

    private final PlatformAccountRepository platformAccountRepository;
    private final PlatformAdapterRegistry platformAdapterRegistry;

    public PlatformStatsService(PlatformAccountRepository platformAccountRepository,
                                PlatformAdapterRegistry platformAdapterRegistry) {
        this.platformAccountRepository = platformAccountRepository;
        this.platformAdapterRegistry = platformAdapterRegistry;
    }

    @Transactional(readOnly = true)
    public PlatformStats getStats(Long userId, Long accountId) {
        PlatformAccount account = platformAccountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Platform account not found"));

        return fetchStats(account);
    }

    @Transactional(readOnly = true)
    public PlatformStats fetchStats(PlatformAccount account) {
        PlatformAdapter adapter = platformAdapterRegistry.getAdapter(account.getPlatform());
        return adapter.fetchStats(account);
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
            PlatformStats stats = fetchStats(account);
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


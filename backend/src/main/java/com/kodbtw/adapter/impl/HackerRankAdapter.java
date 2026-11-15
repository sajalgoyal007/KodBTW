package com.kodbtw.adapter.impl;

import com.kodbtw.adapter.PlatformAdapter;
import com.kodbtw.adapter.hackerrank.HackerRankClient;
import com.kodbtw.adapter.hackerrank.dto.HackerRankPublicStats;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Adapter for HackerRank.
 *
 * <p>Uses HackerRank's public badge and ELO-score REST endpoints. Solved counts are summed across
 * badge domains; difficulty breakdowns and contest participation are not published publicly and
 * therefore stay null.
 */
@Component
public class HackerRankAdapter implements PlatformAdapter {

    private final HackerRankClient client;

    public HackerRankAdapter(HackerRankClient client) {
        this.client = client;
    }

    @Override
    public Platform getPlatform() {
        return Platform.HACKERRANK;
    }

    @Override
    public PlatformStats fetchStats(PlatformAccount account) {
        HackerRankPublicStats stats = client.fetchStats(account.getUsername());
        return PlatformStats.builder()
                .platform(Platform.HACKERRANK)
                .username(account.getUsername())
                .profileUrl("https://www.hackerrank.com/profile/" + account.getUsername())
                .totalProblemsSolved(stats.totalSolved())
                .rating(stats.rating())
                .maxRating(stats.maxRating())
                .lastSyncedAt(LocalDateTime.now(ZoneOffset.UTC))
                .source("HACKERRANK_REAL")
                .build();
    }
}

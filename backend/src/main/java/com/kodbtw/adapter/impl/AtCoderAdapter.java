package com.kodbtw.adapter.impl;

import com.kodbtw.adapter.PlatformAdapter;
import com.kodbtw.adapter.atcoder.AtCoderClient;
import com.kodbtw.adapter.atcoder.dto.AtCoderStats;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Adapter for AtCoder.
 *
 * <p>Combines the public AtCoder rating history with Kenkoooo's public accepted-count and
 * submissions APIs. All values are public and unauthenticated.
 */
@Component
public class AtCoderAdapter implements PlatformAdapter {

    private final AtCoderClient client;

    public AtCoderAdapter(AtCoderClient client) {
        this.client = client;
    }

    @Override
    public Platform getPlatform() {
        return Platform.ATCODER;
    }

    @Override
    public PlatformStats fetchStats(PlatformAccount account) {
        AtCoderStats stats = client.fetchStats(account.getUsername());
        return PlatformStats.builder()
                .platform(Platform.ATCODER)
                .username(account.getUsername())
                .profileUrl("https://atcoder.jp/users/" + account.getUsername())
                .totalProblemsSolved(stats.totalSolved())
                .rating(stats.rating())
                .maxRating(stats.maxRating())
                .rank(stats.rank())
                .contestsParticipated(stats.contestsParticipated())
                .currentStreak(stats.currentStreak())
                .longestStreak(stats.longestStreak())
                .lastSyncedAt(LocalDateTime.now(ZoneOffset.UTC))
                .source("ATCODER_REAL")
                .build();
    }
}

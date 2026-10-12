package com.kodbtw.adapter.impl;

import com.kodbtw.adapter.PlatformAdapter;
import com.kodbtw.adapter.geeksforgeeks.GeeksForGeeksClient;
import com.kodbtw.adapter.geeksforgeeks.dto.GeeksForGeeksStatsResponse;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class GeeksForGeeksAdapter implements PlatformAdapter {

    private final GeeksForGeeksClient client;

    public GeeksForGeeksAdapter(GeeksForGeeksClient client) {
        this.client = client;
    }

    @Override
    public Platform getPlatform() {
        return Platform.GEEKSFORGEEKS;
    }

    @Override
    public PlatformStats fetchStats(PlatformAccount account) {
        GeeksForGeeksStatsResponse response = client.fetchStats(account.getUsername());
        GeeksForGeeksStatsResponse.Stats stats = response.data();
        GeeksForGeeksStatsResponse.DifficultyCounts difficulty = stats.byDifficulty();

        return PlatformStats.builder()
                .platform(Platform.GEEKSFORGEEKS)
                .username(account.getUsername())
                .profileUrl("https://www.geeksforgeeks.org/profile/" + account.getUsername())
                .totalProblemsSolved(stats.totalSolved())
                .easySolved(difficulty == null ? null : difficulty.easy())
                .mediumSolved(difficulty == null ? null : difficulty.medium())
                .hardSolved(difficulty == null ? null : difficulty.hard())
                .lastSyncedAt(LocalDateTime.now(ZoneOffset.UTC))
                .source("GEEKSFORGEEKS_THIRD_PARTY")
                .build();
    }
}

package com.kodbtw.adapter.impl;

import com.kodbtw.adapter.PlatformAdapter;
import com.kodbtw.adapter.hackerrank.HackerRankClient;
import com.kodbtw.adapter.hackerrank.dto.HackerRankStatsResponse;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

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
        HackerRankStatsResponse response = client.fetchStats(account.getUsername());
        return PlatformStats.builder()
                .platform(Platform.HACKERRANK)
                .username(account.getUsername())
                .profileUrl("https://www.hackerrank.com/profile/" + account.getUsername())
                .totalProblemsSolved(response.data().totalSolved())
                .lastSyncedAt(LocalDateTime.now(ZoneOffset.UTC))
                .source("HACKERRANK_THIRD_PARTY")
                .build();
    }
}

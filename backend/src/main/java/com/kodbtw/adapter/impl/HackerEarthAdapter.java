package com.kodbtw.adapter.impl;

import com.kodbtw.adapter.PlatformAdapter;
import com.kodbtw.adapter.hackerearth.HackerEarthClient;
import com.kodbtw.adapter.hackerearth.dto.HackerEarthStats;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class HackerEarthAdapter implements PlatformAdapter {

    private final HackerEarthClient client;

    public HackerEarthAdapter(HackerEarthClient client) {
        this.client = client;
    }

    @Override
    public Platform getPlatform() {
        return Platform.HACKEREARTH;
    }

    @Override
    public PlatformStats fetchStats(PlatformAccount account) {
        HackerEarthStats stats = client.fetchStats(account.getUsername());
        return PlatformStats.builder()
                .platform(Platform.HACKEREARTH)
                .username(account.getUsername())
                .profileUrl("https://www.hackerearth.com/@" + account.getUsername())
                .totalProblemsSolved(stats.totalSolved())
                .rating(stats.points())
                .contestsParticipated(stats.contestsParticipated())
                .lastSyncedAt(LocalDateTime.now(ZoneOffset.UTC))
                .source("HACKEREARTH_REAL")
                .build();
    }
}

package com.kodbtw.adapter.impl;

import com.kodbtw.adapter.PlatformAdapter;
import com.kodbtw.adapter.code360.Code360Client;
import com.kodbtw.adapter.code360.dto.Code360Stats;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class Code360Adapter implements PlatformAdapter {

    private final Code360Client client;

    public Code360Adapter(Code360Client client) {
        this.client = client;
    }

    @Override
    public Platform getPlatform() {
        return Platform.CODE360;
    }

    @Override
    public PlatformStats fetchStats(PlatformAccount account) {
        Code360Stats stats = client.fetchStats(account.getUsername());
        return PlatformStats.builder()
                .platform(Platform.CODE360)
                .username(account.getUsername())
                .profileUrl("https://www.naukri.com/code360/profile/" + account.getUsername())
                .totalProblemsSolved(stats.totalSolved())
                .rating(stats.rating())
                .contestsParticipated(stats.contestsParticipated())
                .currentStreak(stats.currentStreak())
                .longestStreak(stats.longestStreak())
                .lastSyncedAt(LocalDateTime.now(ZoneOffset.UTC))
                .source("CODE360_THIRD_PARTY")
                .build();
    }
}

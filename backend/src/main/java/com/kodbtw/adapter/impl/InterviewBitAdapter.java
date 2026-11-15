package com.kodbtw.adapter.impl;

import com.kodbtw.adapter.PlatformAdapter;
import com.kodbtw.adapter.interviewbit.InterviewBitClient;
import com.kodbtw.adapter.interviewbit.dto.InterviewBitStats;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class InterviewBitAdapter implements PlatformAdapter {

    private final InterviewBitClient client;

    public InterviewBitAdapter(InterviewBitClient client) {
        this.client = client;
    }

    @Override
    public Platform getPlatform() {
        return Platform.INTERVIEWBIT;
    }

    @Override
    public PlatformStats fetchStats(PlatformAccount account) {
        InterviewBitStats stats = client.fetchStats(account.getUsername());
        return PlatformStats.builder()
                .platform(Platform.INTERVIEWBIT)
                .username(account.getUsername())
                .profileUrl("https://www.interviewbit.com/profile/" + account.getUsername() + "/")
                .totalProblemsSolved(stats.totalSolved())
                .rating(stats.score())
                .rank(stats.rank())
                .currentStreak(stats.currentStreak())
                .lastSyncedAt(LocalDateTime.now(ZoneOffset.UTC))
                .source("INTERVIEWBIT_REAL")
                .build();
    }
}

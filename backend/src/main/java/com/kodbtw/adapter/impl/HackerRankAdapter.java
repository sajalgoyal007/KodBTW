package com.kodbtw.adapter.impl;

import com.kodbtw.adapter.PlatformAdapter;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;
import org.springframework.stereotype.Component;

@Component
public class HackerRankAdapter implements PlatformAdapter {

    @Override
    public Platform getPlatform() {
        return Platform.HACKERRANK;
    }

    @Override
    public PlatformStats fetchStats(PlatformAccount account) {
        String profileUrl = (account.getProfileUrl() != null && !account.getProfileUrl().isBlank())
                ? account.getProfileUrl()
                : "https://www.hackerrank.com/profile/" + account.getUsername();

        return PlatformStats.builder()
                .platform(Platform.HACKERRANK)
                .username(account.getUsername())
                .profileUrl(profileUrl)
                .totalProblemsSolved(150)
                .easySolved(80)
                .mediumSolved(50)
                .hardSolved(20)
                .rating(null)
                .rank(null)
                .contestsParticipated(5)
                .currentStreak(null)
                .longestStreak(null)
                .lastSyncedAt(null)
                .source("MOCK")
                .build();
    }
}

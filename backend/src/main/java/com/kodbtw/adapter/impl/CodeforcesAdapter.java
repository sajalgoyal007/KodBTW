package com.kodbtw.adapter.impl;

import com.kodbtw.adapter.PlatformAdapter;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;
import org.springframework.stereotype.Component;

@Component
public class CodeforcesAdapter implements PlatformAdapter {

    @Override
    public Platform getPlatform() {
        return Platform.CODEFORCES;
    }

    @Override
    public PlatformStats fetchStats(PlatformAccount account) {
        String profileUrl = (account.getProfileUrl() != null && !account.getProfileUrl().isBlank())
                ? account.getProfileUrl()
                : "https://codeforces.com/profile/" + account.getUsername();

        return PlatformStats.builder()
                .platform(Platform.CODEFORCES)
                .username(account.getUsername())
                .profileUrl(profileUrl)
                .totalProblemsSolved(420)
                .easySolved(null)
                .mediumSolved(null)
                .hardSolved(null)
                .rating(1450)
                .rank(25000)
                .contestsParticipated(34)
                .currentStreak(4)
                .longestStreak(15)
                .lastSyncedAt(null)
                .source("MOCK")
                .build();
    }
}

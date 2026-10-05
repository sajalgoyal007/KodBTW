package com.kodbtw.adapter.impl;

import com.kodbtw.adapter.PlatformAdapter;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;
import org.springframework.stereotype.Component;

@Component
public class CodeChefAdapter implements PlatformAdapter {

    @Override
    public Platform getPlatform() {
        return Platform.CODECHEF;
    }

    @Override
    public PlatformStats fetchStats(PlatformAccount account) {
        String profileUrl = (account.getProfileUrl() != null && !account.getProfileUrl().isBlank())
                ? account.getProfileUrl()
                : "https://www.codechef.com/users/" + account.getUsername();

        return PlatformStats.builder()
                .platform(Platform.CODECHEF)
                .username(account.getUsername())
                .profileUrl(profileUrl)
                .totalProblemsSolved(210)
                .easySolved(120)
                .mediumSolved(70)
                .hardSolved(20)
                .rating(1750)
                .rank(4500)
                .contestsParticipated(25)
                .currentStreak(null)
                .longestStreak(null)
                .lastSyncedAt(null)
                .source("MOCK")
                .build();
    }
}

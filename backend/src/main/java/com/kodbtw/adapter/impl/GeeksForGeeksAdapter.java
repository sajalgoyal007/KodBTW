package com.kodbtw.adapter.impl;

import com.kodbtw.adapter.PlatformAdapter;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;
import org.springframework.stereotype.Component;

@Component
public class GeeksForGeeksAdapter implements PlatformAdapter {

    @Override
    public Platform getPlatform() {
        return Platform.GEEKSFORGEEKS;
    }

    @Override
    public PlatformStats fetchStats(PlatformAccount account) {
        String profileUrl = (account.getProfileUrl() != null && !account.getProfileUrl().isBlank())
                ? account.getProfileUrl()
                : "https://auth.geeksforgeeks.org/user/" + account.getUsername();

        return PlatformStats.builder()
                .platform(Platform.GEEKSFORGEEKS)
                .username(account.getUsername())
                .profileUrl(profileUrl)
                .totalProblemsSolved(280)
                .easySolved(140)
                .mediumSolved(100)
                .hardSolved(40)
                .rating(null)
                .rank(12500)
                .contestsParticipated(null)
                .currentStreak(8)
                .longestStreak(25)
                .lastSyncedAt(null)
                .source("MOCK")
                .build();
    }
}

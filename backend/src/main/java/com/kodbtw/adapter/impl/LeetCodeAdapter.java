package com.kodbtw.adapter.impl;

import com.kodbtw.adapter.PlatformAdapter;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;
import org.springframework.stereotype.Component;

@Component
public class LeetCodeAdapter implements PlatformAdapter {

    @Override
    public Platform getPlatform() {
        return Platform.LEETCODE;
    }

    @Override
    public PlatformStats fetchStats(PlatformAccount account) {
        String profileUrl = (account.getProfileUrl() != null && !account.getProfileUrl().isBlank())
                ? account.getProfileUrl()
                : "https://leetcode.com/u/" + account.getUsername() + "/";

        return PlatformStats.builder()
                .platform(Platform.LEETCODE)
                .username(account.getUsername())
                .profileUrl(profileUrl)
                .totalProblemsSolved(350)
                .easySolved(180)
                .mediumSolved(130)
                .hardSolved(40)
                .rating(null)
                .rank(null)
                .contestsParticipated(12)
                .currentStreak(7)
                .longestStreak(21)
                .lastSyncedAt(null)
                .source("MOCK")
                .build();
    }
}

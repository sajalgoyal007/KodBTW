package com.kodbtw.adapter.impl;

import com.kodbtw.adapter.PlatformAdapter;
import com.kodbtw.adapter.codechef.CodeChefClient;
import com.kodbtw.adapter.codechef.dto.CodeChefApiResponse;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class CodeChefAdapter implements PlatformAdapter {

    private final CodeChefClient client;

    public CodeChefAdapter(CodeChefClient client) {
        this.client = client;
    }

    @Override
    public Platform getPlatform() {
        return Platform.CODECHEF;
    }

    @Override
    public PlatformStats fetchStats(PlatformAccount account) {
        CodeChefApiResponse response = client.fetchProfile(account.getUsername());
        CodeChefApiResponse.Profile profile = response.profile();

        return PlatformStats.builder()
                .platform(Platform.CODECHEF)
                .username(account.getUsername())
                .profileUrl("https://www.codechef.com/users/" + account.getUsername())
                .totalProblemsSolved(profile.totalSolved())
                .rating(profile.currentRating())
                .maxRating(profile.highestRating())
                .rank(profile.globalRank())
                .lastSyncedAt(LocalDateTime.now(ZoneOffset.UTC))
                .source("CODECHEF_THIRD_PARTY")
                .build();
    }
}

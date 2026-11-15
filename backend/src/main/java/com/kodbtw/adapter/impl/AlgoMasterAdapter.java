package com.kodbtw.adapter.impl;

import com.kodbtw.adapter.PlatformAdapter;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;
import org.springframework.stereotype.Component;

/**
 * Link-only adapter for AlgoMaster.
 *
 * <p>AlgoMaster is a structured learning roadmap rather than a statistics platform and does not
 * publish a public user-stats API. KodBTW therefore stores and displays the user's public profile
 * URL without fetching or inventing numbers. Normal synchronization is skipped before this
 * adapter is consulted ({@link Platform#hasLiveStatsSource()} is false); the adapter exists so
 * every platform is represented in the registry and profile views.
 */
@Component
public class AlgoMasterAdapter implements PlatformAdapter {

    private static final String DEFAULT_PROFILE_URL = "https://algomaster.io/";

    @Override
    public Platform getPlatform() {
        return Platform.ALGOMASTER;
    }

    @Override
    public PlatformStats fetchStats(PlatformAccount account) {
        String profileUrl = account.getProfileUrl();
        if (profileUrl == null || profileUrl.isBlank()) {
            profileUrl = DEFAULT_PROFILE_URL;
        }

        return PlatformStats.builder()
                .platform(Platform.ALGOMASTER)
                .username(account.getUsername())
                .profileUrl(profileUrl)
                .lastSyncedAt(null)
                .source("ALGOMASTER_LINK_ONLY")
                .build();
    }
}

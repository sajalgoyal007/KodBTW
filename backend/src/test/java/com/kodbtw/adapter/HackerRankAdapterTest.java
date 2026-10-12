package com.kodbtw.adapter;

import com.kodbtw.adapter.hackerrank.HackerRankClient;
import com.kodbtw.adapter.hackerrank.dto.HackerRankStatsResponse;
import com.kodbtw.adapter.impl.HackerRankAdapter;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class HackerRankAdapterTest {

    @Test
    void mapsOnlyDocumentedSolvedCountAndKeepsOtherMetricsNull() {
        var response = new HackerRankStatsResponse("success", "retrieved", "hackerrank", "sample_user", false,
                new HackerRankStatsResponse.Stats(8));
        HackerRankClient client = username -> response;
        HackerRankAdapter adapter = new HackerRankAdapter(client);
        PlatformAccount account = account("sample_user");

        PlatformStats stats = adapter.fetchStats(account);

        assertEquals(Platform.HACKERRANK, stats.getPlatform());
        assertEquals("sample_user", stats.getUsername());
        assertEquals("https://www.hackerrank.com/profile/sample_user", stats.getProfileUrl());
        assertEquals(8, stats.getTotalProblemsSolved());
        assertNull(stats.getEasySolved());
        assertNull(stats.getMediumSolved());
        assertNull(stats.getHardSolved());
        assertNull(stats.getRating());
        assertNull(stats.getMaxRating());
        assertNull(stats.getRank());
        assertNull(stats.getContestsParticipated());
        assertNull(stats.getCurrentStreak());
        assertNull(stats.getLongestStreak());
        assertEquals("HACKERRANK_THIRD_PARTY", stats.getSource());
        assertNotNull(stats.getLastSyncedAt());
        assertFalse(Platform.HACKERRANK.hasLiveStatsSource());
    }

    private PlatformAccount account(String username) {
        PlatformAccount account = new PlatformAccount();
        account.setPlatform(Platform.HACKERRANK);
        account.setUsername(username);
        return account;
    }
}

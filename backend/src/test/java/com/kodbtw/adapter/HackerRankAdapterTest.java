package com.kodbtw.adapter;

import com.kodbtw.adapter.hackerrank.HackerRankClient;
import com.kodbtw.adapter.hackerrank.dto.HackerRankPublicStats;
import com.kodbtw.adapter.impl.HackerRankAdapter;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HackerRankAdapterTest {

    @Test
    void mapsPublicSolvedAndEloMetricsAndKeepsUnpublishedMetricsNull() {
        HackerRankClient client = username -> new HackerRankPublicStats(45, 2, 8, 1600, 1600);
        HackerRankAdapter adapter = new HackerRankAdapter(client);
        PlatformAccount account = account("sample_user");

        PlatformStats stats = adapter.fetchStats(account);

        assertEquals(Platform.HACKERRANK, stats.getPlatform());
        assertEquals("sample_user", stats.getUsername());
        assertEquals("https://www.hackerrank.com/profile/sample_user", stats.getProfileUrl());
        assertEquals(45, stats.getTotalProblemsSolved());
        assertEquals(1600, stats.getRating());
        assertEquals(1600, stats.getMaxRating());
        assertNull(stats.getEasySolved());
        assertNull(stats.getMediumSolved());
        assertNull(stats.getHardSolved());
        assertNull(stats.getRank());
        assertNull(stats.getContestsParticipated());
        assertNull(stats.getCurrentStreak());
        assertNull(stats.getLongestStreak());
        assertEquals("HACKERRANK_REAL", stats.getSource());
        assertNotNull(stats.getLastSyncedAt());
        assertTrue(Platform.HACKERRANK.hasLiveStatsSource());
    }

    @Test
    void missingPublicMetricsStayNullInsteadOfBecomingZero() {
        HackerRankClient client = username -> new HackerRankPublicStats(null, null, null, null, null);
        HackerRankAdapter adapter = new HackerRankAdapter(client);

        PlatformStats stats = adapter.fetchStats(account("sample_user"));

        assertNull(stats.getTotalProblemsSolved());
        assertNull(stats.getRating());
        assertNull(stats.getMaxRating());
        assertEquals("HACKERRANK_REAL", stats.getSource());
    }

    private PlatformAccount account(String username) {
        PlatformAccount account = new PlatformAccount();
        account.setPlatform(Platform.HACKERRANK);
        account.setUsername(username);
        return account;
    }
}

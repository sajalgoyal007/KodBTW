package com.kodbtw.adapter;

import com.kodbtw.adapter.hackerearth.HackerEarthClient;
import com.kodbtw.adapter.hackerearth.dto.HackerEarthStats;
import com.kodbtw.adapter.impl.HackerEarthAdapter;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HackerEarthAdapterTest {

    @Test
    void mapsPointsSolvedAndContestCountWithoutInventingBadgeMetrics() {
        HackerEarthClient client = username -> new HackerEarthStats(320, 47, 6, 3);
        HackerEarthAdapter adapter = new HackerEarthAdapter(client);
        PlatformAccount account = account("sample_user");

        PlatformStats stats = adapter.fetchStats(account);

        assertEquals(Platform.HACKEREARTH, stats.getPlatform());
        assertEquals("sample_user", stats.getUsername());
        assertEquals("https://www.hackerearth.com/@sample_user", stats.getProfileUrl());
        assertEquals(320, stats.getRating());
        assertEquals(47, stats.getTotalProblemsSolved());
        assertEquals(6, stats.getContestsParticipated());
        assertNull(stats.getEasySolved());
        assertNull(stats.getMediumSolved());
        assertNull(stats.getHardSolved());
        assertNull(stats.getMaxRating());
        assertNull(stats.getRank());
        assertNull(stats.getCurrentStreak());
        assertNull(stats.getLongestStreak());
        assertEquals("HACKEREARTH_REAL", stats.getSource());
        assertNotNull(stats.getLastSyncedAt());
        assertTrue(Platform.HACKEREARTH.hasLiveStatsSource());
    }

    @Test
    void missingFieldsStayNull() {
        HackerEarthClient client = username -> new HackerEarthStats(null, null, null, 2);
        HackerEarthAdapter adapter = new HackerEarthAdapter(client);

        PlatformStats stats = adapter.fetchStats(account("sample_user"));

        assertNull(stats.getRating());
        assertNull(stats.getTotalProblemsSolved());
        assertNull(stats.getContestsParticipated());
    }

    private PlatformAccount account(String username) {
        PlatformAccount account = new PlatformAccount();
        account.setPlatform(Platform.HACKEREARTH);
        account.setUsername(username);
        return account;
    }
}

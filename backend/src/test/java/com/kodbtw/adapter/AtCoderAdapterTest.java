package com.kodbtw.adapter;

import com.kodbtw.adapter.atcoder.AtCoderClient;
import com.kodbtw.adapter.atcoder.dto.AtCoderStats;
import com.kodbtw.adapter.impl.AtCoderAdapter;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AtCoderAdapterTest {

    @Test
    void mapsAllPublicAtCoderMetrics() {
        AtCoderClient client = username -> new AtCoderStats(1650, 1800, 4321, 512, 34, 6, 21);
        AtCoderAdapter adapter = new AtCoderAdapter(client);
        PlatformAccount account = account("sample_user");

        PlatformStats stats = adapter.fetchStats(account);

        assertEquals(Platform.ATCODER, stats.getPlatform());
        assertEquals("sample_user", stats.getUsername());
        assertEquals("https://atcoder.jp/users/sample_user", stats.getProfileUrl());
        assertEquals(1650, stats.getRating());
        assertEquals(1800, stats.getMaxRating());
        assertEquals(4321, stats.getRank());
        assertEquals(512, stats.getTotalProblemsSolved());
        assertEquals(34, stats.getContestsParticipated());
        assertEquals(6, stats.getCurrentStreak());
        assertEquals(21, stats.getLongestStreak());
        assertEquals("ATCODER_REAL", stats.getSource());
        assertNotNull(stats.getLastSyncedAt());
        assertTrue(Platform.ATCODER.hasLiveStatsSource());
    }

    @Test
    void unavailableMetricsStayNull() {
        AtCoderClient client = username -> new AtCoderStats(null, null, null, 10, null, 0, 0);
        AtCoderAdapter adapter = new AtCoderAdapter(client);

        PlatformStats stats = adapter.fetchStats(account("sample_user"));

        assertNull(stats.getRating());
        assertNull(stats.getMaxRating());
        assertNull(stats.getRank());
        assertNull(stats.getContestsParticipated());
        assertEquals(10, stats.getTotalProblemsSolved());
        assertEquals(0, stats.getCurrentStreak());
        assertEquals(0, stats.getLongestStreak());
    }

    private PlatformAccount account(String username) {
        PlatformAccount account = new PlatformAccount();
        account.setPlatform(Platform.ATCODER);
        account.setUsername(username);
        return account;
    }
}

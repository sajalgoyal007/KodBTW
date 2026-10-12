package com.kodbtw.adapter;

import com.kodbtw.adapter.geeksforgeeks.GeeksForGeeksClient;
import com.kodbtw.adapter.geeksforgeeks.dto.GeeksForGeeksStatsResponse;
import com.kodbtw.adapter.impl.GeeksForGeeksAdapter;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class GeeksForGeeksAdapterTest {

    @Test
    void prototypeDoesNotEnableNormalPlatformSync() {
        assertFalse(Platform.GEEKSFORGEEKS.hasLiveStatsSource());
    }

    @Test
    void mapsOnlyDocumentedNormalizedMetricsAndKeepsUnsupportedValuesNull() {
        GeeksForGeeksStatsResponse response = new GeeksForGeeksStatsResponse(
                "success", "retrieved", "gfg", "sample_user", false,
                new GeeksForGeeksStatsResponse.Stats(
                        126, null, null,
                        new GeeksForGeeksStatsResponse.DifficultyCounts(8, 13, 40, 50, 15)
                )
        );
        GeeksForGeeksClient client = username -> response;
        GeeksForGeeksAdapter adapter = new GeeksForGeeksAdapter(client);
        PlatformAccount account = new PlatformAccount();
        account.setPlatform(Platform.GEEKSFORGEEKS);
        account.setUsername("sample_user");

        PlatformStats stats = adapter.fetchStats(account);

        assertEquals(Platform.GEEKSFORGEEKS, stats.getPlatform());
        assertEquals("sample_user", stats.getUsername());
        assertEquals("https://www.geeksforgeeks.org/profile/sample_user", stats.getProfileUrl());
        assertEquals(126, stats.getTotalProblemsSolved());
        assertEquals(40, stats.getEasySolved());
        assertEquals(50, stats.getMediumSolved());
        assertEquals(15, stats.getHardSolved());
        assertNull(stats.getRating());
        assertNull(stats.getMaxRating());
        assertNull(stats.getRank());
        assertNull(stats.getContestsParticipated());
        assertNull(stats.getCurrentStreak());
        assertNull(stats.getLongestStreak());
        assertEquals("GEEKSFORGEEKS_THIRD_PARTY", stats.getSource());
        assertNotNull(stats.getLastSyncedAt());
    }

    @Test
    void missingDifficultyObjectAndCountsRemainNull() {
        GeeksForGeeksStatsResponse response = new GeeksForGeeksStatsResponse(
                "success", "retrieved", "geeksforgeeks", "sample_user", true,
                new GeeksForGeeksStatsResponse.Stats(12, null, null, null)
        );
        GeeksForGeeksAdapter adapter = new GeeksForGeeksAdapter(username -> response);
        PlatformAccount account = new PlatformAccount();
        account.setPlatform(Platform.GEEKSFORGEEKS);
        account.setUsername("sample_user");

        PlatformStats stats = adapter.fetchStats(account);

        assertEquals(12, stats.getTotalProblemsSolved());
        assertNull(stats.getEasySolved());
        assertNull(stats.getMediumSolved());
        assertNull(stats.getHardSolved());
    }
}

package com.kodbtw.adapter;

import com.kodbtw.adapter.impl.AlgoMasterAdapter;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlgoMasterAdapterTest {

    private final AlgoMasterAdapter adapter = new AlgoMasterAdapter();

    @Test
    void algoMasterIsRegisteredButDeliberatelyLinkOnly() {
        assertEquals(Platform.ALGOMASTER, adapter.getPlatform());
        assertFalse(Platform.ALGOMASTER.hasLiveStatsSource());
        assertTrue(Platform.ALGOMASTER.isLinkOnly());
    }

    @Test
    void preservesSavedProfileUrlAndNeverInventsMetrics() {
        PlatformAccount account = new PlatformAccount();
        account.setPlatform(Platform.ALGOMASTER);
        account.setUsername("sample");
        account.setProfileUrl("https://algomaster.io/profile/sample");

        PlatformStats stats = adapter.fetchStats(account);

        assertEquals(Platform.ALGOMASTER, stats.getPlatform());
        assertEquals("sample", stats.getUsername());
        assertEquals("https://algomaster.io/profile/sample", stats.getProfileUrl());
        assertEquals("ALGOMASTER_LINK_ONLY", stats.getSource());
        assertNull(stats.getTotalProblemsSolved());
        assertNull(stats.getEasySolved());
        assertNull(stats.getMediumSolved());
        assertNull(stats.getHardSolved());
        assertNull(stats.getRating());
        assertNull(stats.getMaxRating());
        assertNull(stats.getRank());
        assertNull(stats.getContestsParticipated());
        assertNull(stats.getCurrentStreak());
        assertNull(stats.getLongestStreak());
        assertNull(stats.getLastSyncedAt());
    }

    @Test
    void fallsBackToTheSiteRootWhenNoProfileUrlIsStored() {
        PlatformAccount account = new PlatformAccount();
        account.setPlatform(Platform.ALGOMASTER);
        account.setUsername("sample");

        PlatformStats stats = adapter.fetchStats(account);

        assertNotNull(stats.getProfileUrl());
        assertEquals("https://algomaster.io/", stats.getProfileUrl());
    }
}

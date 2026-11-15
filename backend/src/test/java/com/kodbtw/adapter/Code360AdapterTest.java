package com.kodbtw.adapter;

import com.kodbtw.adapter.code360.Code360Client;
import com.kodbtw.adapter.code360.dto.Code360Stats;
import com.kodbtw.adapter.impl.Code360Adapter;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Code360AdapterTest {

    @Test
    void mapsParsedCode360MetricsAndProfilePageSource() {
        Code360Client client = username -> new Code360Stats(140, 1620, 9, 4, 18);
        Code360Adapter adapter = new Code360Adapter(client);
        PlatformAccount account = account("sample_user");

        PlatformStats stats = adapter.fetchStats(account);

        assertEquals(Platform.CODE360, stats.getPlatform());
        assertEquals("sample_user", stats.getUsername());
        assertEquals("https://www.naukri.com/code360/profile/sample_user", stats.getProfileUrl());
        assertEquals(140, stats.getTotalProblemsSolved());
        assertEquals(1620, stats.getRating());
        assertEquals(9, stats.getContestsParticipated());
        assertEquals(4, stats.getCurrentStreak());
        assertEquals(18, stats.getLongestStreak());
        assertNull(stats.getEasySolved());
        assertNull(stats.getMediumSolved());
        assertNull(stats.getHardSolved());
        assertNull(stats.getMaxRating());
        assertNull(stats.getRank());
        assertEquals("CODE360_THIRD_PARTY", stats.getSource());
        assertNotNull(stats.getLastSyncedAt());
        assertTrue(Platform.CODE360.hasLiveStatsSource());
    }

    @Test
    void missingFieldsStayNull() {
        Code360Client client = username -> new Code360Stats(null, null, null, null, 7);
        Code360Adapter adapter = new Code360Adapter(client);

        PlatformStats stats = adapter.fetchStats(account("sample_user"));

        assertNull(stats.getTotalProblemsSolved());
        assertNull(stats.getRating());
        assertNull(stats.getContestsParticipated());
        assertNull(stats.getCurrentStreak());
        assertEquals(7, stats.getLongestStreak());
    }

    private PlatformAccount account(String username) {
        PlatformAccount account = new PlatformAccount();
        account.setPlatform(Platform.CODE360);
        account.setUsername(username);
        return account;
    }
}

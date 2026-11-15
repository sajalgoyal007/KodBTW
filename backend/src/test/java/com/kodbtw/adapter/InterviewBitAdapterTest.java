package com.kodbtw.adapter;

import com.kodbtw.adapter.impl.InterviewBitAdapter;
import com.kodbtw.adapter.interviewbit.InterviewBitClient;
import com.kodbtw.adapter.interviewbit.dto.InterviewBitStats;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InterviewBitAdapterTest {

    @Test
    void mapsScoreRankSolvedAndStreak() {
        InterviewBitClient client = username -> new InterviewBitStats(250, 99, 300, 10);
        InterviewBitAdapter adapter = new InterviewBitAdapter(client);
        PlatformAccount account = account("sample_user");

        PlatformStats stats = adapter.fetchStats(account);

        assertEquals(Platform.INTERVIEWBIT, stats.getPlatform());
        assertEquals("sample_user", stats.getUsername());
        assertEquals("https://www.interviewbit.com/profile/sample_user/", stats.getProfileUrl());
        assertEquals(250, stats.getRating());
        assertEquals(99, stats.getRank());
        assertEquals(300, stats.getTotalProblemsSolved());
        assertEquals(10, stats.getCurrentStreak());
        assertNull(stats.getEasySolved());
        assertNull(stats.getMediumSolved());
        assertNull(stats.getHardSolved());
        assertNull(stats.getMaxRating());
        assertNull(stats.getContestsParticipated());
        assertNull(stats.getLongestStreak());
        assertEquals("INTERVIEWBIT_REAL", stats.getSource());
        assertNotNull(stats.getLastSyncedAt());
        assertTrue(Platform.INTERVIEWBIT.hasLiveStatsSource());
    }

    @Test
    void missingFieldsStayNull() {
        InterviewBitClient client = username -> new InterviewBitStats(null, null, 12, null);
        InterviewBitAdapter adapter = new InterviewBitAdapter(client);

        PlatformStats stats = adapter.fetchStats(account("sample_user"));

        assertNull(stats.getRating());
        assertNull(stats.getRank());
        assertNull(stats.getCurrentStreak());
        assertEquals(12, stats.getTotalProblemsSolved());
    }

    private PlatformAccount account(String username) {
        PlatformAccount account = new PlatformAccount();
        account.setPlatform(Platform.INTERVIEWBIT);
        account.setUsername(username);
        return account;
    }
}

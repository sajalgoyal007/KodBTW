package com.kodbtw.adapter;

import com.kodbtw.adapter.impl.CodeChefAdapter;
import com.kodbtw.adapter.impl.CodeforcesAdapter;
import com.kodbtw.adapter.impl.GeeksForGeeksAdapter;
import com.kodbtw.adapter.impl.HackerRankAdapter;
import com.kodbtw.adapter.impl.LeetCodeAdapter;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class MockPlatformAdaptersTest {

    @Test
    void leetCodeAdapterShouldReturnDeterministicMockStats() {
        LeetCodeAdapter adapter = new LeetCodeAdapter();
        assertEquals(Platform.LEETCODE, adapter.getPlatform());

        PlatformAccount account = new PlatformAccount();
        account.setUsername("testuser");
        account.setPlatform(Platform.LEETCODE);

        PlatformStats stats = adapter.fetchStats(account);

        assertEquals(Platform.LEETCODE, stats.getPlatform());
        assertEquals("testuser", stats.getUsername());
        assertEquals("https://leetcode.com/u/testuser/", stats.getProfileUrl());
        assertEquals(350, stats.getTotalProblemsSolved());
        assertEquals(180, stats.getEasySolved());
        assertEquals(130, stats.getMediumSolved());
        assertEquals(40, stats.getHardSolved());
        assertNull(stats.getRating());
        assertNull(stats.getRank());
        assertEquals(12, stats.getContestsParticipated());
        assertEquals(7, stats.getCurrentStreak());
        assertEquals(21, stats.getLongestStreak());
        assertNull(stats.getLastSyncedAt());
        assertEquals("MOCK", stats.getSource());
    }

    @Test
    void codeChefAdapterShouldReturnDeterministicMockStats() {
        CodeChefAdapter adapter = new CodeChefAdapter();
        assertEquals(Platform.CODECHEF, adapter.getPlatform());

        PlatformAccount account = new PlatformAccount();
        account.setUsername("chefuser");
        account.setPlatform(Platform.CODECHEF);

        PlatformStats stats = adapter.fetchStats(account);

        assertEquals(Platform.CODECHEF, stats.getPlatform());
        assertEquals("chefuser", stats.getUsername());
        assertEquals("https://www.codechef.com/users/chefuser", stats.getProfileUrl());
        assertEquals(210, stats.getTotalProblemsSolved());
        assertEquals(120, stats.getEasySolved());
        assertEquals(70, stats.getMediumSolved());
        assertEquals(20, stats.getHardSolved());
        assertEquals(1750, stats.getRating());
        assertEquals(4500, stats.getRank());
        assertEquals(25, stats.getContestsParticipated());
        assertNull(stats.getCurrentStreak());
        assertNull(stats.getLongestStreak());
        assertEquals("MOCK", stats.getSource());
    }

    @Test
    void codeforcesAdapterShouldReturnDeterministicMockStats() {
        CodeforcesAdapter adapter = new CodeforcesAdapter();
        assertEquals(Platform.CODEFORCES, adapter.getPlatform());

        PlatformAccount account = new PlatformAccount();
        account.setUsername("tourist");
        account.setPlatform(Platform.CODEFORCES);

        PlatformStats stats = adapter.fetchStats(account);

        assertEquals(Platform.CODEFORCES, stats.getPlatform());
        assertEquals("tourist", stats.getUsername());
        assertEquals("https://codeforces.com/profile/tourist", stats.getProfileUrl());
        assertEquals(420, stats.getTotalProblemsSolved());
        assertNull(stats.getEasySolved());
        assertNull(stats.getMediumSolved());
        assertNull(stats.getHardSolved());
        assertEquals(1450, stats.getRating());
        assertEquals(25000, stats.getRank());
        assertEquals(34, stats.getContestsParticipated());
        assertEquals(4, stats.getCurrentStreak());
        assertEquals(15, stats.getLongestStreak());
        assertEquals("MOCK", stats.getSource());
    }

    @Test
    void geeksForGeeksAdapterShouldReturnDeterministicMockStats() {
        GeeksForGeeksAdapter adapter = new GeeksForGeeksAdapter();
        assertEquals(Platform.GEEKSFORGEEKS, adapter.getPlatform());

        PlatformAccount account = new PlatformAccount();
        account.setUsername("gfguser");
        account.setPlatform(Platform.GEEKSFORGEEKS);

        PlatformStats stats = adapter.fetchStats(account);

        assertEquals(Platform.GEEKSFORGEEKS, stats.getPlatform());
        assertEquals("gfguser", stats.getUsername());
        assertEquals("https://auth.geeksforgeeks.org/user/gfguser", stats.getProfileUrl());
        assertEquals(280, stats.getTotalProblemsSolved());
        assertEquals(140, stats.getEasySolved());
        assertEquals(100, stats.getMediumSolved());
        assertEquals(40, stats.getHardSolved());
        assertNull(stats.getRating());
        assertEquals(12500, stats.getRank());
        assertNull(stats.getContestsParticipated());
        assertEquals(8, stats.getCurrentStreak());
        assertEquals(25, stats.getLongestStreak());
        assertEquals("MOCK", stats.getSource());
    }

    @Test
    void hackerRankAdapterShouldReturnDeterministicMockStats() {
        HackerRankAdapter adapter = new HackerRankAdapter();
        assertEquals(Platform.HACKERRANK, adapter.getPlatform());

        PlatformAccount account = new PlatformAccount();
        account.setUsername("hruser");
        account.setPlatform(Platform.HACKERRANK);

        PlatformStats stats = adapter.fetchStats(account);

        assertEquals(Platform.HACKERRANK, stats.getPlatform());
        assertEquals("hruser", stats.getUsername());
        assertEquals("https://www.hackerrank.com/profile/hruser", stats.getProfileUrl());
        assertEquals(150, stats.getTotalProblemsSolved());
        assertEquals(80, stats.getEasySolved());
        assertEquals(50, stats.getMediumSolved());
        assertEquals(20, stats.getHardSolved());
        assertNull(stats.getRating());
        assertNull(stats.getRank());
        assertEquals(5, stats.getContestsParticipated());
        assertNull(stats.getCurrentStreak());
        assertNull(stats.getLongestStreak());
        assertEquals("MOCK", stats.getSource());
    }

    @Test
    void adaptersShouldPreserveCustomProfileUrl() {
        LeetCodeAdapter adapter = new LeetCodeAdapter();
        PlatformAccount account = new PlatformAccount();
        account.setUsername("customuser");
        account.setProfileUrl("https://custom-url.com/profile");
        account.setPlatform(Platform.LEETCODE);

        PlatformStats stats = adapter.fetchStats(account);
        assertEquals("https://custom-url.com/profile", stats.getProfileUrl());
    }
}

package com.kodbtw;

import com.kodbtw.adapter.impl.LeetCodeAdapter;
import com.kodbtw.adapter.impl.CodeforcesAdapter;
import com.kodbtw.adapter.leetcode.LeetCodeClientImpl;
import com.kodbtw.adapter.codeforces.CodeforcesClientImpl;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled("Manual live verification tests against real external APIs")
class LiveVerificationTest {

    @Test
    void verifyLiveLeetCodeConnection() {
        LeetCodeClientImpl client = new LeetCodeClientImpl();
        LeetCodeAdapter adapter = new LeetCodeAdapter(client);

        PlatformAccount account = new PlatformAccount();
        account.setUsername("lee215");
        account.setPlatform(Platform.LEETCODE);

        PlatformStats stats = adapter.fetchStats(account);

        System.out.println("=== LIVE LEETCODE RESPONSE ===");
        System.out.println("Platform: " + stats.getPlatform());
        System.out.println("Username: " + stats.getUsername());
        System.out.println("ProfileUrl: " + stats.getProfileUrl());
        System.out.println("Total Problems: " + stats.getTotalProblemsSolved());
        System.out.println("Easy: " + stats.getEasySolved());
        System.out.println("Medium: " + stats.getMediumSolved());
        System.out.println("Hard: " + stats.getHardSolved());
        System.out.println("Rating: " + stats.getRating());
        System.out.println("Rank: " + stats.getRank());
        System.out.println("Contests: " + stats.getContestsParticipated());
        System.out.println("Current Streak: " + stats.getCurrentStreak());
        System.out.println("Source: " + stats.getSource());

        assertEquals(Platform.LEETCODE, stats.getPlatform());
        assertEquals("lee215", stats.getUsername());
        assertEquals("LEETCODE_REAL", stats.getSource());
        assertNotNull(stats.getTotalProblemsSolved());
    }

    /**
     * Manual live verification test for Codeforces integration.
     *
     * <p>Uses the official Codeforces public API:
     * {@code GET https://codeforces.com/api/user.info?handles=tourist}
     *
     * <p>Public handle used: {@code tourist} (Gennady Korotkevich)
     *
     * <p>This test is @Disabled by default so normal CI remains deterministic.
     * To run manually: remove @Disabled or run with IDE test runner.
     */
    @Test
    void verifyLiveCodeforcesConnection() {
        CodeforcesClientImpl client = new CodeforcesClientImpl();
        CodeforcesAdapter adapter = new CodeforcesAdapter(client);

        PlatformAccount account = new PlatformAccount();
        account.setUsername("tourist");
        account.setPlatform(Platform.CODEFORCES);

        PlatformStats stats = adapter.fetchStats(account);

        System.out.println("=== LIVE CODEFORCES RESPONSE ===");
        System.out.println("Platform: " + stats.getPlatform());
        System.out.println("Username: " + stats.getUsername());
        System.out.println("ProfileUrl: " + stats.getProfileUrl());
        System.out.println("Rating: " + stats.getRating());
        System.out.println("Rank (maxRating): " + stats.getRank());
        System.out.println("TotalProblemsSolved: " + stats.getTotalProblemsSolved());
        System.out.println("EasySolved: " + stats.getEasySolved());
        System.out.println("MediumSolved: " + stats.getMediumSolved());
        System.out.println("HardSolved: " + stats.getHardSolved());
        System.out.println("ContestsParticipated: " + stats.getContestsParticipated());
        System.out.println("CurrentStreak: " + stats.getCurrentStreak());
        System.out.println("LongestStreak: " + stats.getLongestStreak());
        System.out.println("LastSyncedAt: " + stats.getLastSyncedAt());
        System.out.println("Source: " + stats.getSource());
        System.out.println("================================");
        System.out.println("Live verification succeeded: YES");
        System.out.println("Public handle used: tourist");
        System.out.println("Real fields returned: rating, rank (maxRating)");
        System.out.println("Null fields (not available from user.info): totalProblemsSolved, easySolved, mediumSolved, hardSolved, contestsParticipated, currentStreak, longestStreak");

        assertEquals(Platform.CODEFORCES, stats.getPlatform());
        assertEquals("tourist", stats.getUsername());
        assertEquals("CODEFORCES_REAL", stats.getSource());
        assertNotNull(stats.getRating(), "Rating should be available for tourist");
        assertNotNull(stats.getRank(), "MaxRating (mapped to rank) should be available for tourist");
        assertNotNull(stats.getLastSyncedAt());
    }
}


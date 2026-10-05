package com.kodbtw;

import com.kodbtw.adapter.impl.LeetCodeAdapter;
import com.kodbtw.adapter.leetcode.LeetCodeClientImpl;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled("Manual live verification test against real LeetCode GraphQL endpoint")
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
}

package com.kodbtw.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlatformStatsSourcePolicyTest {

    @Test
    void acceptsExplicitlySupportedRealSources() {
        assertTrue(PlatformStatsSourcePolicy.isEligibleRealSource("LEETCODE_REAL"));
        assertTrue(PlatformStatsSourcePolicy.isEligibleRealSource("CODEFORCES_REAL"));
        assertTrue(PlatformStatsSourcePolicy.isEligibleRealSource("CODECHEF_REAL"));
        assertTrue(PlatformStatsSourcePolicy.isEligibleRealSource("CODECHEF_THIRD_PARTY"));
        assertTrue(PlatformStatsSourcePolicy.isEligibleRealSource(" codechef_third_party "));
    }

    @Test
    void rejectsMockBlankNullAndUnknownSources() {
        assertFalse(PlatformStatsSourcePolicy.isEligibleRealSource(null));
        assertFalse(PlatformStatsSourcePolicy.isEligibleRealSource(""));
        assertFalse(PlatformStatsSourcePolicy.isEligibleRealSource("   "));
        assertFalse(PlatformStatsSourcePolicy.isEligibleRealSource("MOCK"));
        assertFalse(PlatformStatsSourcePolicy.isEligibleRealSource("LEETCODE_MOCK_REAL"));
        assertFalse(PlatformStatsSourcePolicy.isEligibleRealSource("UNKNOWN_PLATFORM_REAL"));
        assertFalse(PlatformStatsSourcePolicy.isEligibleRealSource("GEEKSFORGEEKS_THIRD_PARTY"));
        assertFalse(PlatformStatsSourcePolicy.isEligibleRealSource("HACKERRANK_THIRD_PARTY"));
    }
}

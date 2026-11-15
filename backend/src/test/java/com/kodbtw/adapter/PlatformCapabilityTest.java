package com.kodbtw.adapter;

import com.kodbtw.entity.Platform;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlatformCapabilityTest {

    @Test
    void allTenPlatformsAreModelled() {
        assertEquals(10, Platform.values().length);
        assertEquals(Set.of("LEETCODE", "CODECHEF", "CODEFORCES", "ATCODER", "GEEKSFORGEEKS",
                        "CODE360", "HACKERRANK", "INTERVIEWBIT", "HACKEREARTH", "ALGOMASTER"),
                Arrays.stream(Platform.values()).map(Enum::name).collect(Collectors.toSet()));
    }

    @Test
    void everyPlatformExceptAlgoMasterHasALiveStatsSource() {
        for (Platform platform : Platform.values()) {
            if (platform == Platform.ALGOMASTER) {
                assertFalse(platform.hasLiveStatsSource(), platform + " must stay link-only");
            } else {
                assertTrue(platform.hasLiveStatsSource(), platform + " must have a live source");
            }
        }
    }

    @Test
    void algoMasterIsTheOnlyLinkOnlyPlatform() {
        for (Platform platform : Platform.values()) {
            assertEquals(platform == Platform.ALGOMASTER, platform.isLinkOnly(), platform + " link-only flag");
        }
    }
}

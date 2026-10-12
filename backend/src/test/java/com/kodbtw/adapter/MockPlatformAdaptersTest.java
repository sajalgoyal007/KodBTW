package com.kodbtw.adapter;

import com.kodbtw.entity.Platform;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class MockPlatformAdaptersTest {

    @Test
    void hackerRankPrototypeDoesNotEnableNormalPlatformSync() {
        assertFalse(Platform.HACKERRANK.hasLiveStatsSource());
    }
}

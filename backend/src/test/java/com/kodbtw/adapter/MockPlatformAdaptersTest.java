package com.kodbtw.adapter;

import com.kodbtw.adapter.impl.GeeksForGeeksAdapter;
import com.kodbtw.adapter.impl.HackerRankAdapter;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;
import com.kodbtw.exception.PlatformSyncUnavailableException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MockPlatformAdaptersTest {

    @Test
    void geeksForGeeksAdapterReportsLiveSyncUnavailable() {
        GeeksForGeeksAdapter adapter = new GeeksForGeeksAdapter();
        assertEquals(Platform.GEEKSFORGEEKS, adapter.getPlatform());

        PlatformAccount account = new PlatformAccount();
        account.setUsername("gfguser");
        account.setPlatform(Platform.GEEKSFORGEEKS);

        assertEquals("GeeksforGeeks live sync unavailable",
                assertThrows(PlatformSyncUnavailableException.class, () -> adapter.fetchStats(account)).getMessage());
    }

    @Test
    void hackerRankAdapterReportsLiveSyncUnavailable() {
        HackerRankAdapter adapter = new HackerRankAdapter();
        assertEquals(Platform.HACKERRANK, adapter.getPlatform());

        PlatformAccount account = new PlatformAccount();
        account.setUsername("hruser");
        account.setPlatform(Platform.HACKERRANK);

        assertEquals("HackerRank live sync unavailable",
                assertThrows(PlatformSyncUnavailableException.class, () -> adapter.fetchStats(account)).getMessage());
    }
}

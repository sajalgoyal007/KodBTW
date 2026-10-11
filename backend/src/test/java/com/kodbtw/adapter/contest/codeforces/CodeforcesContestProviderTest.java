package com.kodbtw.adapter.contest.codeforces;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CodeforcesContestProviderTest {
    private final CodeforcesContestClient client = mock(CodeforcesContestClient.class);
    private final CodeforcesContestProvider provider = new CodeforcesContestProvider(client);

    @Test
    void mapsOnlyValidUpcomingContestsAndSortsChronologically() {
        long now = Instant.now().getEpochSecond();
        when(client.fetchContests()).thenReturn(List.of(
                contest(44, "Later", "BEFORE", now + 7200, 3600),
                contest(43, "Earlier", "BEFORE", now + 1800, 5400),
                contest(42, "Running", "CODING", now - 60, 3600),
                contest(41, "Missing time", "BEFORE", null, 3600),
                contest(40, "Invalid time", "BEFORE", Long.MAX_VALUE, 600),
                contest(-1, "Invalid id", "BEFORE", now + 100, 600),
                contest(38, "Invalid duration", "BEFORE", now + 100, 0),
                contest(39, " ", "BEFORE", now + 100, 600)));

        var result = provider.fetchUpcoming(Instant.parse("2026-10-11T12:00:00Z"));

        assertEquals(List.of("43", "44"), result.stream().map(event -> event.id()).toList());
        assertEquals("Earlier", result.get(0).name());
        assertEquals(Instant.ofEpochSecond(now + 1800), result.get(0).startTime());
        assertEquals(5400, result.get(0).durationSeconds());
        assertEquals("https://codeforces.com/contest/43", result.get(0).officialUrl());
        assertEquals("CODEFORCES_API", result.get(0).source());
        assertEquals(Instant.parse("2026-10-11T12:00:00Z"), result.get(0).fetchedAt());
    }

    private static CodeforcesContestApiResponse.CodeforcesContestRecord contest(
            int id, String name, String phase, Long start, long duration) {
        return new CodeforcesContestApiResponse.CodeforcesContestRecord(id, name, phase, duration, start);
    }
}

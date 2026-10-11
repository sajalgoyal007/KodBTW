package com.kodbtw.service;

import com.kodbtw.adapter.contest.fixture.FixtureContestProvider;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContestCalendarFixtureScenarioTest {
    @Test
    void multipleScenarioReturnsAvailableLabeledFixtureContests() {
        MutableClock clock = new MutableClock(Instant.parse("2026-10-11T12:00:00Z"));
        ContestCalendarService service = service("multiple", clock);

        var response = service.getCalendar(50);

        assertEquals("AVAILABLE", response.sources().get(0).status());
        assertEquals("LOCAL_FIXTURE", response.sources().get(0).source());
        assertEquals(3, response.contests().size());
        assertTrue(response.contests().get(0).name().startsWith("Fixture:"));
        assertEquals("LOCAL_FIXTURE", response.contests().get(0).source());
        assertEquals("https://codeforces.com/contests", response.contests().get(0).officialUrl());
        assertEquals(clock.instant(), response.sources().get(0).fetchedAt());
    }

    @Test
    void emptyScenarioIsAnAvailableEmptySchedule() {
        var response = service("empty", new MutableClock(Instant.parse("2026-10-11T12:00:00Z")))
                .getCalendar(50);

        assertEquals("AVAILABLE", response.sources().get(0).status());
        assertEquals("LOCAL_FIXTURE", response.sources().get(0).source());
        assertTrue(response.contests().isEmpty());
        assertFalse(response.stale());
    }

    @Test
    void unavailableScenarioReturnsSafeUnavailableResponse() {
        var response = service("unavailable", new MutableClock(Instant.parse("2026-10-11T12:00:00Z")))
                .getCalendar(50);

        assertEquals("UNAVAILABLE", response.sources().get(0).status());
        assertEquals("LOCAL_FIXTURE", response.sources().get(0).source());
        assertTrue(response.contests().isEmpty());
        assertEquals("Contest schedule is temporarily unavailable.", response.sources().get(0).message());
    }

    @Test
    void staleScenarioPreservesLastSuccessfulTimestampAfterRefreshFailure() {
        MutableClock clock = new MutableClock(Instant.parse("2026-10-11T12:00:00Z"));
        ContestCalendarService service = service("stale", clock);
        var first = service.getCalendar(50);
        clock.advance(Duration.ofSeconds(2));

        var stale = service.getCalendar(50);

        assertEquals("STALE", stale.sources().get(0).status());
        assertEquals("LOCAL_FIXTURE", stale.sources().get(0).source());
        assertTrue(stale.stale());
        assertEquals(first.sources().get(0).fetchedAt(), stale.sources().get(0).fetchedAt());
        assertEquals(first.contests(), stale.contests());
        assertEquals("Contest schedule is temporarily unavailable.", stale.sources().get(0).message());
    }

    private static ContestCalendarService service(String scenario, MutableClock clock) {
        return new ContestCalendarService(List.of(new FixtureContestProvider(scenario)), Duration.ofSeconds(2), clock);
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override public ZoneId getZone() { return ZoneId.of("UTC"); }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return instant; }
    }
}

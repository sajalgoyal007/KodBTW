package com.kodbtw.service;

import com.kodbtw.adapter.contest.ContestProvider;
import com.kodbtw.dto.contest.ContestDto;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ContestCalendarServiceTest {
    @Test
    void cachesSuccessfulResultUntilTtlExpires() {
        MutableClock clock = new MutableClock(Instant.parse("2026-10-11T00:00:00Z"));
        FakeProvider provider = new FakeProvider();
        ContestCalendarService service = new ContestCalendarService(List.of(provider), Duration.ofMinutes(10), clock);

        service.getCalendar(10);
        service.getCalendar(10);
        assertEquals(1, provider.calls.get());
        clock.advance(Duration.ofMinutes(10));
        service.getCalendar(10);
        assertEquals(2, provider.calls.get());
    }

    @Test
    void rejectsNonPositiveAndUnreasonablyLongCacheTtl() {
        FakeProvider provider = new FakeProvider();
        Clock clock = Clock.systemUTC();

        assertThrows(IllegalArgumentException.class,
                () -> new ContestCalendarService(List.of(provider), Duration.ZERO, clock));
        assertThrows(IllegalArgumentException.class,
                () -> new ContestCalendarService(List.of(provider), Duration.ofSeconds(-1), clock));
        assertThrows(IllegalArgumentException.class,
                () -> new ContestCalendarService(List.of(provider), Duration.ofHours(25), clock));
    }

    @Test
    void limitsReturnedResultsWithoutTruncatingCachedSchedule() {
        MutableClock clock = new MutableClock(Instant.parse("2026-10-11T00:00:00Z"));
        FakeProvider provider = new FakeProvider();
        provider.results = List.of(
                event("12", clock.instant().plusSeconds(3600)),
                event("13", clock.instant().plusSeconds(7200)),
                event("14", clock.instant().plusSeconds(10800)));
        ContestCalendarService service = new ContestCalendarService(List.of(provider), Duration.ofMinutes(10), clock);

        assertEquals(2, service.getCalendar(2).contests().size());
        assertEquals(3, service.getCalendar(10).contests().size());
        assertEquals(1, provider.calls.get());
    }

    @Test
    void concurrentRequestsShareOneRefresh() throws Exception {
        FakeProvider provider = new FakeProvider();
        provider.waitOnFetch = true;
        ContestCalendarService service = new ContestCalendarService(List.of(provider), Duration.ofMinutes(10),
                new MutableClock(Instant.parse("2026-10-11T00:00:00Z")));
        var executor = Executors.newFixedThreadPool(6);
        try {
            var futures = java.util.stream.IntStream.range(0, 6)
                    .mapToObj(ignored -> executor.submit(() -> service.getCalendar(10)))
                    .toList();
            assertTrue(provider.entered.await(2, TimeUnit.SECONDS));
            provider.release.countDown();
            for (var future : futures) assertEquals(1, future.get(2, TimeUnit.SECONDS).contests().size());
            assertEquals(1, provider.calls.get());
        } finally {
            provider.release.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void servesLastSuccessfulEventsAsStaleAfterProviderFailureWithoutLeakingError() {
        MutableClock clock = new MutableClock(Instant.parse("2026-10-11T00:00:00Z"));
        FakeProvider provider = new FakeProvider();
        ContestCalendarService service = new ContestCalendarService(List.of(provider), Duration.ofMinutes(1), clock);
        var first = service.getCalendar(10);
        provider.failure = new IllegalStateException("mysql-password=secret");
        clock.advance(Duration.ofMinutes(1));

        var stale = service.getCalendar(10);

        assertEquals(first.contests(), stale.contests());
        assertTrue(stale.stale());
        assertEquals("STALE", stale.sources().get(0).status());
        assertEquals(first.sources().get(0).fetchedAt(), stale.sources().get(0).fetchedAt(),
                "last successful fetch time must not be replaced by the failed refresh attempt");
        assertEquals("Contest schedule is temporarily unavailable.", stale.sources().get(0).message());
        assertFalse(stale.sources().get(0).message().contains("secret"));
        assertEquals(2, provider.calls.get());
    }

    @Test
    void initialFailureIsUnavailableAndCachedBrieflyWithoutExposingException() {
        MutableClock clock = new MutableClock(Instant.parse("2026-10-11T00:00:00Z"));
        FakeProvider provider = new FakeProvider();
        provider.failure = new IllegalStateException("upstream private detail");
        ContestCalendarService service = new ContestCalendarService(List.of(provider), Duration.ofMinutes(10), clock);

        var response = service.getCalendar(10);
        service.getCalendar(10);

        assertTrue(response.contests().isEmpty());
        assertFalse(response.stale());
        assertEquals("UNAVAILABLE", response.sources().get(0).status());
        assertFalse(response.sources().get(0).message().contains("private detail"));
        assertEquals(1, provider.calls.get());
    }

    private static final class FakeProvider implements ContestProvider {
        private final AtomicInteger calls = new AtomicInteger();
        private final CountDownLatch entered = new CountDownLatch(1);
        private final CountDownLatch release = new CountDownLatch(1);
        private volatile RuntimeException failure;
        private volatile boolean waitOnFetch;
        private List<ContestDto> results;

        @Override public String platform() { return "CODEFORCES"; }
        @Override public String source() { return "CODEFORCES_API"; }
        @Override public List<ContestDto> fetchUpcoming(Instant fetchedAt) {
            calls.incrementAndGet();
            entered.countDown();
            if (waitOnFetch) {
                try {
                    if (!release.await(2, TimeUnit.SECONDS)) throw new IllegalStateException("test timeout");
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(exception);
                }
            }
            if (failure != null) throw failure;
            return results == null ? List.of(event("12", fetchedAt.plusSeconds(3600))) : results;
        }
    }

    private static ContestDto event(String id, Instant startTime) {
        return new ContestDto(id, "CODEFORCES", "Test contest", startTime, 7200,
                "https://codeforces.com/contest/" + id, "CODEFORCES_API", startTime.minusSeconds(3600));
    }

    private static final class MutableClock extends Clock {
        private Instant instant;
        private MutableClock(Instant instant) { this.instant = instant; }
        private void advance(Duration duration) { instant = instant.plus(duration); }
        @Override public ZoneId getZone() { return ZoneId.of("UTC"); }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return instant; }
    }
}

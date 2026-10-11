package com.kodbtw.adapter.contest.fixture;

import com.kodbtw.adapter.contest.ContestProvider;
import com.kodbtw.adapter.contest.ContestProviderUnavailableException;
import com.kodbtw.dto.contest.ContestDto;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/** Deterministic local-only contest fixtures. Never contacts an upstream provider. */
@Component
@ConditionalOnProperty(prefix = "contest-calendar", name = "provider", havingValue = "fixture")
public class FixtureContestProvider implements ContestProvider {
    private static final String PLATFORM = "CODEFORCES";
    private static final String SOURCE = "LOCAL_FIXTURE";
    private static final String OFFICIAL_CONTESTS_URL = "https://codeforces.com/contests";

    private final FixtureScenario scenario;
    private final AtomicInteger fetchCount = new AtomicInteger();

    @Autowired
    public FixtureContestProvider(
            @Value("${contest-calendar.fixture.scenario:multiple}") String scenario) {
        this.scenario = parseScenario(scenario);
    }

    private static FixtureScenario parseScenario(String value) {
        try {
            return FixtureScenario.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException exception) {
            throw new IllegalStateException(
                    "Invalid contest-calendar.fixture.scenario. Supported values: multiple, empty, unavailable, stale.");
        }
    }

    @Override
    public String platform() {
        return PLATFORM;
    }

    @Override
    public String source() {
        return SOURCE;
    }

    @Override
    public List<ContestDto> fetchUpcoming(Instant fetchedAt) {
        int requestNumber = fetchCount.incrementAndGet();
        if (scenario == FixtureScenario.UNAVAILABLE
                || (scenario == FixtureScenario.STALE && requestNumber > 1)) {
            throw new ContestProviderUnavailableException();
        }
        if (scenario == FixtureScenario.EMPTY) {
            return List.of();
        }
        return List.of(
                event("fixture-1", "Fixture: Codeforces Round", fetchedAt.plusSeconds(3_600), 7_200, fetchedAt),
                event("fixture-2", "Fixture: Educational Round", fetchedAt.plusSeconds(86_400), 10_800, fetchedAt),
                event("fixture-3", "Fixture: Global Round", fetchedAt.plusSeconds(172_800), 14_400, fetchedAt));
    }

    private ContestDto event(String id, String name, Instant startTime, long durationSeconds, Instant fetchedAt) {
        return new ContestDto(id, PLATFORM, name, startTime, durationSeconds,
                OFFICIAL_CONTESTS_URL, SOURCE, fetchedAt);
    }

    private enum FixtureScenario {
        MULTIPLE,
        EMPTY,
        UNAVAILABLE,
        STALE
    }
}

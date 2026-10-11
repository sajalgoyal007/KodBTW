package com.kodbtw.adapter.contest.codeforces;

import com.kodbtw.adapter.contest.ContestProvider;
import com.kodbtw.dto.contest.ContestDto;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.net.URI;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@ConditionalOnProperty(prefix = "contest-calendar", name = "provider", havingValue = "codeforces", matchIfMissing = true)
public class CodeforcesContestProvider implements ContestProvider {
    private static final String PLATFORM = "CODEFORCES";
    private static final String SOURCE = "CODEFORCES_API";
    private final CodeforcesContestClient client;

    public CodeforcesContestProvider(CodeforcesContestClient client) {
        this.client = client;
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
        Instant now = Instant.now();
        return client.fetchContests().stream()
                .filter(Objects::nonNull)
                .flatMap(contest -> mapIfValid(contest, now, fetchedAt).stream())
                .sorted(Comparator.comparing(ContestDto::startTime).thenComparing(ContestDto::id))
                .limit(100)
                .toList();
    }

    private Optional<ContestDto> mapIfValid(
            CodeforcesContestApiResponse.CodeforcesContestRecord contest,
            Instant now,
            Instant fetchedAt) {
        if (!"BEFORE".equals(contest.phase()) || contest.id() == null || contest.id() <= 0
                || contest.name() == null || contest.name().isBlank()
                || contest.startTimeSeconds() == null
                || contest.durationSeconds() == null || contest.durationSeconds() <= 0) {
            return Optional.empty();
        }
        try {
            Instant startTime = Instant.ofEpochSecond(contest.startTimeSeconds());
            if (!startTime.isAfter(now)) return Optional.empty();
            String officialUrl = URI.create("https://codeforces.com/contest/" + contest.id()).toString();
            return Optional.of(new ContestDto(contest.id().toString(), PLATFORM, contest.name().trim(),
                    startTime, contest.durationSeconds(), officialUrl, SOURCE, fetchedAt));
        } catch (DateTimeException | IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}

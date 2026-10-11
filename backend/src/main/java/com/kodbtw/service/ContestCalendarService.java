package com.kodbtw.service;

import com.kodbtw.adapter.contest.ContestProvider;
import com.kodbtw.dto.contest.ContestCalendarResponse;
import com.kodbtw.dto.contest.ContestDto;
import com.kodbtw.dto.contest.ContestSourceStatusDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Service
public class ContestCalendarService {
    private static final Logger log = LoggerFactory.getLogger(ContestCalendarService.class);
    private static final Duration FAILURE_RETRY_DELAY = Duration.ofMinutes(1);
    private static final Duration MAX_CACHE_TTL = Duration.ofHours(24);
    private static final String UNAVAILABLE_MESSAGE = "Contest schedule is temporarily unavailable.";

    private final List<ContestProvider> providers;
    private final Duration cacheTtl;
    private final Clock clock;
    private final Object cacheLock = new Object();
    private ContestCalendarResponse cachedResponse;
    private Instant cacheValidUntil = Instant.MIN;
    private Instant retryNotBefore = Instant.MIN;
    private CompletableFuture<ContestCalendarResponse> refreshInFlight;

    @Autowired
    public ContestCalendarService(
            List<ContestProvider> providers,
            @Value("${contest-calendar.cache-ttl:PT10M}") Duration cacheTtl) {
        this(providers, validateCacheTtl(cacheTtl), Clock.systemUTC());
    }

    ContestCalendarService(List<ContestProvider> providers, Duration cacheTtl, Clock clock) {
        this.providers = List.copyOf(providers);
        this.cacheTtl = validateCacheTtl(cacheTtl);
        this.clock = clock;
    }

    private static Duration validateCacheTtl(Duration cacheTtl) {
        if (cacheTtl == null || cacheTtl.isZero() || cacheTtl.isNegative()
                || cacheTtl.compareTo(MAX_CACHE_TTL) > 0) {
            throw new IllegalArgumentException(
                    "Contest calendar cache TTL must be positive and no more than 24 hours.");
        }
        return cacheTtl;
    }

    public ContestCalendarResponse getCalendar(int limit) {
        CompletableFuture<ContestCalendarResponse> future;
        boolean refreshOwner = false;
        synchronized (cacheLock) {
            Instant now = clock.instant();
            if (cachedResponse != null && now.isBefore(cacheValidUntil)) {
                return limit(cachedResponse, limit);
            }
            if (cachedResponse != null && now.isBefore(retryNotBefore)) {
                return limit(asStale(cachedResponse, now), limit);
            }
            if (refreshInFlight != null) {
                future = refreshInFlight;
            } else {
                future = new CompletableFuture<>();
                refreshInFlight = future;
                refreshOwner = true;
            }
        }

        if (refreshOwner) {
            try {
                ContestCalendarResponse result = refresh(staleCandidateOrNull());
                synchronized (cacheLock) {
                    cachedResponse = result;
                    boolean unavailable = result.sources().stream().anyMatch(source -> !source.status().equals("AVAILABLE"));
                    cacheValidUntil = unavailable ? clock.instant().plus(FAILURE_RETRY_DELAY)
                            : clock.instant().plus(cacheTtl);
                    retryNotBefore = unavailable ? cacheValidUntil : Instant.MIN;
                    refreshInFlight = null;
                }
                future.complete(result);
            } catch (RuntimeException exception) {
                synchronized (cacheLock) {
                    refreshInFlight = null;
                }
                future.completeExceptionally(exception);
            }
        }

        try {
            return limit(future.join(), limit);
        } catch (CompletionException exception) {
            throw exception;
        }
    }

    private ContestCalendarResponse refresh(ContestCalendarResponse previous) {
        Instant fetchedAt = clock.instant();
        Instant expiresAt = fetchedAt.plus(cacheTtl);
        List<ContestDto> contests = new ArrayList<>();
        List<ContestSourceStatusDto> statuses = new ArrayList<>();
        boolean anyFailure = false;
        boolean staleDataUsed = false;

        for (ContestProvider provider : providers) {
            try {
                List<ContestDto> providerContests = provider.fetchUpcoming(fetchedAt);
                contests.addAll(providerContests);
                statuses.add(new ContestSourceStatusDto(
                        provider.platform(), "AVAILABLE", null, fetchedAt, expiresAt, provider.source()));
            } catch (RuntimeException exception) {
                anyFailure = true;
                log.warn("Contest provider {} unavailable ({})", provider.platform(), exception.getClass().getSimpleName());
                List<ContestDto> previousContests = previous == null ? List.of()
                        : previous.contests().stream().filter(contest -> contest.platform().equals(provider.platform())).toList();
                contests.addAll(previousContests);
                Instant priorFetchedAt = previous == null ? null : previous.sources().stream()
                        .filter(source -> source.platform().equals(provider.platform()))
                        .map(ContestSourceStatusDto::fetchedAt).findFirst().orElse(null);
                staleDataUsed |= priorFetchedAt != null;
                statuses.add(new ContestSourceStatusDto(provider.platform(),
                        priorFetchedAt == null ? "UNAVAILABLE" : "STALE",
                        UNAVAILABLE_MESSAGE, priorFetchedAt, fetchedAt.plus(FAILURE_RETRY_DELAY), provider.source()));
            }
        }

        contests.sort(Comparator.comparing(ContestDto::startTime).thenComparing(ContestDto::platform).thenComparing(ContestDto::id));
        return new ContestCalendarResponse(fetchedAt, anyFailure && staleDataUsed,
                List.copyOf(statuses), List.copyOf(contests));
    }

    private ContestCalendarResponse staleCandidateOrNull() {
        synchronized (cacheLock) {
            return cachedResponse;
        }
    }

    private ContestCalendarResponse asStale(ContestCalendarResponse response, Instant now) {
        List<ContestSourceStatusDto> statuses = response.sources().stream()
                .map(source -> new ContestSourceStatusDto(source.platform(),
                        source.fetchedAt() == null ? "UNAVAILABLE" : "STALE",
                        UNAVAILABLE_MESSAGE, source.fetchedAt(), now.plus(FAILURE_RETRY_DELAY), source.source()))
                .toList();
        boolean hasStaleData = statuses.stream().anyMatch(source -> source.fetchedAt() != null);
        return new ContestCalendarResponse(response.generatedAt(), hasStaleData, statuses, response.contests());
    }

    private ContestCalendarResponse limit(ContestCalendarResponse response, int limit) {
        List<ContestDto> limited = response.contests().stream().limit(limit).toList();
        return new ContestCalendarResponse(response.generatedAt(), response.stale(), response.sources(), limited);
    }
}

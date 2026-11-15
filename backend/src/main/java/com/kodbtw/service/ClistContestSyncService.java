package com.kodbtw.service;

import com.kodbtw.adapter.contest.clist.ClistContestApiResponse;
import com.kodbtw.adapter.contest.codeforces.CodeforcesContestApiResponse;
import com.kodbtw.contest.ContestPlatform;
import com.kodbtw.contest.ContestUrls;
import com.kodbtw.entity.ContestEvent;
import com.kodbtw.repository.ContestEventRepository;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Keeps {@code contest_events} fresh for all eight supported platforms.
 *
 * <p>Primary source is the clist.by API v4. When credentials are missing or any
 * clist call fails, the service degrades to the official public Codeforces API so
 * the calendar always has data. Every write is an idempotent upsert keyed by
 * {@code external_id}.</p>
 */
@Service
public class ClistContestSyncService {

    private static final Logger log = LoggerFactory.getLogger(ClistContestSyncService.class);

    static final Duration PAST_WINDOW = Duration.ofDays(7);
    static final Duration FUTURE_WINDOW = Duration.ofDays(45);
    static final int CLIST_LIMIT = 500;
    static final String CODEFORCES_API_URL = "https://codeforces.com/api/contest.list?gym=false";
    static final String CLIST_EXTERNAL_PREFIX = "CLIST:";
    static final String CODEFORCES_EXTERNAL_PREFIX = "CODEFORCES:";
    private static final DateTimeFormatter CLIST_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final RestClient restClient;
    private final ContestEventRepository repository;
    private final String clistUsername;
    private final String clistApiKey;
    private final String clistBaseUrl;
    private final boolean syncEnabled;
    private final Clock clock;
    private final ExecutorService startupExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "contest-sync-startup");
        thread.setDaemon(true);
        return thread;
    });

    @Autowired
    public ClistContestSyncService(
            RestClient.Builder restClientBuilder,
            ContestEventRepository repository,
            @Value("${app.clist.username:}") String clistUsername,
            @Value("${app.clist.api-key:}") String clistApiKey,
            @Value("${app.clist.base-url:https://clist.by/api/v4}") String clistBaseUrl,
            @Value("${app.contests.sync-enabled:true}") boolean syncEnabled) {
        this(restClientBuilder, repository, clistUsername, clistApiKey, clistBaseUrl, syncEnabled,
                Clock.systemUTC());
    }

    ClistContestSyncService(
            RestClient.Builder restClientBuilder,
            ContestEventRepository repository,
            String clistUsername,
            String clistApiKey,
            String clistBaseUrl,
            boolean syncEnabled,
            Clock clock) {
        this.restClient = restClientBuilder.build();
        this.repository = repository;
        this.clistUsername = clistUsername == null ? "" : clistUsername.trim();
        this.clistApiKey = clistApiKey == null ? "" : clistApiKey.trim();
        this.clistBaseUrl = clistBaseUrl == null || clistBaseUrl.isBlank()
                ? "https://clist.by/api/v4"
                : stripTrailingSlash(clistBaseUrl.trim());
        this.syncEnabled = syncEnabled;
        this.clock = clock;
    }

    /** Safe, non-blocking first fill so startup never waits on a third-party API. */
    @EventListener(ApplicationReadyEvent.class)
    public void syncOnStartupIfEmpty() {
        if (!syncEnabled) {
            return;
        }
        try {
            if (repository.count() > 0) {
                return;
            }
        } catch (RuntimeException exception) {
            log.warn("Could not check contest_events before initial sync: {}", exception.getMessage());
            return;
        }
        startupExecutor.submit(() -> {
            try {
                SyncResult result = syncNow();
                log.info("Initial contest sync finished: {}", result);
            } catch (RuntimeException exception) {
                log.warn("Initial contest sync failed: {}", exception.getMessage());
            }
        });
    }

    @Scheduled(cron = "${app.contests.sync-cron:0 0 */6 * * *}")
    public void scheduledSync() {
        if (!syncEnabled) {
            return;
        }
        try {
            log.info("Scheduled contest sync finished: {}", syncNow());
        } catch (RuntimeException exception) {
            log.warn("Scheduled contest sync failed: {}", exception.getMessage());
        }
    }

    /** Synchronously refreshes the contest window and returns what changed. */
    public SyncResult syncNow() {
        Instant now = clock.instant();
        boolean fallbackUsed = false;
        List<ContestDraft> drafts;

        if (hasClistCredentials()) {
            try {
                drafts = fetchFromClist(now);
            } catch (RuntimeException exception) {
                log.warn("clist.by sync failed ({}); falling back to the Codeforces API",
                        exception.getMessage());
                drafts = fetchFromCodeforces(now);
                fallbackUsed = true;
            }
        } else {
            log.debug("CLIST_USERNAME/CLIST_API_KEY not configured; using the Codeforces API only");
            drafts = fetchFromCodeforces(now);
            fallbackUsed = true;
        }

        Map<String, ContestDraft> unique = new LinkedHashMap<>();
        for (ContestDraft draft : drafts) {
            unique.putIfAbsent(draft.externalId(), draft);
        }

        LocalDateTime syncedAt = LocalDateTime.ofInstant(now, ZoneOffset.UTC);
        int inserted = 0;
        int updated = 0;
        for (ContestDraft draft : unique.values()) {
            Optional<ContestEvent> existing = repository.findByExternalId(draft.externalId());
            ContestEvent event = existing.orElseGet(ContestEvent::new);
            event.setExternalId(draft.externalId());
            event.setPlatform(draft.platform().name());
            event.setTitle(draft.title());
            event.setStartTimeUtc(LocalDateTime.ofInstant(draft.start(), ZoneOffset.UTC));
            event.setEndTimeUtc(LocalDateTime.ofInstant(draft.end(), ZoneOffset.UTC));
            event.setDurationSeconds(draft.durationSeconds());
            event.setContestUrl(draft.contestUrl());
            event.setLastSyncedAt(syncedAt);
            repository.save(event);
            if (existing.isPresent()) {
                updated++;
            } else {
                inserted++;
            }
        }

        return new SyncResult(unique.size(), inserted, updated, fallbackUsed);
    }

    public boolean hasClistCredentials() {
        return !clistUsername.isBlank() && !clistApiKey.isBlank();
    }

    private List<ContestDraft> fetchFromClist(Instant now) {
        URI uri = buildClistUri(now);
        ClistContestApiResponse response = restClient.get()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "ApiKey " + clistUsername + ":" + clistApiKey)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(ClistContestApiResponse.class);

        if (response == null || response.objects() == null) {
            throw new IllegalStateException("clist.by returned an empty contest payload");
        }

        List<ContestDraft> drafts = new ArrayList<>();
        for (ClistContestApiResponse.ClistContestRecord record : response.objects()) {
            if (record == null || record.id() == null) {
                continue;
            }
            Optional<ContestPlatform> platform = ContestPlatform.fromResource(
                    record.resource() == null ? null : record.resource().name());
            if (platform.isEmpty()) {
                continue;
            }
            Instant start = parseUtc(record.start());
            if (start == null) {
                continue;
            }
            Instant end = parseUtc(record.end());
            long duration = record.duration() != null && record.duration() > 0
                    ? record.duration()
                    : (end != null ? Duration.between(start, end).getSeconds() : 0L);
            if (duration <= 0) {
                continue;
            }
            if (end == null) {
                end = start.plusSeconds(duration);
            }
            if (!overlapsWindow(start, end, now)) {
                continue;
            }
            String title = record.event() == null ? "" : record.event().trim();
            if (title.isEmpty() || !ContestUrls.isSafeHttpUrl(record.href())) {
                continue;
            }
            drafts.add(new ContestDraft(CLIST_EXTERNAL_PREFIX + record.id(), platform.get(), title,
                    start, end, duration, record.href().trim()));
        }
        return drafts;
    }

    private URI buildClistUri(Instant now) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(clistBaseUrl)
                .path("/contest/")
                .queryParam("limit", CLIST_LIMIT);
        for (ContestPlatform platform : ContestPlatform.values()) {
            builder.queryParam("resource", platform.clistResource());
        }
        builder.queryParam("start__gte", formatClistTime(now.minus(PAST_WINDOW)))
                .queryParam("end__lte", formatClistTime(now.plus(FUTURE_WINDOW)));
        builder.queryParam("order_by", "start");
        return builder.encode().build().toUri();
    }

    private List<ContestDraft> fetchFromCodeforces(Instant now) {
        CodeforcesContestApiResponse response = restClient.get()
                .uri(CODEFORCES_API_URL)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(CodeforcesContestApiResponse.class);

        if (response == null || !"OK".equals(response.status()) || response.result() == null) {
            throw new IllegalStateException("Codeforces contest API is unavailable");
        }

        List<ContestDraft> drafts = new ArrayList<>();
        for (CodeforcesContestApiResponse.CodeforcesContestRecord record : response.result()) {
            if (record == null || record.id() == null || record.id() <= 0
                    || record.name() == null || record.name().isBlank()
                    || record.startTimeSeconds() == null
                    || record.durationSeconds() == null || record.durationSeconds() <= 0) {
                continue;
            }
            Instant start = Instant.ofEpochSecond(record.startTimeSeconds());
            long duration = record.durationSeconds();
            Instant end = start.plusSeconds(duration);
            if (!overlapsWindow(start, end, now)) {
                continue;
            }
            String url = "https://codeforces.com/contest/" + record.id();
            drafts.add(new ContestDraft(CODEFORCES_EXTERNAL_PREFIX + record.id(), ContestPlatform.CODEFORCES,
                    record.name().trim(), start, end, duration, url));
        }
        return drafts;
    }

    private static boolean overlapsWindow(Instant start, Instant end, Instant now) {
        return end.isAfter(now.minus(PAST_WINDOW)) && start.isBefore(now.plus(FUTURE_WINDOW));
    }

    private static String formatClistTime(Instant instant) {
        return CLIST_TIME_FORMAT.format(LocalDateTime.ofInstant(instant, ZoneOffset.UTC));
    }

    private static Instant parseUtc(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value).toInstant();
        } catch (DateTimeParseException ignored) {
            // Fall through to the zone-less clist format.
        }
        try {
            return LocalDateTime.parse(value).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private static String stripTrailingSlash(String value) {
        return value.replaceAll("/+$", "");
    }

    @PreDestroy
    void shutdownExecutor() {
        startupExecutor.shutdownNow();
    }

    private record ContestDraft(
            String externalId,
            ContestPlatform platform,
            String title,
            Instant start,
            Instant end,
            long durationSeconds,
            String contestUrl
    ) {
    }

    public record SyncResult(int fetched, int inserted, int updated, boolean fallbackUsed) {
        @Override
        public String toString() {
            return "fetched=" + fetched + ", inserted=" + inserted + ", updated=" + updated
                    + ", fallback=" + fallbackUsed;
        }
    }
}

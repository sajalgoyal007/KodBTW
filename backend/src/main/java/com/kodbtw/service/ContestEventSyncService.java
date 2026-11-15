package com.kodbtw.service;

import com.kodbtw.contest.ContestDraft;
import com.kodbtw.contest.ContestFeed;
import com.kodbtw.entity.ContestEvent;
import com.kodbtw.repository.ContestEventRepository;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Keeps {@code contest_events} fresh for all supported platforms.
 *
 * <p>Every {@link ContestFeed} is independent: a failing provider is logged and skipped while
 * the remaining feeds still run, and previously stored contests are never deleted. Live
 * contests are de-duplicated across feeds by platform, start instant and duration, so a
 * contest reported by both clist.by and a direct provider is stored once.</p>
 */
@Service
public class ContestEventSyncService {

    private static final Logger log = LoggerFactory.getLogger(ContestEventSyncService.class);

    static final Duration PAST_WINDOW = Duration.ofDays(7);
    static final Duration FUTURE_WINDOW = Duration.ofDays(45);

    private final List<ContestFeed> feeds;
    private final ContestEventRepository repository;
    private final boolean syncEnabled;
    private final Clock clock;
    private final ExecutorService startupExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "contest-sync-startup");
        thread.setDaemon(true);
        return thread;
    });

    @Autowired
    public ContestEventSyncService(
            List<ContestFeed> feeds,
            ContestEventRepository repository,
            @Value("${app.contests.sync-enabled:true}") boolean syncEnabled) {
        this(feeds, repository, syncEnabled, Clock.systemUTC());
    }

    ContestEventSyncService(
            List<ContestFeed> feeds,
            ContestEventRepository repository,
            boolean syncEnabled,
            Clock clock) {
        this.feeds = feeds.stream()
                .sorted(Comparator.comparingInt(ContestFeed::priority)
                        .thenComparing(ContestFeed::source))
                .toList();
        this.repository = repository;
        this.syncEnabled = syncEnabled;
        this.clock = clock;
    }

    /** Safe, non-blocking refresh on startup so the calendar fills without waiting on third parties. */
    @EventListener(ApplicationReadyEvent.class)
    public void syncOnStartup() {
        if (!syncEnabled) {
            return;
        }
        startupExecutor.submit(() -> {
            try {
                log.info("Startup contest sync finished: {}", syncNow());
            } catch (RuntimeException exception) {
                log.warn("Startup contest sync failed: {}", exception.getMessage());
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

    /** Synchronously refreshes the contest window and reports what every feed did. */
    public SyncReport syncNow() {
        Instant now = clock.instant();
        Instant fromInclusive = now.minus(PAST_WINDOW);
        Instant toExclusive = now.plus(FUTURE_WINDOW);

        Map<String, ContestDraft> unique = new LinkedHashMap<>();
        List<FeedStatus> statuses = new ArrayList<>();

        for (ContestFeed feed : feeds) {
            if (!feed.isConfigured()) {
                statuses.add(new FeedStatus(feed.source(), FeedStatus.SKIPPED, 0, "not configured"));
                log.debug("Contest feed {} skipped: not configured", feed.source());
                continue;
            }
            try {
                List<ContestDraft> drafts = feed.fetch(fromInclusive, toExclusive);
                int accepted = 0;
                if (drafts != null) {
                    for (ContestDraft draft : drafts) {
                        if (draft != null && unique.putIfAbsent(draft.dedupeKey(), draft) == null) {
                            accepted++;
                        }
                    }
                }
                statuses.add(new FeedStatus(feed.source(), FeedStatus.OK, accepted, null));
            } catch (RuntimeException exception) {
                log.warn("Contest feed {} failed, keeping existing contests: {}",
                        feed.source(), exception.getMessage());
                statuses.add(new FeedStatus(feed.source(), FeedStatus.FAILED, 0,
                        exception.getClass().getSimpleName()));
            }
        }

        LocalDateTime syncedAt = LocalDateTime.ofInstant(now, ZoneOffset.UTC);
        int inserted = 0;
        int updated = 0;
        for (ContestDraft draft : unique.values()) {
            try {
                if (persist(draft, syncedAt)) {
                    inserted++;
                } else {
                    updated++;
                }
            } catch (RuntimeException exception) {
                log.warn("Could not persist contest {}: {}", draft.externalId(), exception.getMessage());
            }
        }

        return new SyncReport(unique.size(), inserted, updated, List.copyOf(statuses));
    }

    private boolean persist(ContestDraft draft, LocalDateTime syncedAt) {
        Optional<ContestEvent> byExternalId = repository.findByExternalId(draft.externalId());
        ContestEvent event;
        boolean created = false;
        if (byExternalId.isPresent()) {
            event = byExternalId.get();
        } else {
            Optional<ContestEvent> sameSlot = repository.findFirstByPlatformAndStartTimeUtcAndDurationSeconds(
                    draft.platform().name(),
                    LocalDateTime.ofInstant(draft.start(), ZoneOffset.UTC),
                    draft.durationSeconds());
            if (sameSlot.isPresent()) {
                event = sameSlot.get();
            } else {
                event = new ContestEvent();
                created = true;
            }
        }

        if (created) {
            event.setExternalId(draft.externalId());
        }
        event.setPlatform(draft.platform().name());
        event.setTitle(draft.title());
        event.setStartTimeUtc(LocalDateTime.ofInstant(draft.start(), ZoneOffset.UTC));
        event.setEndTimeUtc(LocalDateTime.ofInstant(draft.end(), ZoneOffset.UTC));
        event.setDurationSeconds(draft.durationSeconds());
        event.setContestUrl(draft.contestUrl());
        event.setLastSyncedAt(syncedAt);
        repository.save(event);
        return created;
    }

    @PreDestroy
    void shutdownExecutor() {
        startupExecutor.shutdownNow();
    }

    public record FeedStatus(String source, String status, int fetched, String error) {
        public static final String OK = "OK";
        public static final String FAILED = "FAILED";
        public static final String SKIPPED = "SKIPPED";
    }

    public record SyncReport(int fetched, int inserted, int updated, List<FeedStatus> feeds) {
        @Override
        public String toString() {
            return "fetched=" + fetched + ", inserted=" + inserted + ", updated=" + updated
                    + ", feeds=" + feeds;
        }
    }
}

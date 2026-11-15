package com.kodbtw.contest;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * One upstream source of upcoming/live/recent contest events.
 *
 * <p>Feeds are independent: a failure in one feed is caught by the sync service and never
 * prevents the others from running or removes previously stored contests.</p>
 */
public interface ContestFeed {

    /** Short, stable source name used in logs and sync results. */
    String source();

    /** Lower runs first. clist.by (0) is preferred over per-platform fallbacks (10+). */
    int priority();

    /** Platforms this feed can supply. */
    Set<ContestPlatform> platforms();

    /** False when required configuration (for example API credentials) is missing. */
    default boolean isConfigured() {
        return true;
    }

    /**
     * Fetches contests whose start instant falls in {@code [fromInclusive, toExclusive)}.
     * Implementations should return only complete drafts with safe HTTP(S) URLs.
     *
     * @throws RuntimeException when the upstream provider is unavailable or returns junk
     */
    List<ContestDraft> fetch(Instant fromInclusive, Instant toExclusive);
}

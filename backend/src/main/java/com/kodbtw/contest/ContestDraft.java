package com.kodbtw.contest;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Normalized contest ready to be persisted.
 *
 * <p>Instants are always UTC. {@code dedupeKey} lets the sync service collapse the same
 * contest reported by more than one feed (for example clist.by and the direct Codeforces API)
 * instead of storing duplicate rows.</p>
 */
public record ContestDraft(
        String externalId,
        ContestPlatform platform,
        String title,
        Instant start,
        Instant end,
        long durationSeconds,
        String contestUrl
) {

    public ContestDraft {
        Objects.requireNonNull(externalId, "externalId");
        Objects.requireNonNull(platform, "platform");
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        title = title == null ? "" : title.trim();
        contestUrl = contestUrl == null ? "" : contestUrl.trim();
        if (durationSeconds <= 0) {
            durationSeconds = Math.max(0L, Duration.between(start, end).getSeconds());
        }
        if (!end.isAfter(start) || durationSeconds <= 0) {
            throw new IllegalArgumentException("Contest draft must have a positive duration");
        }
    }

    /** Stable identity across feeds: platform plus start instant plus duration. */
    public String dedupeKey() {
        return platform.name() + "|" + start.getEpochSecond() + "|" + durationSeconds;
    }
}

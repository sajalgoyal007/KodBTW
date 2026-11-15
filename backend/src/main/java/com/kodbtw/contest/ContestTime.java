package com.kodbtw.contest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Timezone-safe parsing helpers shared by contest feeds. */
public final class ContestTime {

    private static final DateTimeFormatter CLIST_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter OFFSET_WITH_SPACE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssXXX");

    private ContestTime() {
    }

    /**
     * Parses a clist.by timestamp. clist returns zone-less UTC wall-clock values such as
     * {@code 2026-11-16T12:00:00}; an explicit offset is also accepted.
     */
    public static Instant parseClistUtc(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        try {
            return OffsetDateTime.parse(trimmed).toInstant();
        } catch (DateTimeParseException ignored) {
            // Fall through to the zone-less clist format.
        }
        try {
            return LocalDateTime.parse(trimmed, CLIST_TIME).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    /** Parses an ISO-8601 value with or without an offset; zone-less values are treated as UTC. */
    public static Instant parseIsoUtc(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        try {
            return OffsetDateTime.parse(trimmed).toInstant();
        } catch (DateTimeParseException ignored) {
            // Fall through to a zone-less ISO timestamp.
        }
        try {
            return LocalDateTime.parse(trimmed).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    /** Parses a timestamp such as {@code 2026-10-30 04:30:00+00:00}. */
    public static Instant parseOffsetWithSpace(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value.trim(), OFFSET_WITH_SPACE).toInstant();
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    public static Instant fromEpochSecond(Long epochSecond) {
        if (epochSecond == null || epochSecond <= 0) {
            return null;
        }
        try {
            return Instant.ofEpochSecond(epochSecond);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    public static boolean inWindow(Instant start, Instant fromInclusive, Instant toExclusive) {
        return start != null && !start.isBefore(fromInclusive) && start.isBefore(toExclusive);
    }

    /** clist.by expects UTC wall-clock timestamps without a trailing {@code Z}. */
    public static String formatClistUtc(Instant instant) {
        return CLIST_TIME.format(LocalDateTime.ofInstant(instant, ZoneOffset.UTC));
    }
}

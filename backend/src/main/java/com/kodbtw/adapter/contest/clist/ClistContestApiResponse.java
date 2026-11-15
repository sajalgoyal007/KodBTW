package com.kodbtw.adapter.contest.clist;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Minimal view of the clist.by API v4 contest list response.
 *
 * <p>In API v4 {@code resource} is a plain resource name string (for example
 * {@code codeforces.com}), not an object. Treating it as an object made every
 * authenticated sync fail to deserialize, which silently forced the calendar
 * back to the Codeforces-only fallback.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ClistContestApiResponse(
        List<ClistContestRecord> objects
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ClistContestRecord(
            Long id,
            String event,
            String href,
            String resource,
            String start,
            String end,
            Long duration
    ) {
    }
}

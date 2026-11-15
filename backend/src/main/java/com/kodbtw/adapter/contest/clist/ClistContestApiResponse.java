package com.kodbtw.adapter.contest.clist;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** Minimal view of the clist.by API v4 contest list response. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ClistContestApiResponse(
        List<ClistContestRecord> objects
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ClistContestRecord(
            Long id,
            String event,
            String href,
            ClistResource resource,
            String start,
            String end,
            Long duration
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ClistResource(
            String name
    ) {
    }
}

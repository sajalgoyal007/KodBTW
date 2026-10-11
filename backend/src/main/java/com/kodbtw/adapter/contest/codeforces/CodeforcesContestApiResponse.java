package com.kodbtw.adapter.contest.codeforces;

import java.util.List;

public record CodeforcesContestApiResponse(
        String status,
        String comment,
        List<CodeforcesContestRecord> result
) {
    public record CodeforcesContestRecord(
            Integer id,
            String name,
            String phase,
            Long durationSeconds,
            Long startTimeSeconds
    ) {}
}

package com.kodbtw.adapter.contest.feed;

import com.kodbtw.adapter.contest.codeforces.CodeforcesContestApiResponse;
import com.kodbtw.contest.ContestDraft;
import com.kodbtw.contest.ContestFeed;
import com.kodbtw.contest.ContestPlatform;
import com.kodbtw.contest.ContestTime;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Direct feed for the official Codeforces public API.
 *
 * <p>This is the authoritative source for Codeforces and also acts as a safety net when
 * clist.by is unavailable or rate limited.</p>
 */
@Component
public class CodeforcesContestFeed implements ContestFeed {

    public static final String SOURCE = "CODEFORCES_API";
    static final String API_URL = "https://codeforces.com/api/contest.list?gym=false";

    private final RestClient restClient;

    public CodeforcesContestFeed() {
        this(FeedHttp.client());
    }

    CodeforcesContestFeed(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public String source() {
        return SOURCE;
    }

    @Override
    public int priority() {
        return 10;
    }

    @Override
    public Set<ContestPlatform> platforms() {
        return EnumSet.of(ContestPlatform.CODEFORCES);
    }

    @Override
    public List<ContestDraft> fetch(Instant fromInclusive, Instant toExclusive) {
        CodeforcesContestApiResponse response = restClient.get()
                .uri(API_URL)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(CodeforcesContestApiResponse.class);

        if (response == null || !"OK".equals(response.status()) || response.result() == null) {
            throw new IllegalStateException("Codeforces contest API returned an unusable payload");
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
            if (!ContestTime.inWindow(start, fromInclusive, toExclusive)) {
                continue;
            }
            long duration = record.durationSeconds();
            drafts.add(new ContestDraft(
                    "CODEFORCES:" + record.id(),
                    ContestPlatform.CODEFORCES,
                    record.name().trim(),
                    start,
                    start.plusSeconds(duration),
                    duration,
                    "https://codeforces.com/contest/" + record.id()));
        }
        return drafts;
    }
}

package com.kodbtw.adapter.contest.feed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.kodbtw.contest.ContestDraft;
import com.kodbtw.contest.ContestFeed;
import com.kodbtw.contest.ContestPlatform;
import com.kodbtw.contest.ContestTime;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Direct feed for HackerRank's public upcoming-contest REST endpoint.
 *
 * <p>HackerRank reports epoch seconds, which are already UTC.</p>
 */
@Component
public class HackerRankContestFeed implements ContestFeed {

    public static final String SOURCE = "HACKERRANK_REST";
    static final String API_URL = "https://www.hackerrank.com/rest/contests/upcoming?limit=50&offset=0";

    private static final String USER_AGENT = "KodBTW/1.0 (+https://kodbtw.vercel.app)";

    private final RestClient restClient;

    public HackerRankContestFeed() {
        this(FeedHttp.client());
    }

    HackerRankContestFeed(RestClient restClient) {
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
        return EnumSet.of(ContestPlatform.HACKERRANK);
    }

    @Override
    public List<ContestDraft> fetch(Instant fromInclusive, Instant toExclusive) {
        HackerRankContestListResponse response = restClient.get()
                .uri(API_URL)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.USER_AGENT, USER_AGENT)
                .retrieve()
                .body(HackerRankContestListResponse.class);

        if (response == null) {
            throw new IllegalStateException("HackerRank contest API returned an empty payload");
        }

        List<ContestDraft> drafts = new ArrayList<>();
        if (response.models() == null) {
            return drafts;
        }
        for (HackerRankContestRecord record : response.models()) {
            if (record == null || record.id() == null
                    || record.name() == null || record.name().isBlank()
                    || record.slug() == null || record.slug().isBlank()) {
                continue;
            }
            Instant start = ContestTime.fromEpochSecond(record.epochStarttime());
            if (!ContestTime.inWindow(start, fromInclusive, toExclusive)) {
                continue;
            }
            Instant end = ContestTime.fromEpochSecond(record.epochEndtime());
            long duration = end != null && end.isAfter(start)
                    ? java.time.Duration.between(start, end).getSeconds()
                    : 0L;
            if (duration <= 0) {
                continue;
            }
            String slug = record.slug().trim();
            drafts.add(new ContestDraft(
                    "HACKERRANK:" + record.id(),
                    ContestPlatform.HACKERRANK,
                    record.name().trim(),
                    start,
                    end,
                    duration,
                    "https://www.hackerrank.com/contests/" + slug));
        }
        return drafts;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record HackerRankContestListResponse(List<HackerRankContestRecord> models) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    record HackerRankContestRecord(
            Long id,
            String name,
            String slug,
            Long epochStarttime,
            Long epochEndtime
    ) {
    }
}

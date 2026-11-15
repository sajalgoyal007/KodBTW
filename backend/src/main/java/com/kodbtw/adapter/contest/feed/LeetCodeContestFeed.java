package com.kodbtw.adapter.contest.feed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
import java.util.Map;
import java.util.Set;

/**
 * Direct feed for LeetCode's public GraphQL endpoint.
 *
 * <p>{@code allContests} returns epoch seconds for {@code startTime} and a duration in seconds,
 * so no timezone conversion is required beyond {@link Instant#ofEpochSecond(long)}.</p>
 */
@Component
public class LeetCodeContestFeed implements ContestFeed {

    public static final String SOURCE = "LEETCODE_GRAPHQL";
    static final String API_URL = "https://leetcode.com/graphql";
    static final String QUERY = "query allContests { allContests { title titleSlug startTime duration } }";

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Safari/537.36";

    private final RestClient restClient;

    public LeetCodeContestFeed() {
        this(FeedHttp.client());
    }

    LeetCodeContestFeed(RestClient restClient) {
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
        return EnumSet.of(ContestPlatform.LEETCODE);
    }

    @Override
    public List<ContestDraft> fetch(Instant fromInclusive, Instant toExclusive) {
        LeetCodeGraphQlResponse response = restClient.post()
                .uri(API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.USER_AGENT, USER_AGENT)
                .header(HttpHeaders.REFERER, "https://leetcode.com/contest/")
                .body(Map.of("query", QUERY))
                .retrieve()
                .body(LeetCodeGraphQlResponse.class);

        if (response == null || response.data() == null) {
            throw new IllegalStateException("LeetCode GraphQL returned an empty payload");
        }

        List<ContestDraft> drafts = new ArrayList<>();
        if (response.data().allContests() == null) {
            return drafts;
        }
        for (LeetCodeContestRecord record : response.data().allContests()) {
            if (record == null || record.title() == null || record.title().isBlank()
                    || record.titleSlug() == null || record.titleSlug().isBlank()
                    || record.duration() == null || record.duration() <= 0) {
                continue;
            }
            Instant start = ContestTime.fromEpochSecond(record.startTime());
            if (!ContestTime.inWindow(start, fromInclusive, toExclusive)) {
                continue;
            }
            long duration = record.duration();
            String slug = record.titleSlug().trim();
            drafts.add(new ContestDraft(
                    "LEETCODE:" + slug,
                    ContestPlatform.LEETCODE,
                    record.title().trim(),
                    start,
                    start.plusSeconds(duration),
                    duration,
                    "https://leetcode.com/contest/" + slug + "/"));
        }
        return drafts;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record LeetCodeGraphQlResponse(LeetCodeData data) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record LeetCodeData(List<LeetCodeContestRecord> allContests) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record LeetCodeContestRecord(
            String title,
            String titleSlug,
            Long startTime,
            Long duration
    ) {
    }
}

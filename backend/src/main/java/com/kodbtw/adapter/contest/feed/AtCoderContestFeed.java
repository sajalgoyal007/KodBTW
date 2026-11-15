package com.kodbtw.adapter.contest.feed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.kodbtw.contest.ContestDraft;
import com.kodbtw.contest.ContestFeed;
import com.kodbtw.contest.ContestPlatform;
import com.kodbtw.contest.ContestTime;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Direct feed for AtCoder using the community-maintained Kenkoooo contest index.
 *
 * <p>AtCoder itself has no public JSON API, so Kenkoooo's mirror is the closest stable
 * machine-readable source. Results are epoch-second based and therefore already UTC.</p>
 */
@Component
public class AtCoderContestFeed implements ContestFeed {

    public static final String SOURCE = "ATCODER_KENKOOOO";
    static final String API_URL = "https://kenkoooo.com/atcoder/resources/contests.json";

    private final RestClient restClient;

    public AtCoderContestFeed() {
        this(FeedHttp.client());
    }

    AtCoderContestFeed(RestClient restClient) {
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
        return EnumSet.of(ContestPlatform.ATCODER);
    }

    @Override
    public List<ContestDraft> fetch(Instant fromInclusive, Instant toExclusive) {
        List<AtCoderContestRecord> response = restClient.get()
                .uri(API_URL)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<List<AtCoderContestRecord>>() {
                });

        if (response == null) {
            throw new IllegalStateException("AtCoder contest index returned an empty payload");
        }

        List<ContestDraft> drafts = new ArrayList<>();
        for (AtCoderContestRecord record : response) {
            if (record == null || record.id() == null || record.id().isBlank()
                    || record.title() == null || record.title().isBlank()
                    || record.durationSecond() == null || record.durationSecond() <= 0) {
                continue;
            }
            Instant start = ContestTime.fromEpochSecond(record.startEpochSecond());
            if (!ContestTime.inWindow(start, fromInclusive, toExclusive)) {
                continue;
            }
            long duration = record.durationSecond();
            String id = record.id().trim();
            drafts.add(new ContestDraft(
                    "ATCODER:" + id,
                    ContestPlatform.ATCODER,
                    record.title().trim(),
                    start,
                    start.plusSeconds(duration),
                    duration,
                    "https://atcoder.jp/contests/" + id));
        }
        return drafts;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    record AtCoderContestRecord(
            String id,
            Long startEpochSecond,
            Long durationSecond,
            String title
    ) {
    }
}

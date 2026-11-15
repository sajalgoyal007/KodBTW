package com.kodbtw.adapter.contest.feed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.kodbtw.contest.ContestDraft;
import com.kodbtw.contest.ContestFeed;
import com.kodbtw.contest.ContestPlatform;
import com.kodbtw.contest.ContestTime;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Direct feed for CodeChef's public contest list.
 *
 * <p>CodeChef exposes ISO-8601 timestamps with a {@code +05:30} offset; they are converted to
 * UTC immediately so the rest of the pipeline only ever deals with instants.</p>
 */
@Component
public class CodeChefContestFeed implements ContestFeed {

    public static final String SOURCE = "CODECHEF_API";
    static final String API_URL = "https://www.codechef.com/api/list/contests/all";

    private final RestClient restClient;

    public CodeChefContestFeed() {
        this(FeedHttp.client());
    }

    CodeChefContestFeed(RestClient restClient) {
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
        return EnumSet.of(ContestPlatform.CODECHEF);
    }

    @Override
    public List<ContestDraft> fetch(Instant fromInclusive, Instant toExclusive) {
        CodeChefContestListResponse response = restClient.get()
                .uri(API_URL)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(CodeChefContestListResponse.class);

        if (response == null) {
            throw new IllegalStateException("CodeChef contest API returned an empty payload");
        }

        List<ContestDraft> drafts = new ArrayList<>();
        append(drafts, response.presentContests(), fromInclusive, toExclusive);
        append(drafts, response.futureContests(), fromInclusive, toExclusive);
        return drafts;
    }

    private static void append(List<ContestDraft> drafts, List<CodeChefContestRecord> records,
            Instant fromInclusive, Instant toExclusive) {
        if (records == null) {
            return;
        }
        for (CodeChefContestRecord record : records) {
            if (record == null || record.contestCode() == null || record.contestCode().isBlank()
                    || record.contestName() == null || record.contestName().isBlank()) {
                continue;
            }
            Instant start = ContestTime.parseIsoUtc(record.contestStartDateIso());
            if (!ContestTime.inWindow(start, fromInclusive, toExclusive)) {
                continue;
            }
            Instant end = ContestTime.parseIsoUtc(record.contestEndDateIso());
            long duration = end != null && end.isAfter(start)
                    ? Duration.between(start, end).getSeconds()
                    : parseMinutes(record.contestDuration());
            if (duration <= 0) {
                continue;
            }
            if (end == null || !end.isAfter(start)) {
                end = start.plusSeconds(duration);
            }
            String code = record.contestCode().trim();
            drafts.add(new ContestDraft(
                    "CODECHEF:" + code,
                    ContestPlatform.CODECHEF,
                    record.contestName().trim(),
                    start,
                    end,
                    duration,
                    "https://www.codechef.com/" + code));
        }
    }

    private static long parseMinutes(String value) {
        if (value == null || value.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(value.trim()) * 60L;
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    record CodeChefContestListResponse(
            List<CodeChefContestRecord> presentContests,
            List<CodeChefContestRecord> futureContests
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    record CodeChefContestRecord(
            String contestCode,
            String contestName,
            String contestStartDateIso,
            String contestEndDateIso,
            String contestDuration
    ) {
    }
}

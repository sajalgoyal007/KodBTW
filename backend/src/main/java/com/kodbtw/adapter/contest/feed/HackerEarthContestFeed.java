package com.kodbtw.adapter.contest.feed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.kodbtw.contest.ContestDraft;
import com.kodbtw.contest.ContestFeed;
import com.kodbtw.contest.ContestPlatform;
import com.kodbtw.contest.ContestTime;
import com.kodbtw.contest.ContestUrls;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Direct feed for HackerEarth's public events endpoint (the one its browser extension uses).
 *
 * <p>Timestamps are provided as {@code 2026-10-30 04:30:00+00:00}; parsing keeps the explicit
 * offset and normalizes to UTC.</p>
 */
@Component
public class HackerEarthContestFeed implements ContestFeed {

    public static final String SOURCE = "HACKEREARTH_EVENTS";
    static final String API_URL = "https://www.hackerearth.com/chrome-extension/events/";

    private static final String USER_AGENT = "KodBTW/1.0 (+https://kodbtw.vercel.app)";
    private static final int MAX_EXTERNAL_ID = 128;

    private final RestClient restClient;

    public HackerEarthContestFeed() {
        this(FeedHttp.client());
    }

    HackerEarthContestFeed(RestClient restClient) {
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
        return EnumSet.of(ContestPlatform.HACKEREARTH);
    }

    @Override
    public List<ContestDraft> fetch(Instant fromInclusive, Instant toExclusive) {
        HackerEarthEventResponse response = restClient.get()
                .uri(API_URL)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.USER_AGENT, USER_AGENT)
                .retrieve()
                .body(HackerEarthEventResponse.class);

        if (response == null) {
            throw new IllegalStateException("HackerEarth events API returned an empty payload");
        }

        List<ContestDraft> drafts = new ArrayList<>();
        if (response.response() == null) {
            return drafts;
        }
        for (HackerEarthEvent event : response.response()) {
            if (event == null || event.title() == null || event.title().isBlank()
                    || !ContestUrls.isSafeHttpUrl(event.url())) {
                continue;
            }
            Instant start = ContestTime.parseOffsetWithSpace(event.startUtcTz());
            if (!ContestTime.inWindow(start, fromInclusive, toExclusive)) {
                continue;
            }
            Instant end = ContestTime.parseOffsetWithSpace(event.endUtcTz());
            long duration = end != null && end.isAfter(start)
                    ? Duration.between(start, end).getSeconds()
                    : 0L;
            if (duration <= 0) {
                continue;
            }
            String url = event.url().trim();
            drafts.add(new ContestDraft(
                    externalId(url, start),
                    ContestPlatform.HACKEREARTH,
                    event.title().trim(),
                    start,
                    end,
                    duration,
                    url));
        }
        return drafts;
    }

    private static String externalId(String url, Instant start) {
        String slug = url;
        int slash = slug.lastIndexOf('/');
        if (slash >= 0 && slash < slug.length() - 1) {
            slug = slug.substring(slash + 1);
        }
        slug = slug.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9-]+", "-");
        String id = "HACKEREARTH:" + start.getEpochSecond() + ":" + slug;
        return id.length() <= MAX_EXTERNAL_ID ? id : id.substring(0, MAX_EXTERNAL_ID);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record HackerEarthEventResponse(List<HackerEarthEvent> response) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    record HackerEarthEvent(
            String title,
            String url,
            String startUtcTz,
            String endUtcTz
    ) {
    }
}

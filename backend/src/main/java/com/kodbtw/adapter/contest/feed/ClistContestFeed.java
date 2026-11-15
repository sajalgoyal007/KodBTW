package com.kodbtw.adapter.contest.feed;

import com.kodbtw.adapter.contest.clist.ClistContestApiResponse;
import com.kodbtw.contest.ContestDraft;
import com.kodbtw.contest.ContestFeed;
import com.kodbtw.contest.ContestPlatform;
import com.kodbtw.contest.ContestTime;
import com.kodbtw.contest.ContestUrls;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Primary multi-platform feed backed by the clist.by API v4.
 *
 * <p>A single request asks for every supported resource with {@code resource__in};
 * issuing one request per platform quickly trips clist's 429 rate limit. Results are
 * paged with {@code offset} until the provider returns a short page.</p>
 */
@Component
public class ClistContestFeed implements ContestFeed {

    public static final String SOURCE = "CLIST";
    static final int PAGE_SIZE = 100;
    static final int MAX_PAGES = 5;

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(15);

    private final RestClient restClient;
    private final String username;
    private final String apiKey;
    private final String baseUrl;

    @Autowired
    public ClistContestFeed(
            RestClient.Builder builder,
            @Value("${app.clist.username:}") String username,
            @Value("${app.clist.api-key:}") String apiKey,
            @Value("${app.clist.base-url:https://clist.by/api/v4}") String baseUrl) {
        this(createRestClient(builder), username, apiKey, baseUrl);
    }

    ClistContestFeed(RestClient restClient, String username, String apiKey, String baseUrl) {
        this.restClient = restClient;
        this.username = username == null ? "" : username.trim();
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.baseUrl = baseUrl == null || baseUrl.isBlank()
                ? "https://clist.by/api/v4"
                : baseUrl.trim().replaceAll("/+$", "");
    }

    private static RestClient createRestClient(RestClient.Builder builder) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT);
        factory.setReadTimeout(READ_TIMEOUT);
        return builder.requestFactory(factory).build();
    }

    @Override
    public String source() {
        return SOURCE;
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public Set<ContestPlatform> platforms() {
        return EnumSet.allOf(ContestPlatform.class);
    }

    @Override
    public boolean isConfigured() {
        return !username.isBlank() && !apiKey.isBlank();
    }

    @Override
    public List<ContestDraft> fetch(Instant fromInclusive, Instant toExclusive) {
        if (!isConfigured()) {
            return List.of();
        }
        List<ContestDraft> drafts = new ArrayList<>();
        for (int page = 0; page < MAX_PAGES; page++) {
            ClistContestApiResponse response = restClient.get()
                    .uri(buildUri(fromInclusive, toExclusive, page * PAGE_SIZE))
                    .header(HttpHeaders.AUTHORIZATION, "ApiKey " + username + ":" + apiKey)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(ClistContestApiResponse.class);

            if (response == null || response.objects() == null || response.objects().isEmpty()) {
                break;
            }
            for (ClistContestApiResponse.ClistContestRecord record : response.objects()) {
                ContestDraft draft = toDraft(record, fromInclusive, toExclusive);
                if (draft != null) {
                    drafts.add(draft);
                }
            }
            if (response.objects().size() < PAGE_SIZE) {
                break;
            }
        }
        return drafts;
    }

    private URI buildUri(Instant fromInclusive, Instant toExclusive, int offset) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/contest/")
                .queryParam("limit", PAGE_SIZE)
                .queryParam("offset", offset)
                .queryParam("resource__in", resources())
                .queryParam("start__gte", ContestTime.formatClistUtc(fromInclusive))
                .queryParam("start__lte", ContestTime.formatClistUtc(toExclusive))
                .queryParam("order_by", "start");
        return builder.build().toUri();
    }

    private String resources() {
        return EnumSet.allOf(ContestPlatform.class).stream()
                .map(ContestPlatform::clistResource)
                .collect(Collectors.joining(","));
    }

    private static ContestDraft toDraft(ClistContestApiResponse.ClistContestRecord record,
            Instant fromInclusive, Instant toExclusive) {
        if (record == null || record.id() == null) {
            return null;
        }
        ContestPlatform platform = ContestPlatform.fromResource(record.resource()).orElse(null);
        if (platform == null) {
            return null;
        }
        Instant start = ContestTime.parseClistUtc(record.start());
        if (!ContestTime.inWindow(start, fromInclusive, toExclusive)) {
            return null;
        }
        Instant end = ContestTime.parseClistUtc(record.end());
        long duration = record.duration() != null && record.duration() > 0
                ? record.duration()
                : (end != null ? Duration.between(start, end).getSeconds() : 0L);
        if (end == null) {
            end = start.plusSeconds(duration);
        }
        if (duration <= 0 || !end.isAfter(start)) {
            return null;
        }
        String title = record.event() == null ? "" : record.event().trim();
        if (title.isEmpty() || !ContestUrls.isSafeHttpUrl(record.href())) {
            return null;
        }
        return new ContestDraft(
                SOURCE + ":" + record.id(),
                platform,
                title,
                start,
                end,
                duration,
                record.href().trim());
    }
}

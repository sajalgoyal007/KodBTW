package com.kodbtw.adapter.hackerearth;

import com.kodbtw.adapter.hackerearth.dto.HackerEarthStats;
import com.kodbtw.adapter.support.ProfileMetricParser;
import com.kodbtw.exception.PlatformApiException;
import com.kodbtw.exception.ResourceNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

/**
 * Client for public HackerEarth profile pages ({@code https://www.hackerearth.com/@handle}).
 *
 * <p>HackerEarth does not publish a documented public statistics API. This client extracts a
 * small set of known numeric fields and fails safely when the profile is private, blocked, or
 * has changed shape.
 */
@Component
public class HackerEarthClientImpl implements HackerEarthClient {

    private static final String PROFILE_URL = "https://www.hackerearth.com/@{handle}";
    private static final String USER_AGENT = "KodBTW/1.0 (coding-profile statistics; contact via project repository)";
    private static final String HANDLE_PATTERN = "[A-Za-z0-9_.-]{1,50}";

    private final RestClient restClient;

    public HackerEarthClientImpl() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
                .build();
    }

    public HackerEarthClientImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public HackerEarthStats fetchStats(String username) {
        if (username == null || !username.trim().matches(HANDLE_PATTERN)) {
            throw new ResourceNotFoundException("HackerEarth handle is invalid");
        }
        String handle = username.trim();
        String body;
        try {
            body = restClient.get()
                    .uri(PROFILE_URL, handle)
                    .accept(MediaType.TEXT_HTML)
                    .retrieve()
                    .body(String.class);
        } catch (HttpStatusCodeException exception) {
            int status = exception.getStatusCode().value();
            if (status == 404) {
                throw new ResourceNotFoundException("HackerEarth profile not found");
            }
            if (status == 403) {
                throw new PlatformApiException("HackerEarth access was denied", exception);
            }
            if (status == 429) {
                throw new PlatformApiException("HackerEarth rate limit exceeded", exception);
            }
            if (status >= 500) {
                throw new PlatformApiException("HackerEarth returned an HTTP error", exception);
            }
            throw new PlatformApiException("HackerEarth returned an unexpected HTTP status", exception);
        } catch (RestClientException exception) {
            throw new PlatformApiException("Failed to communicate with HackerEarth", exception);
        }

        if (body == null || body.isBlank()) {
            throw new PlatformApiException("HackerEarth returned an empty profile page");
        }

        Integer points = ProfileMetricParser.findInteger(body,
                "points", "totalPoints", "rating");
        Integer totalSolved = ProfileMetricParser.findInteger(body,
                "solvedProblems", "problemsSolved", "totalSolved");
        Integer contests = ProfileMetricParser.findInteger(body,
                "contestsParticipated", "contestsCount", "contestCount");
        Integer badges = ProfileMetricParser.findInteger(body,
                "badgesCount", "badges", "totalBadges");

        if (points == null && totalSolved == null && contests == null && badges == null) {
            throw new PlatformApiException("HackerEarth profile page did not expose usable statistics");
        }

        return new HackerEarthStats(points, totalSolved, contests, badges);
    }
}

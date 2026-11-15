package com.kodbtw.adapter.code360;

import com.kodbtw.adapter.code360.dto.Code360Stats;
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
 * Client for Code360 (formerly CodeStudio) public profile pages under naukri.com/code360.
 *
 * <p>No documented public statistics API exists, so this client reads the public profile HTML
 * and extracts only a small set of known numeric fields. If the page markup changes, the sync
 * fails safely and the previously cached snapshot is retained.
 */
@Component
public class Code360ClientImpl implements Code360Client {

    private static final String PROFILE_URL = "https://www.naukri.com/code360/profile/{handle}";
    private static final String USER_AGENT = "KodBTW/1.0 (coding-profile statistics; contact via project repository)";
    private static final String HANDLE_PATTERN = "[A-Za-z0-9_.-]{1,50}";

    private final RestClient restClient;

    public Code360ClientImpl() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
                .build();
    }

    public Code360ClientImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Code360Stats fetchStats(String username) {
        if (username == null || !username.trim().matches(HANDLE_PATTERN)) {
            throw new ResourceNotFoundException("Code360 handle is invalid");
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
                throw new ResourceNotFoundException("Code360 profile not found");
            }
            if (status == 403) {
                throw new PlatformApiException("Code360 access was denied", exception);
            }
            if (status == 429) {
                throw new PlatformApiException("Code360 rate limit exceeded", exception);
            }
            if (status >= 500) {
                throw new PlatformApiException("Code360 returned an HTTP error", exception);
            }
            throw new PlatformApiException("Code360 returned an unexpected HTTP status", exception);
        } catch (RestClientException exception) {
            throw new PlatformApiException("Failed to communicate with Code360", exception);
        }

        if (body == null || body.isBlank()) {
            throw new PlatformApiException("Code360 returned an empty profile page");
        }

        Integer totalSolved = ProfileMetricParser.findInteger(body,
                "totalProblemsSolved", "problemsSolved", "totalSolved", "problemCount");
        Integer rating = ProfileMetricParser.findInteger(body,
                "currentRating", "rating", "contestRating");
        Integer contests = ProfileMetricParser.findInteger(body,
                "contestsAttended", "contestsParticipated", "contestCount", "contestsCount");
        Integer currentStreak = ProfileMetricParser.findInteger(body,
                "currentStreak", "streak");
        Integer longestStreak = ProfileMetricParser.findInteger(body,
                "longestStreak", "maxStreak");

        if (totalSolved == null && rating == null && contests == null
                && currentStreak == null && longestStreak == null) {
            throw new PlatformApiException("Code360 profile page did not expose usable statistics");
        }
        if (isNegative(totalSolved) || isNegative(rating) || isNegative(contests)
                || isNegative(currentStreak) || isNegative(longestStreak)) {
            throw new PlatformApiException("Code360 profile page returned invalid statistics");
        }

        return new Code360Stats(totalSolved, rating, contests, currentStreak, longestStreak);
    }

    private boolean isNegative(Integer value) {
        return value != null && value < 0;
    }
}

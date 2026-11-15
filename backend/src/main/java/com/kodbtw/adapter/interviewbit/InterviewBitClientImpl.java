package com.kodbtw.adapter.interviewbit;

import com.kodbtw.adapter.interviewbit.dto.InterviewBitStats;
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
 * Client for the public InterviewBit profile page.
 *
 * <p>The profile page embeds basic score, rank, streak, and solved counts. Parsing is limited to
 * a handful of well-known field names, with safe failure and cached-stat preservation on change.
 */
@Component
public class InterviewBitClientImpl implements InterviewBitClient {

    private static final String PROFILE_URL = "https://www.interviewbit.com/profile/{handle}/";
    private static final String USER_AGENT = "KodBTW/1.0 (coding-profile statistics; contact via project repository)";
    private static final String HANDLE_PATTERN = "[A-Za-z0-9_.-]{1,50}";

    private final RestClient restClient;

    public InterviewBitClientImpl() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
                .build();
    }

    public InterviewBitClientImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public InterviewBitStats fetchStats(String username) {
        if (username == null || !username.trim().matches(HANDLE_PATTERN)) {
            throw new ResourceNotFoundException("InterviewBit handle is invalid");
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
                throw new ResourceNotFoundException("InterviewBit profile not found");
            }
            if (status == 403) {
                throw new PlatformApiException("InterviewBit access was denied", exception);
            }
            if (status == 429) {
                throw new PlatformApiException("InterviewBit rate limit exceeded", exception);
            }
            if (status >= 500) {
                throw new PlatformApiException("InterviewBit returned an HTTP error", exception);
            }
            throw new PlatformApiException("InterviewBit returned an unexpected HTTP status", exception);
        } catch (RestClientException exception) {
            throw new PlatformApiException("Failed to communicate with InterviewBit", exception);
        }

        if (body == null || body.isBlank()) {
            throw new PlatformApiException("InterviewBit returned an empty profile page");
        }

        Integer score = ProfileMetricParser.findInteger(body,
                "score", "totalScore", "reputation");
        Integer rank = ProfileMetricParser.findInteger(body,
                "globalRank", "rank");
        Integer totalSolved = ProfileMetricParser.findInteger(body,
                "totalProblemsSolved", "solvedProblems", "problemsSolved");
        Integer currentStreak = ProfileMetricParser.findInteger(body,
                "currentStreak", "streak");

        if (score == null && rank == null && totalSolved == null && currentStreak == null) {
            throw new PlatformApiException("InterviewBit profile page did not expose usable statistics");
        }

        return new InterviewBitStats(score, rank, totalSolved, currentStreak);
    }
}

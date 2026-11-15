package com.kodbtw.adapter.hackerrank;

import com.fasterxml.jackson.databind.JsonNode;
import com.kodbtw.adapter.hackerrank.dto.HackerRankPublicStats;
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
 * Client for HackerRank's public, unauthenticated REST endpoints.
 *
 * <p>Uses:
 * <ul>
 *   <li>{@code /rest/contests/master/hackers/{handle}/profile} to confirm the profile exists,</li>
 *   <li>{@code /rest/hackers/{handle}/badges} for per-domain solved counts and stars,</li>
 *   <li>{@code /rest/hackers/{handle}/scores_elo} for per-track ELO scores.</li>
 * </ul>
 *
 * <p>No cookies, session state, or browser automation are used. Parsing is defensive: a verified
 * profile with optional badge or score payloads missing returns null metrics instead of invented
 * zeros, while transport and unexpected HTTP failures surface as {@link PlatformApiException} so
 * the sync layer preserves previously cached statistics.
 */
@Component
public class HackerRankClientImpl implements HackerRankClient {

    private static final String PROFILE_URL =
            "https://www.hackerrank.com/rest/contests/master/hackers/{handle}/profile";
    private static final String BADGES_URL =
            "https://www.hackerrank.com/rest/hackers/{handle}/badges";
    private static final String SCORES_URL =
            "https://www.hackerrank.com/rest/hackers/{handle}/scores_elo";
    private static final String USER_AGENT =
            "KodBTW/1.0 (coding-profile statistics; contact via project repository)";
    private static final String HANDLE_PATTERN = "[A-Za-z0-9_.-]{1,50}";

    private final RestClient restClient;

    public HackerRankClientImpl() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public HackerRankClientImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public HackerRankPublicStats fetchStats(String username) {
        if (username == null || !username.trim().matches(HANDLE_PATTERN)) {
            throw new ResourceNotFoundException("HackerRank username is invalid");
        }
        String handle = username.trim();

        verifyProfileExists(handle);
        JsonNode badges = getOptional(BADGES_URL, handle);
        JsonNode scores = getOptional(SCORES_URL, handle);

        Integer totalSolved = null;
        Integer badgeCount = null;
        Integer totalStars = null;
        JsonNode badgeModels = badges == null ? null : badges.path("models");
        if (badgeModels != null && badgeModels.isArray()) {
            int solvedSum = 0;
            int starsSum = 0;
            int count = 0;
            boolean foundSolved = false;
            boolean foundStars = false;
            for (JsonNode badge : badgeModels) {
                if (badge == null || !badge.isObject()) continue;
                count++;
                JsonNode solved = badge.get("solved");
                if (solved != null && solved.canConvertToInt() && solved.asInt() >= 0) {
                    solvedSum += solved.asInt();
                    foundSolved = true;
                }
                JsonNode stars = badge.get("stars");
                if (stars != null && stars.canConvertToInt() && stars.asInt() >= 0) {
                    starsSum += stars.asInt();
                    foundStars = true;
                }
            }
            badgeCount = count;
            totalSolved = foundSolved ? solvedSum : null;
            totalStars = foundStars ? starsSum : null;
        }

        Integer elo = extractElo(scores);

        return new HackerRankPublicStats(totalSolved, badgeCount, totalStars, elo, elo);
    }

    private void verifyProfileExists(String handle) {
        try {
            JsonNode profile = restClient.get()
                    .uri(PROFILE_URL, handle)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(JsonNode.class);
            if (profile == null || profile.path("model").isMissingNode() || profile.path("model").isNull()) {
                throw new ResourceNotFoundException("HackerRank profile not found");
            }
        } catch (HttpStatusCodeException exception) {
            int status = exception.getStatusCode().value();
            if (status == 404) {
                throw new ResourceNotFoundException("HackerRank profile not found");
            }
            throw translate(exception, status);
        } catch (RestClientException exception) {
            throw new PlatformApiException("Failed to communicate with HackerRank", exception);
        }
    }

    private JsonNode getOptional(String uri, String handle) {
        try {
            return restClient.get()
                    .uri(uri, handle)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (HttpStatusCodeException exception) {
            int status = exception.getStatusCode().value();
            if (status == 404) {
                // Thin profiles legitimately have no badges or scores.
                return null;
            }
            throw translate(exception, status);
        } catch (RestClientException exception) {
            throw new PlatformApiException("Failed to communicate with HackerRank", exception);
        }
    }

    private PlatformApiException translate(HttpStatusCodeException exception, int status) {
        if (status == 403) {
            return new PlatformApiException("HackerRank access was denied", exception);
        }
        if (status == 429) {
            return new PlatformApiException("HackerRank API rate limit exceeded", exception);
        }
        if (status >= 500) {
            return new PlatformApiException("HackerRank API returned an HTTP error", exception);
        }
        return new PlatformApiException("HackerRank API returned an unexpected HTTP status", exception);
    }

    /**
     * Derives a single ELO-style value from the per-track scores payload.
     *
     * <p>Contest scores are preferred because they represent rated contests. When no track has a
     * positive contest score, the highest practice score is used instead. Rounded values keep the
     * stored rating integral.
     */
    private Integer extractElo(JsonNode scores) {
        if (scores == null || !scores.isArray()) return null;

        Double bestContest = null;
        Double bestPractice = null;
        for (JsonNode track : scores) {
            if (track == null || !track.isObject()) continue;
            JsonNode contest = track.path("contest");
            if (contest.isObject()) {
                double score = numericValue(contest.get("score"));
                if (score > 0 && (bestContest == null || score > bestContest)) {
                    bestContest = score;
                }
            }
            JsonNode practice = track.path("practice");
            if (practice.isObject()) {
                double score = numericValue(practice.get("score"));
                if (score > 0 && (bestPractice == null || score > bestPractice)) {
                    bestPractice = score;
                }
            }
        }

        Double best = bestContest != null ? bestContest : bestPractice;
        if (best == null) return null;
        long rounded = Math.round(best);
        return rounded > Integer.MAX_VALUE ? null : (int) rounded;
    }

    private double numericValue(JsonNode node) {
        if (node == null || node.isNull()) return 0;
        if (node.isNumber()) return node.asDouble();
        if (node.isTextual()) {
            try {
                return Double.parseDouble(node.asText().trim());
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }
}

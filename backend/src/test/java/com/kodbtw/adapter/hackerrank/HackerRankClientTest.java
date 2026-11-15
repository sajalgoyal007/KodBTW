package com.kodbtw.adapter.hackerrank;

import com.kodbtw.adapter.hackerrank.dto.HackerRankPublicStats;
import com.kodbtw.exception.PlatformApiException;
import com.kodbtw.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class HackerRankClientTest {

    private static final String PROFILE_URL =
            "https://www.hackerrank.com/rest/contests/master/hackers/sample_user/profile";
    private static final String BADGES_URL =
            "https://www.hackerrank.com/rest/hackers/sample_user/badges";
    private static final String SCORES_URL =
            "https://www.hackerrank.com/rest/hackers/sample_user/scores_elo";

    private MockRestServiceServer mockServer;
    private HackerRankClientImpl client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new HackerRankClientImpl(builder.build());
    }

    @Test
    void aggregatesBadgeSolvesAndPrefersContestElo() {
        expectProfile();
        mockServer.expect(requestTo(BADGES_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"status":true,"version":2,"models":[
                          {"badge_name":"Problem Solving","solved":29,"stars":3},
                          {"badge_name":"C++","solved":16,"stars":5}
                        ]}
                        """, MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo(SCORES_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        [
                          {"name":"Algorithms","practice":{"score":295.6},"contest":{"score":1600.4}},
                          {"name":"C++","practice":{"score":900.0},"contest":{"score":0}}
                        ]
                        """, MediaType.APPLICATION_JSON));

        HackerRankPublicStats stats = client.fetchStats("sample_user");

        assertEquals(45, stats.totalSolved());
        assertEquals(2, stats.badgeCount());
        assertEquals(8, stats.totalStars());
        assertEquals(1600, stats.rating());
        assertEquals(1600, stats.maxRating());
        mockServer.verify();
    }

    @Test
    void fallsBackToHighestPracticeScoreWhenNoContestScoreExists() {
        expectProfile();
        mockServer.expect(requestTo(BADGES_URL))
                .andRespond(withSuccess("""
                        {"status":true,"models":[{"solved":5,"stars":1}]}
                        """, MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo(SCORES_URL))
                .andRespond(withSuccess("""
                        [
                          {"practice":{"score":320.2},"contest":{"score":0}},
                          {"practice":{"score":780.8},"contest":{"score":0}}
                        ]
                        """, MediaType.APPLICATION_JSON));

        HackerRankPublicStats stats = client.fetchStats("sample_user");

        assertEquals(5, stats.totalSolved());
        assertEquals(781, stats.rating());
        assertEquals(781, stats.maxRating());
        mockServer.verify();
    }

    @Test
    void toleratesMissingBadgeAndScorePayloadsForThinProfiles() {
        expectProfile();
        mockServer.expect(requestTo(BADGES_URL)).andRespond(withStatus(HttpStatus.NOT_FOUND));
        mockServer.expect(requestTo(SCORES_URL)).andRespond(withStatus(HttpStatus.NOT_FOUND));

        HackerRankPublicStats stats = client.fetchStats("sample_user");

        assertNull(stats.totalSolved());
        assertNull(stats.badgeCount());
        assertNull(stats.totalStars());
        assertNull(stats.rating());
        assertNull(stats.maxRating());
        mockServer.verify();
    }

    @Test
    void emptyBadgeModelsIsAValidProfileWithoutInventedSolvedCounts() {
        expectProfile();
        mockServer.expect(requestTo(BADGES_URL))
                .andRespond(withSuccess("{\"status\":true,\"models\":[],\"version\":2}", MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo(SCORES_URL))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        HackerRankPublicStats stats = client.fetchStats("sample_user");

        assertEquals(0, stats.badgeCount());
        assertNull(stats.totalSolved());
        assertNull(stats.totalStars());
        assertNull(stats.rating());
        mockServer.verify();
    }

    @Test
    void missingProfileModelIsReportedAsMissingProfile() {
        mockServer.expect(requestTo(PROFILE_URL))
                .andRespond(withSuccess("{\"status\":true}", MediaType.APPLICATION_JSON));

        assertThrows(ResourceNotFoundException.class, () -> client.fetchStats("sample_user"));
        mockServer.verify();
    }

    @Test
    void mapsNotFoundToMissingProfile() {
        mockServer.expect(requestTo(
                        "https://www.hackerrank.com/rest/contests/master/hackers/missing/profile"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThrows(ResourceNotFoundException.class, () -> client.fetchStats("missing"));
        mockServer.verify();
    }

    @Test
    void mapsRateLimitToSafeFailure() {
        mockServer.expect(requestTo(PROFILE_URL))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS).body("provider detail must not leak"));

        PlatformApiException exception = assertThrows(PlatformApiException.class,
                () -> client.fetchStats("sample_user"));

        assertEquals("HackerRank API rate limit exceeded", exception.getMessage());
        mockServer.verify();
    }

    @Test
    void mapsServerErrorsToSafeFailure() {
        mockServer.expect(requestTo(PROFILE_URL)).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        PlatformApiException exception = assertThrows(PlatformApiException.class,
                () -> client.fetchStats("sample_user"));

        assertEquals("HackerRank API returned an HTTP error", exception.getMessage());
        mockServer.verify();
    }

    @Test
    void rejectsMalformedOptionalJsonWithoutSurfacingThePayload() {
        expectProfile();
        mockServer.expect(requestTo(BADGES_URL))
                .andRespond(withSuccess("{not-json", MediaType.APPLICATION_JSON));

        PlatformApiException exception = assertThrows(PlatformApiException.class,
                () -> client.fetchStats("sample_user"));

        assertEquals("Failed to communicate with HackerRank", exception.getMessage());
        mockServer.verify();
    }

    @Test
    void rejectsUnsafeHandleWithoutMakingAnyRequest() {
        assertThrows(ResourceNotFoundException.class, () -> client.fetchStats("../someone"));
        mockServer.verify();
    }

    private void expectProfile() {
        mockServer.expect(requestTo(PROFILE_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"model\":{\"username\":\"sample_user\"}}", MediaType.APPLICATION_JSON));
    }
}

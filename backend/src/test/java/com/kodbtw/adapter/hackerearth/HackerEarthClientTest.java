package com.kodbtw.adapter.hackerearth;

import com.kodbtw.adapter.hackerearth.dto.HackerEarthStats;
import com.kodbtw.exception.PlatformApiException;
import com.kodbtw.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class HackerEarthClientTest {

    private static final String PROFILE_URL = "https://www.hackerearth.com/@sample_user";

    private MockRestServiceServer mockServer;
    private HackerEarthClientImpl client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new HackerEarthClientImpl(builder.build());
    }

    @Test
    void extractsPointsSolvedContestsAndBadges() {
        mockServer.expect(requestTo(PROFILE_URL)).andRespond(withSuccess("""
                <html><body>
                  <script>window.profile = {
                    "points": 320,
                    "solvedProblems": 47,
                    "contestsParticipated": 6,
                    "badgesCount": 3
                  };</script>
                </body></html>
                """, MediaType.TEXT_HTML));

        HackerEarthStats stats = client.fetchStats("sample_user");

        assertEquals(320, stats.points());
        assertEquals(47, stats.totalSolved());
        assertEquals(6, stats.contestsParticipated());
        assertEquals(3, stats.badges());
        mockServer.verify();
    }

    @Test
    void missingFieldsStayNullWhenAtLeastOneMetricExists() {
        mockServer.expect(requestTo(PROFILE_URL)).andRespond(withSuccess("""
                <html><body><span data-rating="1800"></span></body></html>
                """, MediaType.TEXT_HTML));

        HackerEarthStats stats = client.fetchStats("sample_user");

        assertEquals(1800, stats.points());
        assertNull(stats.totalSolved());
        assertNull(stats.contestsParticipated());
        assertNull(stats.badges());
        mockServer.verify();
    }

    @Test
    void rejectsPagesWithoutKnownMetrics() {
        mockServer.expect(requestTo(PROFILE_URL))
                .andRespond(withSuccess("<html><body>nothing here</body></html>", MediaType.TEXT_HTML));

        assertThrows(PlatformApiException.class, () -> client.fetchStats("sample_user"));
        mockServer.verify();
    }

    @Test
    void mapsNotFoundToMissingProfile() {
        mockServer.expect(requestTo("https://www.hackerearth.com/@missing"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThrows(ResourceNotFoundException.class, () -> client.fetchStats("missing"));
        mockServer.verify();
    }

    @Test
    void mapsRateLimitAndServerErrorsToSafeFailures() {
        mockServer.expect(requestTo(PROFILE_URL))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS).body("upstream detail"));
        PlatformApiException rateLimited = assertThrows(PlatformApiException.class,
                () -> client.fetchStats("sample_user"));
        assertEquals("HackerEarth rate limit exceeded", rateLimited.getMessage());
        mockServer.verify();

        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer secondServer = MockRestServiceServer.bindTo(builder).build();
        HackerEarthClientImpl secondClient = new HackerEarthClientImpl(builder.build());
        secondServer.expect(requestTo(PROFILE_URL)).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        PlatformApiException unavailable = assertThrows(PlatformApiException.class,
                () -> secondClient.fetchStats("sample_user"));
        assertEquals("HackerEarth returned an HTTP error", unavailable.getMessage());
        secondServer.verify();
    }

    @Test
    void rejectsUnsafeHandleWithoutMakingAnyRequest() {
        assertThrows(ResourceNotFoundException.class, () -> client.fetchStats("../someone"));
        mockServer.verify();
    }
}

package com.kodbtw.adapter.code360;

import com.kodbtw.adapter.code360.dto.Code360Stats;
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

class Code360ClientTest {

    private static final String PROFILE_URL = "https://www.naukri.com/code360/profile/sample_user";

    private MockRestServiceServer mockServer;
    private Code360ClientImpl client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new Code360ClientImpl(builder.build());
    }

    @Test
    void extractsKnownNumericFieldsFromThePublicProfilePage() {
        mockServer.expect(requestTo(PROFILE_URL)).andRespond(withSuccess("""
                <html><body>
                  <script>window.__DATA__ = {
                    "totalProblemsSolved": 140,
                    "currentRating": 1620,
                    "contestsAttended": 9,
                    "currentStreak": 4,
                    "longestStreak": 18
                  };</script>
                </body></html>
                """, MediaType.TEXT_HTML));

        Code360Stats stats = client.fetchStats("sample_user");

        assertEquals(140, stats.totalSolved());
        assertEquals(1620, stats.rating());
        assertEquals(9, stats.contestsParticipated());
        assertEquals(4, stats.currentStreak());
        assertEquals(18, stats.longestStreak());
        mockServer.verify();
    }

    @Test
    void missingFieldsStayNullWhenAtLeastOneMetricExists() {
        mockServer.expect(requestTo(PROFILE_URL)).andRespond(withSuccess("""
                <html><body><div data-problemCount="12"></div></body></html>
                """, MediaType.TEXT_HTML));

        Code360Stats stats = client.fetchStats("sample_user");

        assertEquals(12, stats.totalSolved());
        assertNull(stats.rating());
        assertNull(stats.contestsParticipated());
        assertNull(stats.currentStreak());
        assertNull(stats.longestStreak());
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
        mockServer.expect(requestTo("https://www.naukri.com/code360/profile/missing"))
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
        assertEquals("Code360 rate limit exceeded", rateLimited.getMessage());
        mockServer.verify();

        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer secondServer = MockRestServiceServer.bindTo(builder).build();
        Code360ClientImpl secondClient = new Code360ClientImpl(builder.build());
        secondServer.expect(requestTo(PROFILE_URL)).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        PlatformApiException unavailable = assertThrows(PlatformApiException.class,
                () -> secondClient.fetchStats("sample_user"));
        assertEquals("Code360 returned an HTTP error", unavailable.getMessage());
        secondServer.verify();
    }

    @Test
    void rejectsUnsafeHandleWithoutMakingAnyRequest() {
        assertThrows(ResourceNotFoundException.class, () -> client.fetchStats("../someone"));
        mockServer.verify();
    }
}

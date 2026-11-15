package com.kodbtw.adapter.interviewbit;

import com.kodbtw.adapter.interviewbit.dto.InterviewBitStats;
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

class InterviewBitClientTest {

    private static final String PROFILE_URL = "https://www.interviewbit.com/profile/sample_user/";

    private MockRestServiceServer mockServer;
    private InterviewBitClientImpl client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new InterviewBitClientImpl(builder.build());
    }

    @Test
    void extractsScoreRankSolvedAndStreak() {
        mockServer.expect(requestTo(PROFILE_URL)).andRespond(withSuccess("""
                <html><body>
                  <script>window.profile = {
                    "totalScore": 250,
                    "globalRank": 99,
                    "solvedProblems": 300,
                    "currentStreak": 10
                  };</script>
                </body></html>
                """, MediaType.TEXT_HTML));

        InterviewBitStats stats = client.fetchStats("sample_user");

        assertEquals(250, stats.score());
        assertEquals(99, stats.rank());
        assertEquals(300, stats.totalSolved());
        assertEquals(10, stats.currentStreak());
        mockServer.verify();
    }

    @Test
    void missingFieldsStayNullWhenAtLeastOneMetricExists() {
        mockServer.expect(requestTo(PROFILE_URL)).andRespond(withSuccess("""
                <html><body><span data-score="42"></span></body></html>
                """, MediaType.TEXT_HTML));

        InterviewBitStats stats = client.fetchStats("sample_user");

        assertEquals(42, stats.score());
        assertNull(stats.rank());
        assertNull(stats.totalSolved());
        assertNull(stats.currentStreak());
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
        mockServer.expect(requestTo("https://www.interviewbit.com/profile/missing/"))
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
        assertEquals("InterviewBit rate limit exceeded", rateLimited.getMessage());
        mockServer.verify();

        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer secondServer = MockRestServiceServer.bindTo(builder).build();
        InterviewBitClientImpl secondClient = new InterviewBitClientImpl(builder.build());
        secondServer.expect(requestTo(PROFILE_URL)).andRespond(withStatus(HttpStatus.BAD_GATEWAY));
        PlatformApiException unavailable = assertThrows(PlatformApiException.class,
                () -> secondClient.fetchStats("sample_user"));
        assertEquals("InterviewBit returned an HTTP error", unavailable.getMessage());
        secondServer.verify();
    }

    @Test
    void rejectsUnsafeHandleWithoutMakingAnyRequest() {
        assertThrows(ResourceNotFoundException.class, () -> client.fetchStats("../someone"));
        mockServer.verify();
    }
}

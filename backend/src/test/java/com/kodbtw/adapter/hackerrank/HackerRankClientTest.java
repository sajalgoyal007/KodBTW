package com.kodbtw.adapter.hackerrank;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodbtw.adapter.hackerrank.dto.HackerRankStatsResponse;
import com.kodbtw.exception.PlatformApiException;
import com.kodbtw.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.SocketTimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class HackerRankClientTest {

    private static final String REQUEST_URL = "https://hackerrank-stats.tashif.codes/sample_user/stats";
    private MockRestServiceServer mockServer;
    private HackerRankClientImpl client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new HackerRankClientImpl(builder.build());
    }

    @Test
    void parsesDocumentedFixtureAndAcceptsOnlyTheDocumentedPlatformEnvelope() throws IOException {
        String fixture = new String(new ClassPathResource("fixtures/hackerrank-stats-success.json")
                .getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        mockServer.expect(requestTo(REQUEST_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(fixture, MediaType.APPLICATION_JSON));

        HackerRankStatsResponse response = client.fetchStats("sample_user");

        assertEquals("success", response.status());
        assertEquals("hackerrank", response.platform());
        assertEquals(8, response.data().totalSolved());
        mockServer.verify();
    }

    @Test
    void missingOptionalDataStaysNullButMissingSolvedCountIsRejected() {
        mockServer.expect(requestTo(REQUEST_URL)).andRespond(withSuccess("""
                {"status":"success","platform":"hackerrank","username":"sample_user",
                 "data":{"totalSolved":4}}
                """, MediaType.APPLICATION_JSON));
        assertEquals(4, client.fetchStats("sample_user").data().totalSolved());
        mockServer.verify();

        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer secondServer = MockRestServiceServer.bindTo(builder).build();
        HackerRankClientImpl secondClient = new HackerRankClientImpl(builder.build());
        secondServer.expect(requestTo(REQUEST_URL)).andRespond(withSuccess("""
                {"status":"success","platform":"hackerrank","username":"sample_user",
                 "data":{"submissionCalendar":{}}}
                """, MediaType.APPLICATION_JSON));
        assertThrows(PlatformApiException.class, () -> secondClient.fetchStats("sample_user"));
        secondServer.verify();
    }

    @Test
    void rejectsWrongUsernameOrPlatformEnvelope() {
        mockServer.expect(requestTo(REQUEST_URL)).andRespond(withSuccess(response("other", "sample_user"), MediaType.APPLICATION_JSON));
        assertThrows(PlatformApiException.class, () -> client.fetchStats("sample_user"));
        mockServer.verify();

        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer secondServer = MockRestServiceServer.bindTo(builder).build();
        HackerRankClientImpl secondClient = new HackerRankClientImpl(builder.build());
        secondServer.expect(requestTo(REQUEST_URL)).andRespond(withSuccess(response("hackerrank", "different"), MediaType.APPLICATION_JSON));
        assertThrows(PlatformApiException.class, () -> secondClient.fetchStats("sample_user"));
        secondServer.verify();
    }

    @Test
    void mapsNotFoundToMissingProfile() {
        mockServer.expect(requestTo("https://hackerrank-stats.tashif.codes/missing/stats"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));
        assertThrows(ResourceNotFoundException.class, () -> client.fetchStats("missing"));
        mockServer.verify();
    }

    @Test
    void mapsRateLimitToSafeFailure() {
        mockServer.expect(requestTo(REQUEST_URL))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS).body("provider detail"));
        PlatformApiException exception = assertThrows(PlatformApiException.class,
                () -> client.fetchStats("sample_user"));
        assertEquals("HackerRank stats provider rate limit exceeded", exception.getMessage());
        mockServer.verify();
    }

    @Test
    void mapsServerErrorsToSafeFailure() {
        mockServer.expect(requestTo(REQUEST_URL)).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        PlatformApiException exception = assertThrows(PlatformApiException.class,
                () -> client.fetchStats("sample_user"));
        assertEquals("HackerRank stats provider returned an HTTP error", exception.getMessage());
        mockServer.verify();
    }

    @Test
    void rejectsMalformedJson() {
        mockServer.expect(requestTo(REQUEST_URL))
                .andRespond(withSuccess("{malformed", MediaType.APPLICATION_JSON));
        assertThrows(PlatformApiException.class, () -> client.fetchStats("sample_user"));
        mockServer.verify();
    }

    @Test
    void mapsTimeoutCauseToSafeFailure() {
        mockServer.expect(requestTo(REQUEST_URL)).andRespond(request -> {
            throw new java.io.IOException("simulated timeout", new SocketTimeoutException("timeout"));
        });
        PlatformApiException exception = assertThrows(PlatformApiException.class,
                () -> client.fetchStats("sample_user"));
        org.junit.jupiter.api.Assertions.assertTrue(hasTimeoutCause(exception));
        mockServer.verify();
    }

    @Test
    void emptyActivityHistoryDoesNotBecomeZeroActivityOrSolvedCount() {
        mockServer.expect(requestTo(REQUEST_URL)).andRespond(withSuccess("""
                {"status":"success","platform":"hackerrank","username":"sample_user",
                 "data":{"submissionCalendar":{}}}
                """, MediaType.APPLICATION_JSON));
        assertThrows(PlatformApiException.class, () -> client.fetchStats("sample_user"));
        mockServer.verify();
    }

    private boolean hasTimeoutCause(Throwable throwable) {
        for (Throwable current = throwable; current != null; current = current.getCause()) {
            if (current instanceof SocketTimeoutException) return true;
        }
        return false;
    }

    private String response(String platform, String username) {
        return """
                {"status":"success","platform":"%s","username":"%s","data":{"totalSolved":8}}
                """.formatted(platform, username);
    }
}

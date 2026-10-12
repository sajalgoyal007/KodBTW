package com.kodbtw.adapter.geeksforgeeks;

import com.kodbtw.exception.PlatformApiException;
import com.kodbtw.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GeeksForGeeksClientTest {

    private MockRestServiceServer mockServer;
    private GeeksForGeeksClientImpl client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new GeeksForGeeksClientImpl(builder.build());
    }

    @Test
    void parsesDocumentedStatsEnvelopeAndGfgPlatformName() {
        mockServer.expect(requestTo("https://gfg-stats.tashif.codes/sample_user/stats"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"status":"success","message":"retrieved","platform":"gfg",
                         "username":"sample_user","cached":false,"data":{
                           "totalSolved":126,"totalQuestions":null,"acceptanceRate":null,
                           "byDifficulty":{"school":8,"basic":13,"easy":40,"medium":50,"hard":15},
                           "topicAnalysis":[]}}
                        """, MediaType.APPLICATION_JSON));

        var response = client.fetchStats("sample_user");

        assertEquals("success", response.status());
        assertEquals("gfg", response.platform());
        assertEquals("sample_user", response.username());
        assertEquals(126, response.data().totalSolved());
        assertEquals(40, response.data().byDifficulty().easy());
        mockServer.verify();
    }

    @Test
    void acceptsOnlyTheTwoDocumentedPlatformEnvelopeSpellings() {
        mockServer.expect(requestTo("https://gfg-stats.tashif.codes/sample_user/stats"))
                .andRespond(withSuccess(validResponse("GeeksForGeeks", "sample_user"), MediaType.APPLICATION_JSON));

        assertEquals("GeeksForGeeks", client.fetchStats("sample_user").platform());
        mockServer.verify();
    }

    @Test
    void acceptsMissingOptionalMetricsWhenAtLeastOneMappedMetricExists() {
        mockServer.expect(requestTo("https://gfg-stats.tashif.codes/sample_user/stats"))
                .andRespond(withSuccess("""
                        {"status":"success","platform":"gfg","username":"sample_user","data":{
                          "totalSolved":20,"byDifficulty":{"easy":null,"medium":7,"hard":null}}}
                        """, MediaType.APPLICATION_JSON));

        var response = client.fetchStats("sample_user");

        assertEquals(20, response.data().totalSolved());
        assertEquals(7, response.data().byDifficulty().medium());
        mockServer.verify();
    }

    @Test
    void rejectsResponseWithNoMappedMetrics() {
        mockServer.expect(requestTo("https://gfg-stats.tashif.codes/sample_user/stats"))
                .andRespond(withSuccess("""
                        {"status":"success","platform":"gfg","username":"sample_user",
                         "data":{"totalSolved":null,"byDifficulty":{"easy":null,"medium":null,"hard":null}}}
                        """, MediaType.APPLICATION_JSON));

        assertThrows(PlatformApiException.class, () -> client.fetchStats("sample_user"));
        mockServer.verify();
    }

    @Test
    void rejectsWrongUsernameAndUnknownPlatformNames() {
        mockServer.expect(requestTo("https://gfg-stats.tashif.codes/sample_user/stats"))
                .andRespond(withSuccess(validResponse("gfg", "different_user"), MediaType.APPLICATION_JSON));
        assertThrows(PlatformApiException.class, () -> client.fetchStats("sample_user"));
        mockServer.verify();

        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer secondServer = MockRestServiceServer.bindTo(builder).build();
        GeeksForGeeksClientImpl secondClient = new GeeksForGeeksClientImpl(builder.build());
        secondServer.expect(requestTo("https://gfg-stats.tashif.codes/sample_user/stats"))
                .andRespond(withSuccess(validResponse("other-platform", "sample_user"), MediaType.APPLICATION_JSON));
        assertThrows(PlatformApiException.class, () -> secondClient.fetchStats("sample_user"));
        secondServer.verify();
    }

    @Test
    void mapsHttpNotFoundToMissingProfile() {
        mockServer.expect(requestTo("https://gfg-stats.tashif.codes/missing/stats"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThrows(ResourceNotFoundException.class, () -> client.fetchStats("missing"));
        mockServer.verify();
    }

    @Test
    void mapsHttpRateLimitToSafeFailure() {
        mockServer.expect(requestTo("https://gfg-stats.tashif.codes/sample_user/stats"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS).body("provider detail must not leak"));

        PlatformApiException exception = assertThrows(PlatformApiException.class,
                () -> client.fetchStats("sample_user"));

        assertEquals("GeeksforGeeks stats provider rate limit exceeded", exception.getMessage());
        mockServer.verify();
    }

    @Test
    void mapsServerAndGatewayTimeoutStatusesToSafeFailures() {
        mockServer.expect(requestTo("https://gfg-stats.tashif.codes/sample_user/stats"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        PlatformApiException unavailable = assertThrows(PlatformApiException.class,
                () -> client.fetchStats("sample_user"));
        assertEquals("GeeksforGeeks stats provider returned an HTTP error", unavailable.getMessage());
        mockServer.verify();

        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer secondServer = MockRestServiceServer.bindTo(builder).build();
        GeeksForGeeksClientImpl secondClient = new GeeksForGeeksClientImpl(builder.build());
        secondServer.expect(requestTo("https://gfg-stats.tashif.codes/sample_user/stats"))
                .andRespond(withStatus(HttpStatus.GATEWAY_TIMEOUT));
        PlatformApiException timeoutStatus = assertThrows(PlatformApiException.class,
                () -> secondClient.fetchStats("sample_user"));
        assertEquals("GeeksforGeeks stats provider timed out", timeoutStatus.getMessage());
        secondServer.verify();
    }

    @Test
    void rejectsMalformedJsonWithoutSurfacingProviderBody() {
        mockServer.expect(requestTo("https://gfg-stats.tashif.codes/sample_user/stats"))
                .andRespond(withSuccess("{not-json", MediaType.APPLICATION_JSON));

        PlatformApiException exception = assertThrows(PlatformApiException.class,
                () -> client.fetchStats("sample_user"));

        org.junit.jupiter.api.Assertions.assertTrue(exception.getMessage().startsWith("Failed to communicate"));
        mockServer.verify();
    }

    @Test
    void preservesTimeoutCauseForSyncFailureClassification() {
        mockServer.expect(requestTo("https://gfg-stats.tashif.codes/sample_user/stats"))
                .andRespond(request -> {
                    throw new ResourceAccessException("Read timed out", new SocketTimeoutException("Read timed out"));
                });

        PlatformApiException exception = assertThrows(PlatformApiException.class,
                () -> client.fetchStats("sample_user"));

        Throwable cause = exception;
        while (cause != null && !(cause instanceof SocketTimeoutException)) cause = cause.getCause();
        org.junit.jupiter.api.Assertions.assertNotNull(cause);
        mockServer.verify();
    }

    @Test
    void rejectsUnsafeUsernameWithoutMakingRequest() {
        assertThrows(ResourceNotFoundException.class, () -> client.fetchStats("../someone"));
        mockServer.verify();
    }

    private String validResponse(String platform, String username) {
        return """
                {"status":"success","platform":"%s","username":"%s","data":{
                  "totalSolved":10,"byDifficulty":{"easy":2,"medium":5,"hard":3}}}
                """.formatted(platform, username);
    }
}

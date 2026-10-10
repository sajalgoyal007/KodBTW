package com.kodbtw.adapter.codechef;

import com.kodbtw.exception.PlatformApiException;
import com.kodbtw.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.ResourceAccessException;

import java.net.SocketTimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CodeChefClientTest {
    private MockRestServiceServer mockServer;
    private CodeChefClientImpl client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new CodeChefClientImpl(builder.build());
    }

    @Test
    void parsesUsableProfileResponse() {
        mockServer.expect(requestTo("https://codechef-stats.tashif.codes/profile/chef_user"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"success":true,"status":200,"handle":"chef_user","profile":{
                          "currentRating":1247,"highestRating":1519,"globalRank":76970,"totalSolved":656}}
                        """, MediaType.APPLICATION_JSON));

        var response = client.fetchProfile("chef_user");

        assertEquals(Boolean.TRUE, response.success());
        assertEquals(200, response.status());
        assertEquals("chef_user", response.handle());
        assertEquals(1247, response.profile().currentRating());
        assertEquals(1519, response.profile().highestRating());
        assertEquals(76970, response.profile().globalRank());
        assertEquals(656, response.profile().totalSolved());
        mockServer.verify();
    }

    @Test
    void mapsHttpNotFoundToMissingProfile() {
        mockServer.expect(requestTo("https://codechef-stats.tashif.codes/profile/missing"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThrows(ResourceNotFoundException.class, () -> client.fetchProfile("missing"));
        mockServer.verify();
    }

    @Test
    void mapsProviderReportedNotFoundToMissingProfile() {
        mockServer.expect(requestTo("https://codechef-stats.tashif.codes/profile/missing"))
                .andRespond(withSuccess("""
                        {"success":false,"status":404,"handle":"missing","profile":null}
                        """, MediaType.APPLICATION_JSON));

        assertThrows(ResourceNotFoundException.class, () -> client.fetchProfile("missing"));
        mockServer.verify();
    }

    @Test
    void rejectsSuccessEnvelopeWithoutProfileStatistics() {
        mockServer.expect(requestTo("https://codechef-stats.tashif.codes/profile/empty"))
                .andRespond(withSuccess("""
                        {"success":true,"status":200,"handle":"empty","profile":{
                          "currentRating":null,"highestRating":null,"globalRank":null,"totalSolved":null}}
                        """, MediaType.APPLICATION_JSON));

        assertThrows(PlatformApiException.class, () -> client.fetchProfile("empty"));
        mockServer.verify();
    }

    @Test
    void rejectsMalformedJson() {
        mockServer.expect(requestTo("https://codechef-stats.tashif.codes/profile/broken"))
                .andRespond(withSuccess("{not-json", MediaType.APPLICATION_JSON));

        assertThrows(PlatformApiException.class, () -> client.fetchProfile("broken"));
        mockServer.verify();
    }

    @Test
    void rejectsSuccessFlagWithUnexpectedResponseStatus() {
        mockServer.expect(requestTo("https://codechef-stats.tashif.codes/profile/failed"))
                .andRespond(withSuccess("""
                        {"success":true,"status":503,"handle":"failed","profile":{"totalSolved":0}}
                        """, MediaType.APPLICATION_JSON));

        assertThrows(PlatformApiException.class, () -> client.fetchProfile("failed"));
        mockServer.verify();
    }

    @Test
    void rejectsUnsafeHandleBeforeMakingRequest() {
        assertThrows(ResourceNotFoundException.class, () -> client.fetchProfile("../other"));
        mockServer.verify();
    }

    @Test
    void mapsHttpRateLimitToSafeRateLimitFailure() {
        mockServer.expect(requestTo("https://codechef-stats.tashif.codes/profile/chef_user"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)
                        .body("provider detail must not be surfaced"));

        PlatformApiException exception = assertThrows(
                PlatformApiException.class, () -> client.fetchProfile("chef_user"));

        assertEquals("CodeChef stats provider rate limit exceeded", exception.getMessage());
        mockServer.verify();
    }

    @Test
    void mapsHttpServerErrorToSafeUpstreamFailure() {
        mockServer.expect(requestTo("https://codechef-stats.tashif.codes/profile/chef_user"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("provider detail must not be surfaced"));

        PlatformApiException exception = assertThrows(
                PlatformApiException.class, () -> client.fetchProfile("chef_user"));

        assertEquals("CodeChef stats provider returned an HTTP error", exception.getMessage());
        mockServer.verify();
    }

    @Test
    void preservesTimeoutCauseForSafeSyncFailureClassification() {
        mockServer.expect(requestTo("https://codechef-stats.tashif.codes/profile/chef_user"))
                .andRespond(request -> {
                    throw new ResourceAccessException("Read timed out", new SocketTimeoutException("Read timed out"));
                });

        PlatformApiException exception = assertThrows(
                PlatformApiException.class, () -> client.fetchProfile("chef_user"));

        Throwable cause = exception;
        while (cause != null && !(cause instanceof SocketTimeoutException)) cause = cause.getCause();
        org.junit.jupiter.api.Assertions.assertNotNull(cause);
        mockServer.verify();
    }
}

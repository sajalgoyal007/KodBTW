package com.kodbtw.adapter.contest.codeforces;

import com.kodbtw.adapter.contest.ContestProviderUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.SocketTimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import org.springframework.http.HttpStatus;

class CodeforcesContestClientTest {
    private MockRestServiceServer server;
    private CodeforcesContestClientImpl client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new CodeforcesContestClientImpl(builder.build());
    }

    @Test
    void parsesDocumentedContestListEnvelope() {
        server.expect(requestTo("https://codeforces.com/api/contest.list?gym=false"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"status":"OK","result":[{"id":2100,"name":"Round","phase":"BEFORE","durationSeconds":7200,"startTimeSeconds":1791720000}]}
                        """, MediaType.APPLICATION_JSON));

        var result = client.fetchContests();

        assertEquals(1, result.size());
        assertEquals(2100, result.get(0).id());
        assertEquals("Round", result.get(0).name());
        server.verify();
    }

    @Test
    void mapsHttpFailureAndDoesNotExposeUpstreamBody() {
        server.expect(requestTo("https://codeforces.com/api/contest.list?gym=false"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("provider-secret-detail").contentType(MediaType.TEXT_PLAIN));

        ContestProviderUnavailableException exception = assertThrows(
                ContestProviderUnavailableException.class, client::fetchContests);
        org.junit.jupiter.api.Assertions.assertFalse(exception.getMessage().contains("provider-secret-detail"));
        server.verify();
    }

    @Test
    void mapsMalformedJsonToSafeProviderFailure() {
        server.expect(requestTo("https://codeforces.com/api/contest.list?gym=false"))
                .andRespond(withSuccess("{not-json", MediaType.APPLICATION_JSON));

        assertThrows(ContestProviderUnavailableException.class, client::fetchContests);
        server.verify();
    }

    @Test
    void mapsTimeoutToSafeProviderFailure() {
        server.expect(requestTo("https://codeforces.com/api/contest.list?gym=false"))
                .andRespond(withException(new SocketTimeoutException("upstream-secret-timeout")));

        ContestProviderUnavailableException exception = assertThrows(
                ContestProviderUnavailableException.class, client::fetchContests);
        org.junit.jupiter.api.Assertions.assertEquals("Contest provider is unavailable", exception.getMessage());
        server.verify();
    }

    @Test
    void rejectsFailedStatusAndMissingResult() {
        server.expect(requestTo("https://codeforces.com/api/contest.list?gym=false"))
                .andRespond(withSuccess("""
                        {"status":"FAILED","comment":"private upstream detail"}
                        """, MediaType.APPLICATION_JSON));
        assertThrows(ContestProviderUnavailableException.class, client::fetchContests);
        server.verify();

        RestClient.Builder missingBuilder = RestClient.builder();
        MockRestServiceServer missingServer = MockRestServiceServer.bindTo(missingBuilder).build();
        CodeforcesContestClientImpl missingClient = new CodeforcesContestClientImpl(missingBuilder.build());
        missingServer.expect(requestTo("https://codeforces.com/api/contest.list?gym=false"))
                .andRespond(withSuccess("{\"status\":\"OK\"}", MediaType.APPLICATION_JSON));
        assertThrows(ContestProviderUnavailableException.class, missingClient::fetchContests);
        missingServer.verify();
    }
}

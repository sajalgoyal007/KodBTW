package com.kodbtw.adapter.codeforces;

import com.kodbtw.adapter.codeforces.dto.CodeforcesDto;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CodeforcesClientTest {

    private MockRestServiceServer mockServer;
    private CodeforcesClientImpl client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new CodeforcesClientImpl(builder.build());
    }

    @Test
    void shouldSuccessfullyParseUserInfo() {
        String responseJson = """
                {
                  "status": "OK",
                  "result": [{
                    "handle": "tourist",
                    "rating": 3384,
                    "maxRating": 4009,
                    "rank": "legendary grandmaster",
                    "maxRank": "tourist",
                    "contribution": 112,
                    "friendOfCount": 91104,
                    "registrationTimeSeconds": 1265987288
                  }]
                }
                """;

        mockServer.expect(requestTo("https://codeforces.com/api/user.info?handles=tourist"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        CodeforcesDto.UserInfo info = client.fetchUserInfo("tourist");

        assertNotNull(info);
        assertEquals("tourist", info.handle());
        assertEquals(3384, info.rating());
        assertEquals(4009, info.maxRating());
        assertEquals("legendary grandmaster", info.rank());
        assertEquals(112, info.contribution());
        assertEquals(91104, info.friendOfCount());

        mockServer.verify();
    }

    @Test
    void shouldHandleUnratedUserWithNullRatingFields() {
        String responseJson = """
                {
                  "status": "OK",
                  "result": [{
                    "handle": "newbie123",
                    "contribution": 0,
                    "friendOfCount": 2,
                    "registrationTimeSeconds": 1600000000
                  }]
                }
                """;

        mockServer.expect(requestTo("https://codeforces.com/api/user.info?handles=newbie123"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        CodeforcesDto.UserInfo info = client.fetchUserInfo("newbie123");

        assertNotNull(info);
        assertEquals("newbie123", info.handle());
        assertNull(info.rating());
        assertNull(info.maxRating());
        assertNull(info.rank());

        mockServer.verify();
    }

    @Test
    void shouldThrowResourceNotFoundWhenStatusFailed() {
        String responseJson = """
                {
                  "status": "FAILED",
                  "comment": "handles: User with handle nonexistent_user_99999 not found"
                }
                """;

        mockServer.expect(requestTo("https://codeforces.com/api/user.info?handles=nonexistent_user_99999"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        assertThrows(ResourceNotFoundException.class, () -> client.fetchUserInfo("nonexistent_user_99999"));
        mockServer.verify();
    }

    @Test
    void shouldThrowResourceNotFoundOnHttp400() {
        mockServer.expect(requestTo("https://codeforces.com/api/user.info?handles=baduser"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"status\":\"FAILED\",\"comment\":\"handles: User with handle baduser not found\"}"));

        assertThrows(ResourceNotFoundException.class, () -> client.fetchUserInfo("baduser"));
        mockServer.verify();
    }

    @Test
    void shouldThrowPlatformApiExceptionOnHttp500() {
        mockServer.expect(requestTo("https://codeforces.com/api/user.info?handles=anyuser"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(PlatformApiException.class, () -> client.fetchUserInfo("anyuser"));
        mockServer.verify();
    }

    @Test
    void shouldThrowPlatformApiExceptionOnHttp503() {
        mockServer.expect(requestTo("https://codeforces.com/api/user.info?handles=anyuser"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThrows(PlatformApiException.class, () -> client.fetchUserInfo("anyuser"));
        mockServer.verify();
    }

    @Test
    void shouldThrowResourceNotFoundOnBlankHandle() {
        assertThrows(ResourceNotFoundException.class, () -> client.fetchUserInfo("   "));
    }

    @Test
    void shouldThrowResourceNotFoundOnNullHandle() {
        assertThrows(ResourceNotFoundException.class, () -> client.fetchUserInfo(null));
    }

    @Test
    void shouldThrowPlatformApiExceptionOnFailedStatusWithNonNotFoundComment() {
        String responseJson = """
                {
                  "status": "FAILED",
                  "comment": "Call limit exceeded"
                }
                """;

        mockServer.expect(requestTo("https://codeforces.com/api/user.info?handles=someuser"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        PlatformApiException ex = assertThrows(PlatformApiException.class,
                () -> client.fetchUserInfo("someuser"));
        assertEquals("Codeforces API error: Call limit exceeded", ex.getMessage());
        mockServer.verify();
    }

    @Test
    void shouldThrowPlatformApiExceptionOnEmptyResultList() {
        String responseJson = """
                {
                  "status": "OK",
                  "result": []
                }
                """;

        mockServer.expect(requestTo("https://codeforces.com/api/user.info?handles=emptyuser"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        assertThrows(ResourceNotFoundException.class, () -> client.fetchUserInfo("emptyuser"));
        mockServer.verify();
    }
}

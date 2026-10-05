package com.kodbtw.adapter.leetcode;

import com.kodbtw.adapter.leetcode.dto.LeetCodeDto;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class LeetCodeClientTest {

    private MockRestServiceServer mockServer;
    private LeetCodeClientImpl client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new LeetCodeClientImpl(builder.build());
    }

    @Test
    void shouldSuccessfullyParseUserProfile() {
        String responseJson = """
                {
                  "data": {
                    "matchedUser": {
                      "username": "lee215",
                      "profile": { "ranking": 103463 },
                      "submitStatsGlobal": {
                        "acSubmissionNum": [
                          { "difficulty": "All", "count": 686 },
                          { "difficulty": "Easy", "count": 126 },
                          { "difficulty": "Medium", "count": 411 },
                          { "difficulty": "Hard", "count": 149 }
                        ]
                      },
                      "userCalendar": { "streak": 3 }
                    },
                    "userContestRanking": {
                      "rating": 2204.316,
                      "attendedContestsCount": 27,
                      "globalRanking": 7550
                    }
                  }
                }
                """;

        mockServer.expect(requestTo("https://leetcode.com/graphql"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Referer", "https://leetcode.com"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        LeetCodeDto.GraphQLData data = client.fetchUserProfile("lee215");

        assertNotNull(data);
        assertNotNull(data.matchedUser());
        assertEquals("lee215", data.matchedUser().username());
        assertEquals(103463, data.matchedUser().profile().ranking());
        assertEquals(4, data.matchedUser().submitStatsGlobal().acSubmissionNum().size());
        assertEquals(2204.316, data.userContestRanking().rating());

        mockServer.verify();
    }

    @Test
    void shouldThrowResourceNotFoundWhenUserDoesNotExist() {
        String responseJson = """
                {
                  "errors": [
                    { "message": "That user does not exist." }
                  ],
                  "data": {
                    "matchedUser": null,
                    "userContestRanking": null
                  }
                }
                """;

        mockServer.expect(requestTo("https://leetcode.com/graphql"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        assertThrows(ResourceNotFoundException.class, () -> client.fetchUserProfile("nonexistent"));
        mockServer.verify();
    }

    @Test
    void shouldThrowResourceNotFoundWhenMatchedUserIsNull() {
        String responseJson = """
                {
                  "data": {
                    "matchedUser": null,
                    "userContestRanking": null
                  }
                }
                """;

        mockServer.expect(requestTo("https://leetcode.com/graphql"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        assertThrows(ResourceNotFoundException.class, () -> client.fetchUserProfile("nulluser"));
        mockServer.verify();
    }

    @Test
    void shouldThrowPlatformApiExceptionOnHttpError() {
        mockServer.expect(requestTo("https://leetcode.com/graphql"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY));

        assertThrows(PlatformApiException.class, () -> client.fetchUserProfile("anyuser"));
        mockServer.verify();
    }

    @Test
    void shouldThrowResourceNotFoundOnBlankUsername() {
        assertThrows(ResourceNotFoundException.class, () -> client.fetchUserProfile("   "));
    }
}

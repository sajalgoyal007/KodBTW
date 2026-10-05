package com.kodbtw.adapter.leetcode;

import com.kodbtw.adapter.leetcode.dto.LeetCodeDto;
import com.kodbtw.exception.PlatformApiException;
import com.kodbtw.exception.ResourceNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.Map;

@Component
public class LeetCodeClientImpl implements LeetCodeClient {

    private static final String LEETCODE_GRAPHQL_URL = "https://leetcode.com/graphql";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final String REFERER = "https://leetcode.com";

    private static final String GRAPHQL_QUERY = """
            query getUserProfile($username: String!) {
              matchedUser(username: $username) {
                username
                profile {
                  ranking
                }
                submitStatsGlobal {
                  acSubmissionNum {
                    difficulty
                    count
                  }
                }
                userCalendar {
                  streak
                }
              }
              userContestRanking(username: $username) {
                rating
                attendedContestsCount
                globalRanking
              }
            }
            """;

    private final RestClient restClient;

    public LeetCodeClientImpl() {
        this(createDefaultRestClient());
    }

    public LeetCodeClientImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    private static RestClient createDefaultRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        return RestClient.builder()
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
                .defaultHeader(HttpHeaders.REFERER, REFERER)
                .build();
    }

    @Override
    public LeetCodeDto.GraphQLData fetchUserProfile(String username) {
        if (username == null || username.isBlank()) {
            throw new ResourceNotFoundException("LeetCode username cannot be blank");
        }

        LeetCodeDto.GraphQLRequest requestBody = new LeetCodeDto.GraphQLRequest(
                GRAPHQL_QUERY,
                Map.of("username", username.trim())
        );

        LeetCodeDto.GraphQLResponse response;
        try {
            response = restClient.post()
                    .uri(LEETCODE_GRAPHQL_URL)
                    .header(HttpHeaders.USER_AGENT, USER_AGENT)
                    .header(HttpHeaders.REFERER, REFERER)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(LeetCodeDto.GraphQLResponse.class);
        } catch (RestClientException ex) {
            throw new PlatformApiException("Failed to communicate with LeetCode API: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new PlatformApiException("Unexpected error calling LeetCode API: " + ex.getMessage(), ex);
        }

        if (response == null || response.data() == null) {
            if (response != null && response.errors() != null && !response.errors().isEmpty()) {
                String errorMsg = response.errors().get(0).message();
                if (errorMsg != null && errorMsg.toLowerCase().contains("does not exist")) {
                    throw new ResourceNotFoundException("LeetCode user not found: " + username);
                }
                throw new PlatformApiException("LeetCode API error: " + errorMsg);
            }
            throw new PlatformApiException("Empty response received from LeetCode API");
        }

        if (response.data().matchedUser() == null) {
            throw new ResourceNotFoundException("LeetCode user not found: " + username);
        }

        return response.data();
    }
}

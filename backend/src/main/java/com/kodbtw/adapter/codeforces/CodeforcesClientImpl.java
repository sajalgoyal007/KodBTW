package com.kodbtw.adapter.codeforces;

import com.kodbtw.adapter.codeforces.dto.CodeforcesDto;
import com.kodbtw.exception.PlatformApiException;
import com.kodbtw.exception.ResourceNotFoundException;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

/**
 * Implementation of {@link CodeforcesClient} using Spring's {@link RestClient}.
 *
 * <p>Uses the official public Codeforces API:
 * {@code GET https://codeforces.com/api/user.info?handles=<handle>}
 *
 * <p>This is a fully public endpoint. No API key, authentication token,
 * cookies, or browser automation is used. Only a public Codeforces handle is required.
 *
 * <p>Rate limiting: Codeforces API allows approximately 5 requests per second for
 * unauthenticated access. This client does not implement aggressive retries.
 * If rate-limited, the error is propagated cleanly.
 */
@Component
public class CodeforcesClientImpl implements CodeforcesClient {

    private static final String CODEFORCES_API_URL = "https://codeforces.com/api/user.info";

    private final RestClient restClient;

    public CodeforcesClientImpl() {
        this(createDefaultRestClient());
    }

    public CodeforcesClientImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    private static RestClient createDefaultRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public CodeforcesDto.UserInfo fetchUserInfo(String handle) {
        if (handle == null || handle.isBlank()) {
            throw new ResourceNotFoundException("Codeforces handle cannot be blank");
        }

        String trimmedHandle = handle.trim();

        CodeforcesDto.ApiResponse response;
        try {
            response = restClient.get()
                    .uri(CODEFORCES_API_URL + "?handles={handle}", trimmedHandle)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(CodeforcesDto.ApiResponse.class);
        } catch (RestClientException ex) {
            String message = ex.getMessage();
            // Codeforces returns HTTP 400 for user-not-found, which RestClient treats as a client error
            if (message != null && message.contains("400")) {
                throw new ResourceNotFoundException("Codeforces user not found: " + trimmedHandle);
            }
            throw new PlatformApiException("Failed to communicate with Codeforces API: " + message, ex);
        } catch (Exception ex) {
            throw new PlatformApiException("Unexpected error calling Codeforces API: " + ex.getMessage(), ex);
        }

        if (response == null) {
            throw new PlatformApiException("Empty response received from Codeforces API");
        }

        if ("FAILED".equals(response.status())) {
            String comment = response.comment();
            if (comment != null && comment.toLowerCase().contains("not found")) {
                throw new ResourceNotFoundException("Codeforces user not found: " + trimmedHandle);
            }
            throw new PlatformApiException("Codeforces API error: " + comment);
        }

        if (response.result() == null || response.result().isEmpty()) {
            throw new ResourceNotFoundException("Codeforces user not found: " + trimmedHandle);
        }

        return response.result().get(0);
    }
}

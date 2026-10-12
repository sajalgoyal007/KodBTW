package com.kodbtw.adapter.hackerrank;

import com.kodbtw.adapter.hackerrank.dto.HackerRankStatsResponse;
import com.kodbtw.exception.PlatformApiException;
import com.kodbtw.exception.ResourceNotFoundException;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.Locale;

/**
 * Isolated client prototype for an unofficial HackerRank stats provider.
 * Normal HackerRank synchronization remains disabled by Platform.hasLiveStatsSource().
 */
@Component
public class HackerRankClientImpl implements HackerRankClient {

    private static final String API_BASE_URL = "https://hackerrank-stats.tashif.codes/";
    private final RestClient restClient;

    public HackerRankClientImpl() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    public HackerRankClientImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public HackerRankStatsResponse fetchStats(String username) {
        if (username == null || !username.trim().matches("[A-Za-z0-9_.-]{1,50}")) {
            throw new ResourceNotFoundException("HackerRank username is invalid");
        }

        String requestedUsername = username.trim();
        HackerRankStatsResponse response;
        try {
            response = restClient.get()
                    .uri(API_BASE_URL + "{username}/stats", requestedUsername)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(HackerRankStatsResponse.class);
        } catch (HttpStatusCodeException exception) {
            int status = exception.getStatusCode().value();
            if (status == 404) {
                throw new ResourceNotFoundException("HackerRank profile not found");
            }
            if (status == 429) {
                throw new PlatformApiException("HackerRank stats provider rate limit exceeded", exception);
            }
            if (status == 504) {
                throw new PlatformApiException("HackerRank stats provider timed out", exception);
            }
            throw new PlatformApiException("HackerRank stats provider returned an HTTP error", exception);
        } catch (RestClientException exception) {
            throw new PlatformApiException("Failed to communicate with HackerRank stats provider", exception);
        }

        validateResponse(response, requestedUsername);
        return response;
    }

    private void validateResponse(HackerRankStatsResponse response, String requestedUsername) {
        if (response == null
                || response.status() == null
                || !"success".equalsIgnoreCase(response.status())
                || response.platform() == null
                || !"hackerrank".equals(response.platform().trim().toLowerCase(Locale.ROOT))
                || response.username() == null
                || !requestedUsername.equalsIgnoreCase(response.username().trim())
                || response.data() == null) {
            throw new PlatformApiException("HackerRank stats provider returned an invalid response");
        }

        Integer totalSolved = response.data().totalSolved();
        if (totalSolved == null) {
            throw new PlatformApiException("HackerRank stats provider returned no usable solved count");
        }
        if (totalSolved < 0) {
            throw new PlatformApiException("HackerRank stats provider returned invalid statistics");
        }
    }
}

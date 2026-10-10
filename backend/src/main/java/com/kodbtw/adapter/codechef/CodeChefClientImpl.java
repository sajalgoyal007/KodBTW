package com.kodbtw.adapter.codechef;

import com.kodbtw.adapter.codechef.dto.CodeChefApiResponse;
import com.kodbtw.exception.PlatformApiException;
import com.kodbtw.exception.ResourceNotFoundException;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

@Component
public class CodeChefClientImpl implements CodeChefClient {

    private static final String API_BASE_URL =
            "https://codechef-stats.tashif.codes/profile/";

    private final RestClient restClient;

    public CodeChefClientImpl() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(5));

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .build();
    }

    public CodeChefClientImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public CodeChefApiResponse fetchProfile(String handle) {
        if (handle == null || !handle.trim().matches("[A-Za-z0-9_.-]{1,50}")) {
            throw new ResourceNotFoundException("CodeChef handle is invalid");
        }

        String trimmedHandle = handle.trim();
        CodeChefApiResponse response;

        try {
            response = restClient.get()
                    .uri(API_BASE_URL + "{handle}", trimmedHandle)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(CodeChefApiResponse.class);
        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new ResourceNotFoundException("CodeChef profile not found: " + trimmedHandle);
            }
            if (ex.getStatusCode().value() == 429) {
                throw new PlatformApiException("CodeChef stats provider rate limit exceeded", ex);
            }
            throw new PlatformApiException("CodeChef stats provider returned an HTTP error", ex);
        } catch (RestClientException ex) {
            throw new PlatformApiException("Failed to communicate with CodeChef stats provider", ex);
        }

        if (response == null) {
            throw new PlatformApiException("CodeChef stats provider returned an invalid response");
        }

        if (response.status() != null && response.status() == 404) {
            throw new ResourceNotFoundException("CodeChef profile not found: " + trimmedHandle);
        }
        if (response.status() == null || response.status() != 200) {
            throw new PlatformApiException("CodeChef stats provider returned an unexpected status");
        }
        if (!Boolean.TRUE.equals(response.success())) {
            throw new PlatformApiException("CodeChef stats provider did not report success");
        }

        if (response.profile() == null) {
            throw new ResourceNotFoundException("CodeChef profile not found: " + trimmedHandle);
        }

        CodeChefApiResponse.Profile profile = response.profile();
        boolean hasUsefulStats = profile.currentRating() != null
                || profile.highestRating() != null
                || profile.globalRank() != null
                || profile.totalSolved() != null;

        if (!hasUsefulStats) {
            throw new PlatformApiException("CodeChef stats provider returned no usable profile statistics");
        }

        if (response.handle() != null && !response.handle().equalsIgnoreCase(trimmedHandle)) {
            throw new PlatformApiException("CodeChef stats provider returned a different profile");
        }

        if (profile.currentRating() != null && profile.currentRating() < 0
                || profile.highestRating() != null && profile.highestRating() < 0
                || profile.globalRank() != null && profile.globalRank() < 0
                || profile.totalSolved() != null && profile.totalSolved() < 0) {
            throw new PlatformApiException("CodeChef stats provider returned invalid negative statistics");
        }

        return response;
    }
}

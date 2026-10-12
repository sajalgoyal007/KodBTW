package com.kodbtw.adapter.geeksforgeeks;

import com.kodbtw.adapter.geeksforgeeks.dto.GeeksForGeeksStatsResponse;
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
 * Prototype client for the unofficial GFG stats provider. Normal platform sync remains disabled
 * until GFG is explicitly added to Platform.hasLiveStatsSource().
 */
@Component
public class GeeksForGeeksClientImpl implements GeeksForGeeksClient {

    private static final String API_BASE_URL = "https://gfg-stats.tashif.codes/";

    private final RestClient restClient;

    public GeeksForGeeksClientImpl() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    public GeeksForGeeksClientImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public GeeksForGeeksStatsResponse fetchStats(String username) {
        if (username == null || !username.trim().matches("[A-Za-z0-9_.-]{1,50}")) {
            throw new ResourceNotFoundException("GeeksforGeeks username is invalid");
        }

        String requestedUsername = username.trim();
        GeeksForGeeksStatsResponse response;
        try {
            response = restClient.get()
                    .uri(API_BASE_URL + "{username}/stats", requestedUsername)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(GeeksForGeeksStatsResponse.class);
        } catch (HttpStatusCodeException exception) {
            int status = exception.getStatusCode().value();
            if (status == 404) {
                throw new ResourceNotFoundException("GeeksforGeeks profile not found");
            }
            if (status == 429) {
                throw new PlatformApiException("GeeksforGeeks stats provider rate limit exceeded", exception);
            }
            if (status == 504) {
                throw new PlatformApiException("GeeksforGeeks stats provider timed out", exception);
            }
            throw new PlatformApiException("GeeksforGeeks stats provider returned an HTTP error", exception);
        } catch (RestClientException exception) {
            throw new PlatformApiException("Failed to communicate with GeeksforGeeks stats provider", exception);
        }

        validateResponse(response, requestedUsername);
        return response;
    }

    private void validateResponse(GeeksForGeeksStatsResponse response, String requestedUsername) {
        if (response == null
                || response.status() == null
                || !"success".equalsIgnoreCase(response.status())
                || response.platform() == null
                || !isSupportedPlatformName(response.platform())
                || response.username() == null
                || !requestedUsername.equalsIgnoreCase(response.username().trim())
                || response.data() == null) {
            throw new PlatformApiException("GeeksforGeeks stats provider returned an invalid response");
        }

        GeeksForGeeksStatsResponse.Stats stats = response.data();
        GeeksForGeeksStatsResponse.DifficultyCounts difficulty = stats.byDifficulty();
        Integer easy = difficulty == null ? null : difficulty.easy();
        Integer medium = difficulty == null ? null : difficulty.medium();
        Integer hard = difficulty == null ? null : difficulty.hard();

        if (stats.totalSolved() == null && easy == null && medium == null && hard == null) {
            throw new PlatformApiException("GeeksforGeeks stats provider returned no usable statistics");
        }
        if (isNegative(stats.totalSolved()) || isNegative(easy) || isNegative(medium) || isNegative(hard)) {
            throw new PlatformApiException("GeeksforGeeks stats provider returned invalid statistics");
        }
    }

    private boolean isSupportedPlatformName(String platform) {
        String normalized = platform.trim().toLowerCase(Locale.ROOT);
        return normalized.equals("gfg") || normalized.equals("geeksforgeeks");
    }

    private boolean isNegative(Integer value) {
        return value != null && value < 0;
    }
}

package com.kodbtw.adapter.contest.codeforces;

import com.kodbtw.adapter.contest.ContestProviderUnavailableException;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "contest-calendar", name = "provider", havingValue = "codeforces", matchIfMissing = true)
public class CodeforcesContestClientImpl implements CodeforcesContestClient {
    private static final String API_URL = "https://codeforces.com/api/contest.list?gym=false";
    private final RestClient restClient;

    public CodeforcesContestClientImpl() {
        this(createDefaultRestClient());
    }

    public CodeforcesContestClientImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    private static RestClient createDefaultRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(8));
        return RestClient.builder().requestFactory(factory).build();
    }

    @Override
    public List<CodeforcesContestApiResponse.CodeforcesContestRecord> fetchContests() {
        try {
            CodeforcesContestApiResponse response = restClient.get()
                    .uri(API_URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(CodeforcesContestApiResponse.class);
            if (response == null || !"OK".equals(response.status()) || response.result() == null) {
                throw new ContestProviderUnavailableException();
            }
            return response.result();
        } catch (ContestProviderUnavailableException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new ContestProviderUnavailableException(exception);
        }
    }
}

package com.kodbtw.adapter.atcoder;

import com.kodbtw.adapter.atcoder.dto.AtCoderStats;
import com.kodbtw.exception.PlatformApiException;
import com.kodbtw.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AtCoderClientTest {

    private static final String HISTORY_URL = "https://atcoder.jp/users/sample_user/history/json";
    private static final String AC_RANK_URL =
            "https://kenkoooo.com/atcoder/atcoder-api/v3/user/ac_rank?user=sample_user";
    private static final String SUBMISSIONS_URL =
            "https://kenkoooo.com/atcoder/atcoder-api/v3/user/submissions?user=sample_user&from_second=";

    private MockRestServiceServer mockServer;
    private AtCoderClientImpl client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new AtCoderClientImpl(builder.build());
    }

    @Test
    void combinesHistoryRankSolvedCountsAndStreaks() {
        long today = LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toEpochSecond();
        long day = 86_400L;

        mockServer.expect(requestTo(HISTORY_URL)).andRespond(withSuccess("""
                [{"IsRated":true,"NewRating":1200},{"IsRated":true,"NewRating":1450}]
                """, MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo(AC_RANK_URL))
                .andRespond(withSuccess("{\"count\":321,\"rank\":4567}", MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo(startsWith(SUBMISSIONS_URL))).andRespond(withSuccess("""
                [
                  {"epoch_second":%d,"problem_id":"a","result":"AC"},
                  {"epoch_second":%d,"problem_id":"b","result":"AC"},
                  {"epoch_second":%d,"problem_id":"c","result":"AC"},
                  {"epoch_second":%d,"problem_id":"d","result":"AC"},
                  {"epoch_second":%d,"problem_id":"e","result":"WA"}
                ]
                """.formatted(today, today - day, today - 2 * day, today - 10 * day, today),
                MediaType.APPLICATION_JSON));

        AtCoderStats stats = client.fetchStats("sample_user");

        assertEquals(1450, stats.rating());
        assertEquals(1450, stats.maxRating());
        assertEquals(4567, stats.rank());
        assertEquals(321, stats.totalSolved());
        assertEquals(2, stats.contestsParticipated());
        assertEquals(3, stats.currentStreak());
        assertEquals(3, stats.longestStreak());
        mockServer.verify();
    }

    @Test
    void emptyPayloadsProduceZerosOnlyWhereTheApiReportsThem() {
        mockServer.expect(requestTo(HISTORY_URL))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo(AC_RANK_URL))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo(startsWith(SUBMISSIONS_URL)))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        AtCoderStats stats = client.fetchStats("sample_user");

        assertNull(stats.rating());
        assertNull(stats.maxRating());
        assertNull(stats.rank());
        assertNull(stats.totalSolved());
        assertEquals(0, stats.contestsParticipated());
        assertEquals(0, stats.currentStreak());
        assertEquals(0, stats.longestStreak());
        mockServer.verify();
    }

    @Test
    void mapsNotFoundToMissingProfile() {
        mockServer.expect(requestTo("https://atcoder.jp/users/missing/history/json"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThrows(ResourceNotFoundException.class, () -> client.fetchStats("missing"));
        mockServer.verify();
    }

    @Test
    void mapsRateLimitToSafeFailure() {
        mockServer.expect(requestTo(HISTORY_URL))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS).body("upstream detail"));

        PlatformApiException exception = assertThrows(PlatformApiException.class,
                () -> client.fetchStats("sample_user"));

        assertEquals("AtCoder API rate limit exceeded", exception.getMessage());
        mockServer.verify();
    }

    @Test
    void mapsServerErrorsToSafeFailure() {
        mockServer.expect(requestTo(HISTORY_URL)).andRespond(withStatus(HttpStatus.BAD_GATEWAY));

        PlatformApiException exception = assertThrows(PlatformApiException.class,
                () -> client.fetchStats("sample_user"));

        assertEquals("AtCoder API returned an HTTP error", exception.getMessage());
        mockServer.verify();
    }

    @Test
    void rejectsUnsafeHandleWithoutMakingAnyRequest() {
        assertThrows(ResourceNotFoundException.class, () -> client.fetchStats("../someone"));
        mockServer.verify();
    }
}

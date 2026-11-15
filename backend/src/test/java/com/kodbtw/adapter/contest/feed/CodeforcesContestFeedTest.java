package com.kodbtw.adapter.contest.feed;

import com.kodbtw.contest.ContestDraft;
import com.kodbtw.contest.ContestPlatform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CodeforcesContestFeedTest {

    private static final Instant FROM = Instant.parse("2026-11-08T00:00:00Z");
    private static final Instant TO = Instant.parse("2026-12-31T00:00:00Z");

    private MockRestServiceServer server;
    private CodeforcesContestFeed feed;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        feed = new CodeforcesContestFeed(builder.build());
    }

    @Test
    void fetchesUpcomingContestsInUtc() {
        server.expect(requestTo(CodeforcesContestFeed.API_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"status":"OK","result":[
                          {"id":1001,"name":"Codeforces Round 999","phase":"BEFORE",
                           "durationSeconds":7200,"startTimeSeconds":1794835800},
                          {"id":1002,"name":"Old Round","phase":"FINISHED",
                           "durationSeconds":7200,"startTimeSeconds":1700000000}
                        ]}
                        """, MediaType.APPLICATION_JSON));

        List<ContestDraft> drafts = feed.fetch(FROM, TO);

        assertThat(drafts).singleElement().satisfies(draft -> {
            assertThat(draft.externalId()).isEqualTo("CODEFORCES:1001");
            assertThat(draft.platform()).isEqualTo(ContestPlatform.CODEFORCES);
            assertThat(draft.title()).isEqualTo("Codeforces Round 999");
            assertThat(draft.start()).isEqualTo(Instant.ofEpochSecond(1794835800L));
            assertThat(draft.durationSeconds()).isEqualTo(7200L);
            assertThat(draft.contestUrl()).isEqualTo("https://codeforces.com/contest/1001");
        });
        server.verify();
    }

    @Test
    void rejectsUnusableResponses() {
        server.expect(requestTo(CodeforcesContestFeed.API_URL))
                .andRespond(withSuccess("{\"status\":\"FAILED\"}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> feed.fetch(FROM, TO))
                .isInstanceOf(IllegalStateException.class);
    }
}

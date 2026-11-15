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
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class HackerRankContestFeedTest {

    private static final Instant FROM = Instant.parse("2026-11-08T00:00:00Z");
    private static final Instant TO = Instant.parse("2026-12-31T00:00:00Z");

    private MockRestServiceServer server;
    private HackerRankContestFeed feed;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        feed = new HackerRankContestFeed(builder.build());
    }

    @Test
    void mapsEpochStartAndEndTimesToUtc() {
        server.expect(requestTo(HackerRankContestFeed.API_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"models":[
                          {"id":405563,"name":"Orchestrate December'26","slug":"hackerrank-orchestrate-december26",
                           "epoch_starttime":1794835800,"epoch_endtime":1794841200}
                        ]}
                        """, MediaType.APPLICATION_JSON));

        List<ContestDraft> drafts = feed.fetch(FROM, TO);

        assertThat(drafts).singleElement().satisfies(draft -> {
            assertThat(draft.externalId()).isEqualTo("HACKERRANK:405563");
            assertThat(draft.platform()).isEqualTo(ContestPlatform.HACKERRANK);
            assertThat(draft.start()).isEqualTo(Instant.ofEpochSecond(1794835800L));
            assertThat(draft.end()).isEqualTo(Instant.ofEpochSecond(1794841200L));
            assertThat(draft.durationSeconds()).isEqualTo(5400L);
            assertThat(draft.contestUrl())
                    .isEqualTo("https://www.hackerrank.com/contests/hackerrank-orchestrate-december26");
        });
        server.verify();
    }
}

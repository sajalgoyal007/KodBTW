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

class AtCoderContestFeedTest {

    private static final Instant FROM = Instant.parse("2026-11-08T00:00:00Z");
    private static final Instant TO = Instant.parse("2026-12-31T00:00:00Z");

    private MockRestServiceServer server;
    private AtCoderContestFeed feed;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        feed = new AtCoderContestFeed(builder.build());
    }

    @Test
    void mapsEpochSecondsToUtcInstants() {
        server.expect(requestTo(AtCoderContestFeed.API_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        [
                          {"id":"abc484","start_epoch_second":1794835800,"duration_second":6000,
                           "title":"AtCoder Beginner Contest 484","rate_change":"~ 1999"},
                          {"id":"APG4b","start_epoch_second":0,"duration_second":3153600000,
                           "title":"Guide","rate_change":"-"}
                        ]
                        """, MediaType.APPLICATION_JSON));

        List<ContestDraft> drafts = feed.fetch(FROM, TO);

        assertThat(drafts).singleElement().satisfies(draft -> {
            assertThat(draft.externalId()).isEqualTo("ATCODER:abc484");
            assertThat(draft.platform()).isEqualTo(ContestPlatform.ATCODER);
            assertThat(draft.start()).isEqualTo(Instant.ofEpochSecond(1794835800L));
            assertThat(draft.durationSeconds()).isEqualTo(6000L);
            assertThat(draft.contestUrl()).isEqualTo("https://atcoder.jp/contests/abc484");
        });
        server.verify();
    }
}

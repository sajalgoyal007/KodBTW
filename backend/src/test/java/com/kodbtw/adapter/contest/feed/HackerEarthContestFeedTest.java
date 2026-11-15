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

class HackerEarthContestFeedTest {

    private static final Instant FROM = Instant.parse("2026-11-08T00:00:00Z");
    private static final Instant TO = Instant.parse("2026-12-31T00:00:00Z");

    private MockRestServiceServer server;
    private HackerEarthContestFeed feed;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        feed = new HackerEarthContestFeed(builder.build());
    }

    @Test
    void parsesOffsetTimestampsAndNormalizesToUtc() {
        server.expect(requestTo(HackerEarthContestFeed.API_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"response":[
                          {"title":"Holiday Hackathon","url":"https://www.hackerearth.com/challenges/hackathon/holiday-hack/",
                           "start_utc_tz":"2026-11-16 04:30:00+00:00","end_utc_tz":"2026-11-16 10:30:00+00:00"}
                        ]}
                        """, MediaType.APPLICATION_JSON));

        List<ContestDraft> drafts = feed.fetch(FROM, TO);

        assertThat(drafts).singleElement().satisfies(draft -> {
            assertThat(draft.platform()).isEqualTo(ContestPlatform.HACKEREARTH);
            assertThat(draft.externalId()).startsWith("HACKEREARTH:");
            assertThat(draft.start()).isEqualTo(Instant.parse("2026-11-16T04:30:00Z"));
            assertThat(draft.end()).isEqualTo(Instant.parse("2026-11-16T10:30:00Z"));
            assertThat(draft.durationSeconds()).isEqualTo(21600L);
            assertThat(draft.contestUrl())
                    .isEqualTo("https://www.hackerearth.com/challenges/hackathon/holiday-hack/");
        });
        server.verify();
    }

    @Test
    void acceptsNonUtcOffsetsAndConvertsToUtc() {
        server.expect(requestTo(HackerEarthContestFeed.API_URL))
                .andRespond(withSuccess("""
                        {"response":[
                          {"title":"IST event","url":"https://www.hackerearth.com/challenges/competitive/ist-event/",
                           "start_utc_tz":"2026-11-16 19:00:00+05:30","end_utc_tz":"2026-11-16 21:00:00+05:30"}
                        ]}
                        """, MediaType.APPLICATION_JSON));

        List<ContestDraft> drafts = feed.fetch(FROM, TO);

        assertThat(drafts).singleElement()
                .extracting(ContestDraft::start)
                .isEqualTo(Instant.parse("2026-11-16T13:30:00Z"));
    }
}

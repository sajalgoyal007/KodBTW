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
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class LeetCodeContestFeedTest {

    private static final Instant FROM = Instant.parse("2026-11-08T00:00:00Z");
    private static final Instant TO = Instant.parse("2026-12-31T00:00:00Z");

    private MockRestServiceServer server;
    private LeetCodeContestFeed feed;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        feed = new LeetCodeContestFeed(builder.build());
    }

    @Test
    void postsTheAllContestsQueryAndMapsEpochSeconds() {
        server.expect(requestTo(LeetCodeContestFeed.API_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("allContests")))
                .andRespond(withSuccess("""
                        {"data":{"allContests":[
                          {"title":"Weekly Contest 528","titleSlug":"weekly-contest-528",
                           "startTime":1794835800,"duration":5400}
                        ]}}
                        """, MediaType.APPLICATION_JSON));

        List<ContestDraft> drafts = feed.fetch(FROM, TO);

        assertThat(drafts).singleElement().satisfies(draft -> {
            assertThat(draft.externalId()).isEqualTo("LEETCODE:weekly-contest-528");
            assertThat(draft.platform()).isEqualTo(ContestPlatform.LEETCODE);
            assertThat(draft.start()).isEqualTo(Instant.ofEpochSecond(1794835800L));
            assertThat(draft.durationSeconds()).isEqualTo(5400L);
            assertThat(draft.contestUrl())
                    .isEqualTo("https://leetcode.com/contest/weekly-contest-528/");
        });
        server.verify();
    }
}

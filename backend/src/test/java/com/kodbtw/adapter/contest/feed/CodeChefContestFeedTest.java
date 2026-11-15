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

class CodeChefContestFeedTest {

    private static final Instant FROM = Instant.parse("2026-11-08T00:00:00Z");
    private static final Instant TO = Instant.parse("2026-12-31T00:00:00Z");

    private MockRestServiceServer server;
    private CodeChefContestFeed feed;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        feed = new CodeChefContestFeed(builder.build());
    }

    @Test
    void convertsIstTimestampsToUtc() {
        server.expect(requestTo(CodeChefContestFeed.API_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "status":"success",
                          "present_contests":[],
                          "future_contests":[
                            {"contest_code":"DSAMONDAY029","contest_name":"Monday Munch DSA Challenge",
                             "contest_start_date_iso":"2026-11-16T19:00:00+05:30",
                             "contest_end_date_iso":"2026-11-16T22:00:00+05:30","contest_duration":"180"}
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        List<ContestDraft> drafts = feed.fetch(FROM, TO);

        assertThat(drafts).singleElement().satisfies(draft -> {
            assertThat(draft.platform()).isEqualTo(ContestPlatform.CODECHEF);
            assertThat(draft.title()).isEqualTo("Monday Munch DSA Challenge");
            // 19:00 IST is 13:30 UTC.
            assertThat(draft.start()).isEqualTo(Instant.parse("2026-11-16T13:30:00Z"));
            assertThat(draft.end()).isEqualTo(Instant.parse("2026-11-16T16:30:00Z"));
            assertThat(draft.durationSeconds()).isEqualTo(10800L);
            assertThat(draft.contestUrl()).isEqualTo("https://www.codechef.com/DSAMONDAY029");
        });
        server.verify();
    }

    @Test
    void includesPresentContestsAndDropsOnesOutsideTheWindow() {
        server.expect(requestTo(CodeChefContestFeed.API_URL))
                .andRespond(withSuccess("""
                        {
                          "present_contests":[
                            {"contest_code":"LIVE1","contest_name":"Live Contest",
                             "contest_start_date_iso":"2026-11-15T20:00:00+05:30",
                             "contest_end_date_iso":"2026-11-15T22:00:00+05:30"}
                          ],
                          "future_contests":[
                            {"contest_code":"FAR1","contest_name":"Far Future",
                             "contest_start_date_iso":"2027-06-01T20:00:00+05:30",
                             "contest_end_date_iso":"2027-06-01T22:00:00+05:30"}
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        List<ContestDraft> drafts = feed.fetch(FROM, TO);

        assertThat(drafts).extracting(ContestDraft::externalId).containsExactly("CODECHEF:LIVE1");
    }
}

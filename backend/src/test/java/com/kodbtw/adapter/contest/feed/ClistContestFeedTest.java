package com.kodbtw.adapter.contest.feed;

import com.kodbtw.contest.ContestDraft;
import com.kodbtw.contest.ContestPlatform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ClistContestFeedTest {

    private static final Instant FROM = Instant.parse("2026-11-08T00:00:00Z");
    private static final Instant TO = Instant.parse("2026-12-31T00:00:00Z");

    private RestClient.Builder builder;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
    }

    private ClistContestFeed feed(String username, String apiKey) {
        return new ClistContestFeed(builder.build(), username, apiKey, "https://clist.by/api/v4");
    }

    @Test
    void requestsEverySupportedResourceInOneCallAndReturnsAllPlatforms() {
        server.expect(request -> {
                    String url = request.getURI().toString();
                    assertThat(url).startsWith("https://clist.by/api/v4/contest/");
                    assertThat(url).contains("resource__in=", "order_by=start", "offset=0");
                    assertThat(url).contains("start__gte=2026-11-08T00", "start__lte=2026-12-31T00");
                })
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "ApiKey alice:secret"))
                .andRespond(withSuccess(payload(), MediaType.APPLICATION_JSON));

        List<ContestDraft> drafts = feed("alice", "secret").fetch(FROM, TO);

        assertThat(drafts).hasSize(8);
        Set<ContestPlatform> platforms = drafts.stream()
                .map(ContestDraft::platform)
                .collect(Collectors.toSet());
        assertThat(platforms).containsExactlyInAnyOrderElementsOf(
                EnumSet.allOf(ContestPlatform.class));
        assertThat(drafts).allMatch(draft -> draft.start().equals(Instant.parse("2026-11-16T12:00:00Z")));
        assertThat(drafts).allMatch(draft -> draft.durationSeconds() == 7200L);
        assertThat(drafts).allMatch(draft -> draft.contestUrl().startsWith("https://"));
        server.verify();
    }

    @Test
    void skipsUnsupportedResourcesUnsafeUrlsAndEventsOutsideTheWindow() {
        String payload = """
                {"objects":[
                  {"id":1,"event":"Unknown","href":"https://example.com/1","resource":"someothersite.com",
                   "start":"2026-11-16T12:00:00","end":"2026-11-16T14:00:00","duration":7200},
                  {"id":2,"event":"Unsafe","href":"javascript:alert(1)","resource":"codeforces.com",
                   "start":"2026-11-16T12:00:00","end":"2026-11-16T14:00:00","duration":7200},
                  {"id":3,"event":"Too late","href":"https://codeforces.com/contest/3","resource":"codeforces.com",
                   "start":"2027-03-01T12:00:00","end":"2027-03-01T14:00:00","duration":7200}
                ]}
                """;
        server.expect(request -> assertThat(request.getURI().toString()).contains("clist.by"))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        assertThat(feed("alice", "secret").fetch(FROM, TO)).isEmpty();
    }

    @Test
    void derivesDurationWhenClistOmitsIt() {
        String payload = """
                {"objects":[
                  {"id":7,"event":"Two and a half hours","href":"https://leetcode.com/contest/weekly",
                   "resource":"leetcode.com","start":"2026-11-16T12:00:00","end":"2026-11-16T14:30:00"}
                ]}
                """;
        server.expect(request -> assertThat(request.getURI().toString()).contains("clist.by"))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        List<ContestDraft> drafts = feed("alice", "secret").fetch(FROM, TO);

        assertThat(drafts).singleElement().satisfies(draft -> {
            assertThat(draft.platform()).isEqualTo(ContestPlatform.LEETCODE);
            assertThat(draft.durationSeconds()).isEqualTo(9000L);
        });
    }

    @Test
    void doesNothingWhenCredentialsAreMissing() {
        assertThat(feed("", "").fetch(FROM, TO)).isEmpty();
        assertThat(feed("alice", "  ").fetch(FROM, TO)).isEmpty();
        server.verify();
    }

    @Test
    void propagatesProviderFailuresSoTheOrchestratorCanFallBack() {
        server.expect(request -> assertThat(request.getURI().toString()).contains("clist.by"))
                .andRespond(withServerError());

        org.assertj.core.api.Assertions
                .assertThatThrownBy(() -> feed("alice", "secret").fetch(FROM, TO))
                .isInstanceOf(RuntimeException.class);
    }

    private static String payload() {
        String[][] entries = {
                {"codeforces.com", "https://codeforces.com/contest/1"},
                {"leetcode.com", "https://leetcode.com/contest/weekly-contest-1/"},
                {"codechef.com", "https://www.codechef.com/START1"},
                {"atcoder.jp", "https://atcoder.jp/contests/abc1"},
                {"geeksforgeeks.org", "https://www.geeksforgeeks.org/events/1"},
                {"codingninjas.com", "https://www.naukri.com/code360/contests/1"},
                {"hackerrank.com", "https://www.hackerrank.com/contests/1"},
                {"hackerearth.com", "https://www.hackerearth.com/challenges/1"}
        };
        StringBuilder objects = new StringBuilder();
        for (int index = 0; index < entries.length; index++) {
            if (index > 0) {
                objects.append(',');
            }
            objects.append("""
                    {"id":%d,"event":"Contest %d","href":"%s","resource":"%s",
                     "start":"2026-11-16T12:00:00","end":"2026-11-16T14:00:00","duration":7200}
                    """.formatted(100 + index, index, entries[index][1], entries[index][0]));
        }
        return "{\"objects\":[" + objects + "]}";
    }
}

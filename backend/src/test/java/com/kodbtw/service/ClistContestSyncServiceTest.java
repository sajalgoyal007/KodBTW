package com.kodbtw.service;

import com.kodbtw.contest.ContestPlatform;
import com.kodbtw.entity.ContestEvent;
import com.kodbtw.repository.ContestEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class ClistContestSyncServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-15T06:00:00Z");
    private static final String CLIST_CONTAINS = "https://clist.by/api/v4/contest/";

    @Mock
    private ContestEventRepository repository;

    private RestClient.Builder builder;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        lenient().when(repository.findByExternalId(anyString())).thenReturn(Optional.empty());
        lenient().when(repository.save(any(ContestEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private ClistContestSyncService service(String username, String apiKey) {
        return new ClistContestSyncService(builder, repository, username, apiKey,
                "https://clist.by/api/v4", true, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private static String codeforcesJson(Instant start) {
        return """
                {"status":"OK","result":[
                  {"id":1001,"name":"Codeforces Round 999","phase":"BEFORE",
                   "durationSeconds":7200,"startTimeSeconds":%d}
                ]}
                """.formatted(start.getEpochSecond());
    }

    private static String clistJson(String... resources) {
        StringBuilder objects = new StringBuilder();
        for (int index = 0; index < resources.length; index++) {
            if (index > 0) {
                objects.append(',');
            }
            objects.append("""
                    {"id":%d,"event":"Contest %d","href":"https://example.com/contest/%d",
                     "resource":{"name":"%s"},
                     "start":"2026-11-16T12:00:00","end":"2026-11-16T14:00:00","duration":7200}
                    """.formatted(10 + index, index, index, resources[index]));
        }
        return "{\"objects\":[" + objects + "]}";
    }

    @Test
    void fallsBackToCodeforcesWhenClistCredentialsAreMissing() {
        Instant start = NOW.plus(Duration.ofDays(1));
        server.expect(requestTo(ClistContestSyncService.CODEFORCES_API_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(codeforcesJson(start), MediaType.APPLICATION_JSON));

        ClistContestSyncService.SyncResult result = service("", "").syncNow();

        assertThat(result.fallbackUsed()).isTrue();
        assertThat(result.fetched()).isEqualTo(1);
        assertThat(result.inserted()).isEqualTo(1);
        assertThat(result.updated()).isZero();
        server.verify();

        ArgumentCaptor<ContestEvent> captor = ArgumentCaptor.forClass(ContestEvent.class);
        verify(repository).save(captor.capture());
        ContestEvent saved = captor.getValue();
        assertThat(saved.getExternalId()).isEqualTo("CODEFORCES:1001");
        assertThat(saved.getPlatform()).isEqualTo("CODEFORCES");
        assertThat(saved.getTitle()).isEqualTo("Codeforces Round 999");
        assertThat(saved.getDurationSeconds()).isEqualTo(7200L);
        assertThat(saved.getStartTimeUtc())
                .isEqualTo(LocalDateTime.ofInstant(start, ZoneOffset.UTC));
        assertThat(saved.getContestUrl()).isEqualTo("https://codeforces.com/contest/1001");
    }

    @Test
    void syncsAllEightPlatformsFromClist() {
        server.expect(request -> {
                    String url = request.getURI().toString();
                    assertThat(url).startsWith(CLIST_CONTAINS);
                    assertThat(url).contains("resource=codeforces.com", "resource=codingninjas.com",
                            "resource=atcoder.jp", "resource=hackerearth.com");
                    assertThat(url).contains("start__gte=2026-11-08T06", "end__lte=2026-12-30T06");
                })
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "ApiKey alice:secret"))
                .andRespond(withSuccess(clistJson(
                        "codeforces.com", "leetcode.com", "codechef.com", "atcoder.jp",
                        "geeksforgeeks.org", "codingninjas.com", "hackerrank.com",
                        "hackerearth.com"), MediaType.APPLICATION_JSON));

        ClistContestSyncService.SyncResult result = service("alice", "secret").syncNow();

        assertThat(result.fallbackUsed()).isFalse();
        assertThat(result.fetched()).isEqualTo(8);
        assertThat(result.inserted()).isEqualTo(8);
        server.verify();

        ArgumentCaptor<ContestEvent> captor = ArgumentCaptor.forClass(ContestEvent.class);
        verify(repository, times(8)).save(captor.capture());
        assertThat(captor.getAllValues()).extracting(ContestEvent::getPlatform)
                .containsExactlyInAnyOrderElementsOf(
                        EnumSet.allOf(ContestPlatform.class).stream().map(Enum::name).toList());
        assertThat(captor.getAllValues()).extracting(ContestEvent::getContestUrl)
                .allMatch(url -> url.startsWith("https://"));
    }

    @Test
    void fallsBackToCodeforcesWhenClistFails() {
        Instant start = NOW.plus(Duration.ofDays(2));
        server.expect(request -> assertThat(request.getURI().toString()).startsWith(CLIST_CONTAINS))
                .andRespond(withServerError());
        server.expect(requestTo(ClistContestSyncService.CODEFORCES_API_URL))
                .andRespond(withSuccess(codeforcesJson(start), MediaType.APPLICATION_JSON));

        ClistContestSyncService.SyncResult result = service("alice", "secret").syncNow();

        assertThat(result.fallbackUsed()).isTrue();
        assertThat(result.inserted()).isEqualTo(1);
        server.verify();
    }

    @Test
    void updatesExistingRowsInsteadOfInsertingDuplicates() {
        ContestEvent existing = new ContestEvent();
        existing.setId(42L);
        existing.setExternalId("CODEFORCES:1001");
        existing.setTitle("Old title");
        when(repository.findByExternalId("CODEFORCES:1001")).thenReturn(Optional.of(existing));

        server.expect(requestTo(ClistContestSyncService.CODEFORCES_API_URL))
                .andRespond(withSuccess(codeforcesJson(NOW.plus(Duration.ofDays(1))),
                        MediaType.APPLICATION_JSON));

        ClistContestSyncService.SyncResult result = service("", "").syncNow();

        assertThat(result.inserted()).isZero();
        assertThat(result.updated()).isEqualTo(1);
        verify(repository).save(same(existing));
        assertThat(existing.getTitle()).isEqualTo("Codeforces Round 999");
        assertThat(existing.getLastSyncedAt()).isEqualTo(LocalDateTime.ofInstant(NOW, ZoneOffset.UTC));
    }

    @Test
    void rejectsNonHttpContestUrls() {
        String payload = """
                {"objects":[
                  {"id":1,"event":"Unsafe","href":"javascript:alert(1)",
                   "resource":{"name":"codeforces.com"},
                   "start":"2026-11-16T12:00:00","end":"2026-11-16T14:00:00","duration":7200}
                ]}
                """;
        server.expect(request -> assertThat(request.getURI().toString()).startsWith(CLIST_CONTAINS))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        ClistContestSyncService.SyncResult result = service("alice", "secret").syncNow();

        assertThat(result.fetched()).isZero();
        assertThat(result.inserted()).isZero();
        verify(repository, never()).save(any(ContestEvent.class));
    }

    @Test
    void ignoresUnsupportedResourcesAndEventsOutsideTheWindow() {
        String payload = """
                {"objects":[
                  {"id":1,"event":"Unknown site","href":"https://example.com/1",
                   "resource":{"name":"someothersite.com"},
                   "start":"2026-11-16T12:00:00","end":"2026-11-16T14:00:00","duration":7200},
                  {"id":2,"event":"Too far away","href":"https://codeforces.com/contest/2",
                   "resource":{"name":"codeforces.com"},
                   "start":"2027-03-01T12:00:00","end":"2027-03-01T14:00:00","duration":7200},
                  {"id":3,"event":"Already long finished","href":"https://codeforces.com/contest/3",
                   "resource":{"name":"codeforces.com"},
                   "start":"2026-06-01T12:00:00","end":"2026-06-01T14:00:00","duration":7200}
                ]}
                """;
        server.expect(request -> assertThat(request.getURI().toString()).startsWith(CLIST_CONTAINS))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        ClistContestSyncService.SyncResult result = service("alice", "secret").syncNow();

        assertThat(result.fetched()).isZero();
        verify(repository, never()).save(any(ContestEvent.class));
    }

    @Test
    void derivesMissingClistDurationFromStartAndEnd() {
        String payload = """
                {"objects":[
                  {"id":7,"event":"Two and a half hours","href":"https://leetcode.com/contest/weekly",
                   "resource":{"name":"leetcode.com"},
                   "start":"2026-11-16T12:00:00","end":"2026-11-16T14:30:00"}
                ]}
                """;
        server.expect(request -> assertThat(request.getURI().toString()).startsWith(CLIST_CONTAINS))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        ClistContestSyncService.SyncResult result = service("alice", "secret").syncNow();

        assertThat(result.fetched()).isEqualTo(1);
        ArgumentCaptor<ContestEvent> captor = ArgumentCaptor.forClass(ContestEvent.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getDurationSeconds()).isEqualTo(9000L);
        assertThat(captor.getValue().getPlatform()).isEqualTo("LEETCODE");
    }

    @Test
    void reportsWhetherClistCredentialsAreConfigured() {
        assertThat(service("alice", "secret").hasClistCredentials()).isTrue();
        assertThat(service("", "secret").hasClistCredentials()).isFalse();
        assertThat(service("alice", " ").hasClistCredentials()).isFalse();
    }
}

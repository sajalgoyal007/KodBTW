package com.kodbtw.service;

import com.kodbtw.contest.ContestDraft;
import com.kodbtw.contest.ContestFeed;
import com.kodbtw.contest.ContestPlatform;
import com.kodbtw.entity.ContestEvent;
import com.kodbtw.repository.ContestEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContestEventSyncServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-15T06:00:00Z");
    private static final Instant START = Instant.parse("2026-11-16T13:30:00Z");

    @Mock
    private ContestEventRepository repository;

    @BeforeEach
    void setUp() {
        lenient().when(repository.findByExternalId(anyString())).thenReturn(Optional.empty());
        lenient().when(repository.findFirstByPlatformAndStartTimeUtcAndDurationSeconds(
                        anyString(), any(LocalDateTime.class), anyLong()))
                .thenReturn(Optional.empty());
        lenient().when(repository.save(any(ContestEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private ContestEventSyncService service(ContestFeed... feeds) {
        return new ContestEventSyncService(List.of(feeds), repository, true,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private static ContestFeed feed(String source, int priority, List<ContestDraft> drafts) {
        ContestFeed feed = mock(ContestFeed.class);
        lenient().when(feed.source()).thenReturn(source);
        lenient().when(feed.priority()).thenReturn(priority);
        lenient().when(feed.isConfigured()).thenReturn(true);
        lenient().when(feed.fetch(any(), any())).thenReturn(drafts);
        return feed;
    }

    private static ContestDraft draft(String externalId, ContestPlatform platform, Instant start,
            long durationSeconds) {
        return new ContestDraft(externalId, platform, "Contest " + externalId, start,
                start.plusSeconds(durationSeconds), durationSeconds,
                "https://example.com/" + externalId);
    }

    @Test
    void aggregatesEveryPlatformFromMultipleFeeds() {
        ContestFeed clist = feed("CLIST", 0, List.of(
                draft("CLIST:1", ContestPlatform.CODEFORCES, START, 7200),
                draft("CLIST:2", ContestPlatform.LEETCODE, START.plusSeconds(3600), 5400),
                draft("CLIST:3", ContestPlatform.CODECHEF, START.plusSeconds(7200), 10800),
                draft("CLIST:4", ContestPlatform.ATCODER, START.plusSeconds(10800), 6000),
                draft("CLIST:5", ContestPlatform.GFG, START.plusSeconds(14400), 3600),
                draft("CLIST:6", ContestPlatform.CODE360, START.plusSeconds(18000), 3600),
                draft("CLIST:7", ContestPlatform.HACKERRANK, START.plusSeconds(21600), 5400),
                draft("CLIST:8", ContestPlatform.HACKEREARTH, START.plusSeconds(25200), 7200)));

        ContestEventSyncService.SyncReport report = service(clist).syncNow();

        assertThat(report.fetched()).isEqualTo(8);
        assertThat(report.inserted()).isEqualTo(8);
        assertThat(report.updated()).isZero();
        verify(repository, org.mockito.Mockito.times(8)).save(any(ContestEvent.class));
    }

    @Test
    void oneFailingFeedNeverBlocksOthersAndNeverDeletesExistingRows() {
        ContestFeed broken = feed("BROKEN", 0, List.of());
        when(broken.fetch(any(), any())).thenThrow(new IllegalStateException("provider down"));
        ContestFeed healthy = feed("HEALTHY", 10, List.of(
                draft("HEALTHY:1", ContestPlatform.ATCODER, START, 6000)));

        ContestEventSyncService.SyncReport report = service(broken, healthy).syncNow();

        assertThat(report.fetched()).isEqualTo(1);
        assertThat(report.inserted()).isEqualTo(1);
        assertThat(report.feeds()).extracting(ContestEventSyncService.FeedStatus::status)
                .containsExactly(ContestEventSyncService.FeedStatus.FAILED,
                        ContestEventSyncService.FeedStatus.OK);
        verify(repository, never()).deleteAll();
        verify(repository, never()).delete(any(ContestEvent.class));
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    void skipsUnconfiguredFeedsWithoutCallingThem() {
        ContestFeed unconfigured = mock(ContestFeed.class);
        when(unconfigured.source()).thenReturn("NO_CREDS");
        when(unconfigured.priority()).thenReturn(0);
        when(unconfigured.isConfigured()).thenReturn(false);
        ContestFeed healthy = feed("CLIST", 10, List.of(
                draft("CLIST:1", ContestPlatform.CODEFORCES, START, 7200)));

        ContestEventSyncService.SyncReport report = service(unconfigured, healthy).syncNow();

        verify(unconfigured, never()).fetch(any(), any());
        assertThat(report.feeds())
                .extracting(ContestEventSyncService.FeedStatus::source,
                        ContestEventSyncService.FeedStatus::status)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("NO_CREDS",
                                ContestEventSyncService.FeedStatus.SKIPPED),
                        org.assertj.core.groups.Tuple.tuple("CLIST",
                                ContestEventSyncService.FeedStatus.OK));
    }

    @Test
    void deduplicatesTheSameContestReportedByTwoFeeds() {
        ContestFeed clist = feed("CLIST", 0, List.of(
                draft("CLIST:1", ContestPlatform.CODEFORCES, START, 7200)));
        ContestFeed direct = feed("CODEFORCES_API", 10, List.of(
                draft("CODEFORCES:1001", ContestPlatform.CODEFORCES, START, 7200)));

        ContestEventSyncService.SyncReport report = service(clist, direct).syncNow();

        assertThat(report.fetched()).isEqualTo(1);
        assertThat(report.inserted()).isEqualTo(1);
        org.mockito.ArgumentCaptor<ContestEvent> captor =
                org.mockito.ArgumentCaptor.forClass(ContestEvent.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getExternalId()).isEqualTo("CLIST:1");
    }

    @Test
    void updatesAnExistingRowMatchedBySlotEvenWhenTheExternalIdChanged() {
        ContestEvent existing = new ContestEvent();
        existing.setId(42L);
        existing.setExternalId("CLIST:1");
        existing.setPlatform("CODEFORCES");
        existing.setTitle("Old title");
        when(repository.findByExternalId("CODEFORCES:1001")).thenReturn(Optional.empty());
        when(repository.findFirstByPlatformAndStartTimeUtcAndDurationSeconds(
                "CODEFORCES", LocalDateTime.ofInstant(START, ZoneOffset.UTC), 7200L))
                .thenReturn(Optional.of(existing));
        ContestFeed direct = feed("CODEFORCES_API", 10, List.of(
                draft("CODEFORCES:1001", ContestPlatform.CODEFORCES, START, 7200)));

        ContestEventSyncService.SyncReport report = service(direct).syncNow();

        assertThat(report.inserted()).isZero();
        assertThat(report.updated()).isEqualTo(1);
        verify(repository).save(same(existing));
        assertThat(existing.getExternalId()).isEqualTo("CLIST:1");
        assertThat(existing.getTitle()).isEqualTo("Contest CODEFORCES:1001");
        assertThat(existing.getLastSyncedAt()).isEqualTo(LocalDateTime.ofInstant(NOW, ZoneOffset.UTC));
    }

    @Test
    void updatesRowsMatchedByExternalId() {
        ContestEvent existing = new ContestEvent();
        existing.setId(7L);
        existing.setExternalId("CLIST:1");
        when(repository.findByExternalId("CLIST:1")).thenReturn(Optional.of(existing));
        ContestFeed clist = feed("CLIST", 0, List.of(
                draft("CLIST:1", ContestPlatform.CODEFORCES, START, 7200)));

        ContestEventSyncService.SyncReport report = service(clist).syncNow();

        assertThat(report.inserted()).isZero();
        assertThat(report.updated()).isEqualTo(1);
        verify(repository).save(same(existing));
    }
}

package com.kodbtw.service;

import com.kodbtw.dto.contest.ContestEventDto;
import com.kodbtw.dto.contest.ContestMonthCalendarResponse;
import com.kodbtw.entity.ContestEvent;
import com.kodbtw.repository.ContestEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContestEventQueryServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-15T12:00:00Z");

    @Mock
    private ContestEventRepository repository;

    private ContestEventQueryService service(Instant now) {
        return new ContestEventQueryService(repository, Clock.fixed(now, ZoneOffset.UTC));
    }

    private static ContestEvent event(long id, String platform, String title,
            Instant start, Instant end) {
        ContestEvent event = new ContestEvent();
        event.setId(id);
        event.setExternalId("TEST:" + id);
        event.setPlatform(platform);
        event.setTitle(title);
        event.setStartTimeUtc(LocalDateTime.ofInstant(start, ZoneOffset.UTC));
        event.setEndTimeUtc(LocalDateTime.ofInstant(end, ZoneOffset.UTC));
        event.setDurationSeconds(Duration.between(start, end).getSeconds());
        event.setContestUrl("https://example.com/contest/" + id);
        event.setLastSyncedAt(LocalDateTime.ofInstant(NOW, ZoneOffset.UTC));
        return event;
    }

    @Test
    void computesStatusesDisplayNamesAndCalendarMetadata() {
        when(repository
                .findByStartTimeUtcGreaterThanEqualAndStartTimeUtcLessThanOrderByStartTimeUtcAsc(
                        any(), any()))
                .thenReturn(List.of(
                        event(1, "CODEFORCES", "Upcoming Round",
                                NOW.plus(Duration.ofHours(1)), NOW.plus(Duration.ofHours(3))),
                        event(2, "LEETCODE", "Weekly Contest",
                                NOW.minus(Duration.ofHours(1)), NOW.plus(Duration.ofHours(1))),
                        event(3, "GFG", "Practice Contest",
                                NOW.minus(Duration.ofHours(3)), NOW.minus(Duration.ofHours(2)))));

        ContestMonthCalendarResponse response = service(NOW).getMonthCalendar(2026, 11, List.of());

        assertThat(response.year()).isEqualTo(2026);
        assertThat(response.month()).isEqualTo(11);
        assertThat(response.timeZone()).isEqualTo("Asia/Kolkata");
        assertThat(response.generatedAt()).isEqualTo(NOW);
        assertThat(response.contests()).hasSize(3);
        assertThat(response.contests())
                .extracting(ContestEventDto::platform, ContestEventDto::status,
                        ContestEventDto::platformDisplayName)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("CODEFORCES", "UPCOMING", "Codeforces"),
                        org.assertj.core.groups.Tuple.tuple("LEETCODE", "ONGOING", "LeetCode"),
                        org.assertj.core.groups.Tuple.tuple("GFG", "COMPLETED", "GeeksforGeeks"));
    }

    @Test
    void resolvesMonthBoundariesInIst() {
        Instant justAfterIstMidnight = Instant.parse("2026-10-31T20:00:00Z");

        service(justAfterIstMidnight).getMonthCalendar(null, null, null);

        verify(repository)
                .findByStartTimeUtcGreaterThanEqualAndStartTimeUtcLessThanOrderByStartTimeUtcAsc(
                        LocalDateTime.of(2026, 10, 31, 18, 30),
                        LocalDateTime.of(2026, 11, 30, 18, 30));
    }

    @Test
    void filtersByPlatformAndAcceptsAllAndAliases() {
        when(repository
                .findByStartTimeUtcGreaterThanEqualAndStartTimeUtcLessThanOrderByStartTimeUtcAsc(
                        any(), any()))
                .thenReturn(List.of(
                        event(1, "CODEFORCES", "CF", NOW.plus(Duration.ofHours(1)),
                                NOW.plus(Duration.ofHours(2))),
                        event(2, "LEETCODE", "LC", NOW.plus(Duration.ofHours(1)),
                                NOW.plus(Duration.ofHours(2))),
                        event(3, "GFG", "GFG", NOW.plus(Duration.ofHours(1)),
                                NOW.plus(Duration.ofHours(2)))));

        ContestEventQueryService service = service(NOW);

        assertThat(service.getMonthCalendar(2026, 11, List.of("LEETCODE")).contests())
                .extracting(ContestEventDto::platform).containsExactly("LEETCODE");
        assertThat(service.getMonthCalendar(2026, 11, List.of("CODEFORCES,GFG")).contests())
                .extracting(ContestEventDto::platform).containsExactly("CODEFORCES", "GFG");
        assertThat(service.getMonthCalendar(2026, 11, List.of("ALL")).contests()).hasSize(3);
        assertThat(service.getMonthCalendar(2026, 11, List.of("GEEKSFORGEEKS")).contests())
                .extracting(ContestEventDto::platform).containsExactly("GFG");
    }

    @Test
    void fillsMissingYearOrMonthFromTheCurrentIstMonth() {
        when(repository
                .findByStartTimeUtcGreaterThanEqualAndStartTimeUtcLessThanOrderByStartTimeUtcAsc(
                        any(), any()))
                .thenReturn(List.of());

        ContestMonthCalendarResponse response = service(NOW).getMonthCalendar(2024, null, null);

        assertThat(response.year()).isEqualTo(2024);
        assertThat(response.month()).isEqualTo(11);
    }

    @Test
    void rejectsInvalidMonthYearAndPlatform() {
        ContestEventQueryService service = service(NOW);

        assertThatThrownBy(() -> service.getMonthCalendar(2026, 13, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("month");
        assertThatThrownBy(() -> service.getMonthCalendar(1999, 1, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("year");
        assertThatThrownBy(() -> service.getMonthCalendar(2026, 1, List.of("HACKERX")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported contest platform");
    }

    @Test
    void blanksUnsafeContestUrlsEvenIfStoredDataIsCompromised() {
        ContestEvent unsafe = event(9, "CODEFORCES", "Unsafe", NOW.plus(Duration.ofHours(1)),
                NOW.plus(Duration.ofHours(2)));
        unsafe.setContestUrl("javascript:alert(1)");
        when(repository
                .findByStartTimeUtcGreaterThanEqualAndStartTimeUtcLessThanOrderByStartTimeUtcAsc(
                        any(), any()))
                .thenReturn(List.of(unsafe));

        ContestMonthCalendarResponse response = service(NOW).getMonthCalendar(2026, 11, null);

        assertThat(response.contests()).singleElement()
                .extracting(ContestEventDto::contestUrl).isEqualTo("");
    }
}

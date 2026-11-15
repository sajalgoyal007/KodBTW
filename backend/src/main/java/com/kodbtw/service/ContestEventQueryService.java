package com.kodbtw.service;

import com.kodbtw.contest.ContestPlatform;
import com.kodbtw.contest.ContestUrls;
import com.kodbtw.dto.contest.ContestEventDto;
import com.kodbtw.dto.contest.ContestMonthCalendarResponse;
import com.kodbtw.entity.ContestEvent;
import com.kodbtw.repository.ContestEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Read model for the monthly contest calendar.
 *
 * <p>Month boundaries are resolved in {@code Asia/Kolkata} (IST) because the
 * calendar is IST-first, while every stored/returned instant stays in UTC so
 * clients can render it in the user's own timezone.</p>
 */
@Service
public class ContestEventQueryService {

    public static final ZoneId CALENDAR_ZONE = ZoneId.of("Asia/Kolkata");
    private static final int MIN_YEAR = 2000;
    private static final int MAX_YEAR = 2100;

    private final ContestEventRepository repository;
    private final Clock clock;

    @Autowired
    public ContestEventQueryService(ContestEventRepository repository) {
        this(repository, Clock.systemUTC());
    }

    ContestEventQueryService(ContestEventRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public ContestMonthCalendarResponse getMonthCalendar(Integer year, Integer month,
            List<String> platforms) {
        YearMonth selected = resolveMonth(year, month);
        Set<ContestPlatform> platformFilter = parsePlatforms(platforms);

        Instant from = selected.atDay(1).atStartOfDay(CALENDAR_ZONE).toInstant();
        Instant to = selected.plusMonths(1).atDay(1).atStartOfDay(CALENDAR_ZONE).toInstant();

        List<ContestEvent> events =
                repository.findByStartTimeUtcGreaterThanEqualAndStartTimeUtcLessThanOrderByStartTimeUtcAsc(
                        LocalDateTime.ofInstant(from, ZoneOffset.UTC),
                        LocalDateTime.ofInstant(to, ZoneOffset.UTC));

        Instant now = clock.instant();
        List<ContestEventDto> contests = events.stream()
                .filter(event -> platformFilter.isEmpty()
                        || ContestPlatform.fromCode(event.getPlatform())
                                .map(platformFilter::contains)
                                .orElse(false))
                .map(event -> toDto(event, now))
                .toList();

        return new ContestMonthCalendarResponse(selected.getYear(), selected.getMonthValue(),
                CALENDAR_ZONE.getId(), now, contests);
    }

    private YearMonth resolveMonth(Integer year, Integer month) {
        YearMonth current = YearMonth.now(clock.withZone(CALENDAR_ZONE));
        int resolvedYear = year == null ? current.getYear() : year;
        int resolvedMonth = month == null ? current.getMonthValue() : month;

        if (resolvedYear < MIN_YEAR || resolvedYear > MAX_YEAR) {
            throw new IllegalArgumentException(
                    "Contest calendar year must be between " + MIN_YEAR + " and " + MAX_YEAR + ".");
        }
        if (resolvedMonth < 1 || resolvedMonth > 12) {
            throw new IllegalArgumentException("Contest calendar month must be between 1 and 12.");
        }
        return YearMonth.of(resolvedYear, resolvedMonth);
    }

    private Set<ContestPlatform> parsePlatforms(List<String> platforms) {
        if (platforms == null || platforms.isEmpty()) {
            return EnumSet.noneOf(ContestPlatform.class);
        }
        Set<ContestPlatform> selected = EnumSet.noneOf(ContestPlatform.class);
        for (String value : platforms) {
            if (value == null) {
                continue;
            }
            for (String token : value.split(",")) {
                String candidate = token.trim();
                if (candidate.isEmpty()) {
                    continue;
                }
                if (candidate.equalsIgnoreCase("ALL")) {
                    return EnumSet.noneOf(ContestPlatform.class);
                }
                ContestPlatform platform = ContestPlatform.fromCode(candidate)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Unsupported contest platform: " + candidate));
                selected.add(platform);
            }
        }
        return selected;
    }

    private ContestEventDto toDto(ContestEvent event, Instant now) {
        Instant start = event.getStartTimeUtc().toInstant(ZoneOffset.UTC);
        Instant end = event.getEndTimeUtc().toInstant(ZoneOffset.UTC);
        String platformCode = event.getPlatform() == null
                ? ""
                : event.getPlatform().toUpperCase(Locale.ROOT);
        String displayName = ContestPlatform.fromCode(platformCode)
                .map(ContestPlatform::displayName)
                .orElse(platformCode);
        String status = now.isBefore(start) ? "UPCOMING"
                : now.isBefore(end) ? "ONGOING" : "COMPLETED";
        String contestUrl = ContestUrls.isSafeHttpUrl(event.getContestUrl())
                ? event.getContestUrl().trim() : "";
        return new ContestEventDto(
                String.valueOf(event.getId()),
                platformCode,
                displayName,
                event.getTitle(),
                start,
                end,
                event.getDurationSeconds(),
                contestUrl,
                status);
    }
}

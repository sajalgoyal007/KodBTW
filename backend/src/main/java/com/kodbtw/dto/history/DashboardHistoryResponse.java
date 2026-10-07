package com.kodbtw.dto.history;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record DashboardHistoryResponse(
        String range,
        List<LocalDate> snapshotDates,
        List<OverallHistoryPointDto> overallSolved,
        List<PlatformHistorySeriesDto> platforms,
        HistoryFreshnessDto freshness
) {
    public record OverallHistoryPointDto(
            LocalDate date,
            Integer totalSolved,
            Integer easySolved,
            Integer mediumSolved,
            Integer hardSolved,
            int observedPlatforms,
            int platformsWithTotalSolved,
            int platformsWithEasySolved,
            int platformsWithMediumSolved,
            int platformsWithHardSolved
    ) {}

    public record PlatformHistorySeriesDto(String platform, List<PlatformHistoryPointDto> points) {}

    public record PlatformHistoryPointDto(
            LocalDate date,
            Integer totalSolved,
            Integer totalSolvedChange,
            Integer easySolved,
            Integer easySolvedChange,
            Integer mediumSolved,
            Integer mediumSolvedChange,
            Integer hardSolved,
            Integer hardSolvedChange,
            Integer rating
    ) {}

    public record HistoryFreshnessDto(
            int snapshotCount,
            LocalDate latestSnapshotDate,
            LocalDateTime lastUpdatedAt,
            Long ageHours
    ) {}
}

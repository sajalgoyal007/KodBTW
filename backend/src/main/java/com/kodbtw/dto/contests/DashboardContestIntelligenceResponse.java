package com.kodbtw.dto.contests;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record DashboardContestIntelligenceResponse(
        LocalDateTime generatedAt,
        boolean realDataOnly,
        boolean contestHistoryAvailable,
        String contestHistoryMessage,
        int ratingObservationCount,
        LocalDate oldestObservationDate,
        LocalDate latestObservationDate,
        LocalDateTime lastUpdatedAt,
        List<PlatformRatingHistory> platforms
) {
    public record PlatformRatingHistory(
            String platform,
            int observationCount,
            Integer currentRating,
            LocalDate latestSnapshotDate,
            Integer highestObservedRating,
            Integer lowestObservedRating,
            Integer change,
            LocalDate firstObservationDate,
            LocalDate latestObservationDate,
            List<RatingObservation> observations
    ) {}

    public record RatingObservation(LocalDate date, Integer rating) {}
}

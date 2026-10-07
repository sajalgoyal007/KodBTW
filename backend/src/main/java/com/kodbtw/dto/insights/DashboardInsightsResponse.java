package com.kodbtw.dto.insights;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record DashboardInsightsResponse(
        LocalDateTime generatedAt,
        DataAvailability dataAvailability,
        String summary,
        List<Insight> strengths,
        List<Insight> areasToImprove,
        List<Insight> platformInsights,
        List<Insight> progressInsights,
        List<Insight> difficultyInsights,
        List<Insight> ratingInsights,
        List<Insight> streakInsights
) {
    public record DataAvailability(
            int snapshotCount,
            int platformCount,
            LocalDate oldestSnapshotDate,
            LocalDate latestSnapshotDate,
            boolean hasHistoricalComparison,
            boolean realDataOnly
    ) {}

    public record Insight(
            String type,
            String category,
            String severity,
            String title,
            String message,
            String platform,
            String metric,
            BigDecimal value,
            BigDecimal previousValue,
            BigDecimal change,
            LocalDate date,
            LocalDate comparisonDate,
            Map<String, BigDecimal> supportingValues
    ) {}
}

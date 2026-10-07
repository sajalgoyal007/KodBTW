package com.kodbtw.service;

import com.kodbtw.dto.history.DashboardHistoryResponse;
import com.kodbtw.entity.PlatformStatSnapshot;
import com.kodbtw.repository.PlatformStatSnapshotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.kodbtw.dto.history.DashboardHistoryResponse.HistoryFreshnessDto;
import static com.kodbtw.dto.history.DashboardHistoryResponse.OverallHistoryPointDto;
import static com.kodbtw.dto.history.DashboardHistoryResponse.PlatformHistoryPointDto;
import static com.kodbtw.dto.history.DashboardHistoryResponse.PlatformHistorySeriesDto;

@Service
public class DashboardHistoryService {
    private final PlatformStatSnapshotRepository snapshots;

    public DashboardHistoryService(PlatformStatSnapshotRepository snapshots) {
        this.snapshots = snapshots;
    }

    @Transactional(readOnly = true)
    public DashboardHistoryResponse getHistory(Long userId, String requestedRange) {
        HistoryRange range = HistoryRange.parse(requestedRange);
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate startDate = range.startDate(today);

        List<PlatformStatSnapshot> rows = snapshots
                .findAllByUserIdAndSnapshotDateGreaterThanEqualAndSnapshotDateLessThanEqualAndSourceEndingWithOrderBySnapshotDateAscPlatformAscSnapshottedAtAsc(
                        userId, startDate, today, "_REAL");

        // Defensive source filtering keeps MOCK and any unexpected source out of real history.
        TreeMap<LocalDate, TreeMap<String, PlatformStatSnapshot>> byDate = new TreeMap<>();
        TreeMap<String, TreeMap<LocalDate, PlatformStatSnapshot>> byPlatform = new TreeMap<>();
        for (PlatformStatSnapshot row : rows) {
            if (!isReal(row) || row.getSnapshotDate() == null || row.getPlatform() == null) continue;
            byDate.computeIfAbsent(row.getSnapshotDate(), ignored -> new TreeMap<>())
                    .merge(row.getPlatform(), row, DashboardHistoryService::laterObservation);
            byPlatform.computeIfAbsent(row.getPlatform(), ignored -> new TreeMap<>())
                    .merge(row.getSnapshotDate(), row, DashboardHistoryService::laterObservation);
        }

        List<LocalDate> dates = new ArrayList<>(byDate.keySet());
        List<OverallHistoryPointDto> overall = byDate.entrySet().stream()
                .map(entry -> aggregate(entry.getKey(), entry.getValue()))
                .toList();
        List<PlatformHistorySeriesDto> platformSeries = byPlatform.entrySet().stream()
                .map(entry -> toSeries(entry.getKey(), entry.getValue()))
                .toList();

        return new DashboardHistoryResponse(requestedRange, dates, overall, platformSeries,
                freshness(byDate, rows));
    }

    private static boolean isReal(PlatformStatSnapshot row) {
        return row.getSource() != null && row.getSource().endsWith("_REAL");
    }

    private static PlatformStatSnapshot laterObservation(PlatformStatSnapshot first, PlatformStatSnapshot second) {
        LocalDateTime firstAt = first.getSnapshottedAt();
        LocalDateTime secondAt = second.getSnapshottedAt();
        if (firstAt == null) return second;
        if (secondAt == null) return first;
        int byTime = firstAt.compareTo(secondAt);
        if (byTime != 0) return byTime < 0 ? second : first;
        return first.getId() == null || second.getId() == null || first.getId() <= second.getId() ? second : first;
    }

    private OverallHistoryPointDto aggregate(LocalDate date, Map<String, PlatformStatSnapshot> rows) {
        int totalCoverage = 0;
        int easyCoverage = 0;
        int mediumCoverage = 0;
        int hardCoverage = 0;
        Integer total = null;
        Integer easy = null;
        Integer medium = null;
        Integer hard = null;
        for (PlatformStatSnapshot row : rows.values()) {
            if (row.getTotalSolved() != null) { total = add(total, row.getTotalSolved()); totalCoverage++; }
            if (row.getEasySolved() != null) { easy = add(easy, row.getEasySolved()); easyCoverage++; }
            if (row.getMediumSolved() != null) { medium = add(medium, row.getMediumSolved()); mediumCoverage++; }
            if (row.getHardSolved() != null) { hard = add(hard, row.getHardSolved()); hardCoverage++; }
        }
        return new OverallHistoryPointDto(date, total, easy, medium, hard, rows.size(), totalCoverage,
                easyCoverage, mediumCoverage, hardCoverage);
    }

    private static Integer add(Integer current, Integer value) {
        return current == null ? value : current + value;
    }

    private PlatformHistorySeriesDto toSeries(String platform, TreeMap<LocalDate, PlatformStatSnapshot> observations) {
        List<PlatformHistoryPointDto> points = new ArrayList<>();
        Integer previousTotal = null, previousEasy = null, previousMedium = null, previousHard = null;
        for (Map.Entry<LocalDate, PlatformStatSnapshot> entry : observations.entrySet()) {
            PlatformStatSnapshot row = entry.getValue();
            Integer totalChange = change(row.getTotalSolved(), previousTotal);
            Integer easyChange = change(row.getEasySolved(), previousEasy);
            Integer mediumChange = change(row.getMediumSolved(), previousMedium);
            Integer hardChange = change(row.getHardSolved(), previousHard);
            points.add(new PlatformHistoryPointDto(entry.getKey(), row.getTotalSolved(), totalChange,
                    row.getEasySolved(), easyChange, row.getMediumSolved(), mediumChange,
                    row.getHardSolved(), hardChange, row.getRating()));
            if (row.getTotalSolved() != null) previousTotal = row.getTotalSolved();
            if (row.getEasySolved() != null) previousEasy = row.getEasySolved();
            if (row.getMediumSolved() != null) previousMedium = row.getMediumSolved();
            if (row.getHardSolved() != null) previousHard = row.getHardSolved();
        }
        return new PlatformHistorySeriesDto(platform, points);
    }

    private static Integer change(Integer current, Integer previous) {
        return current == null || previous == null ? null : current - previous;
    }

    private HistoryFreshnessDto freshness(TreeMap<LocalDate, TreeMap<String, PlatformStatSnapshot>> byDate,
                                          List<PlatformStatSnapshot> rows) {
        if (byDate.isEmpty()) return new HistoryFreshnessDto(0, null, null, null);
        LocalDate latestDate = byDate.lastKey();
        LocalDateTime latestUpdate = rows.stream().filter(DashboardHistoryService::isReal)
                .map(row -> row.getStatsLastSyncedAt() != null ? row.getStatsLastSyncedAt() : row.getSnapshottedAt())
                .filter(value -> value != null)
                .max(Comparator.naturalOrder()).orElse(null);
        Long ageHours = latestUpdate == null ? null : Math.max(0, Duration.between(latestUpdate,
                LocalDateTime.now(ZoneOffset.UTC)).toHours());
        int count = (int) rows.stream().filter(DashboardHistoryService::isReal).count();
        return new HistoryFreshnessDto(count, latestDate, latestUpdate, ageHours);
    }

    private enum HistoryRange {
        DAYS_7("7d", 6), DAYS_30("30d", 29), DAYS_90("90d", 89), YEAR_1("1y", -1);

        private final String label;
        private final int days;

        HistoryRange(String label, int days) { this.label = label; this.days = days; }

        LocalDate startDate(LocalDate today) {
            return days < 0 ? today.minusYears(1) : today.minusDays(days);
        }

        static HistoryRange parse(String value) {
            for (HistoryRange range : values()) if (range.label.equals(value)) return range;
            throw new IllegalArgumentException("Unsupported history range. Use 7d, 30d, 90d, or 1y.");
        }
    }
}

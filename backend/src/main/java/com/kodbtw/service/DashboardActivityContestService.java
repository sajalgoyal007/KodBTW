package com.kodbtw.service;

import com.kodbtw.dto.activity.DashboardActivityResponse;
import com.kodbtw.dto.contests.DashboardContestIntelligenceResponse;
import com.kodbtw.dto.contests.DashboardContestIntelligenceResponse.PlatformRatingHistory;
import com.kodbtw.dto.contests.DashboardContestIntelligenceResponse.RatingObservation;
import com.kodbtw.entity.PlatformStatSnapshot;
import com.kodbtw.repository.PlatformStatSnapshotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

@Service
public class DashboardActivityContestService {
    private static final String ACTIVITY_UNAVAILABLE =
            "Activity history is not available from the connected platform yet.";
    private static final String CONTESTS_UNAVAILABLE =
            "Contest history is not available from the connected platform yet.";

    private final PlatformStatSnapshotRepository snapshots;

    public DashboardActivityContestService(PlatformStatSnapshotRepository snapshots) {
        this.snapshots = snapshots;
    }

    @Transactional(readOnly = true)
    public DashboardActivityResponse getActivity(Long userId) {
        List<String> platforms = snapshots.findAllByUserId(userId).stream()
                .filter(DashboardActivityContestService::isReal)
                .filter(row -> row.getSnapshotDate() != null)
                .map(PlatformStatSnapshot::getPlatform)
                .filter(platform -> platform != null && !platform.isBlank())
                .distinct()
                .sorted()
                .toList();
        return new DashboardActivityResponse(LocalDateTime.now(ZoneOffset.UTC), false, true,
                ACTIVITY_UNAVAILABLE, platforms);
    }

    @Transactional(readOnly = true)
    public DashboardContestIntelligenceResponse getContestIntelligence(Long userId) {
        List<PlatformStatSnapshot> rows = snapshots.findAllByUserId(userId).stream()
                .filter(DashboardActivityContestService::isReal)
                .filter(row -> row.getPlatform() != null && row.getSnapshotDate() != null)
                .toList();

        TreeMap<String, TreeMap<LocalDate, PlatformStatSnapshot>> grouped = new TreeMap<>();
        for (PlatformStatSnapshot row : rows) {
            grouped.computeIfAbsent(row.getPlatform(), ignored -> new TreeMap<>())
                    .merge(row.getSnapshotDate(), row, DashboardActivityContestService::laterSnapshot);
        }

        List<PlatformRatingHistory> platformHistories = new ArrayList<>();
        List<LocalDate> dates = new ArrayList<>();
        int observationCount = 0;
        LocalDateTime lastUpdatedAt = null;
        for (Map.Entry<String, TreeMap<LocalDate, PlatformStatSnapshot>> entry : grouped.entrySet()) {
            List<RatingObservation> observations = entry.getValue().entrySet().stream()
                    .filter(point -> point.getValue().getRating() != null)
                    .map(point -> new RatingObservation(point.getKey(), point.getValue().getRating()))
                    .toList();
            if (observations.isEmpty()) continue;
            PlatformStatSnapshot latestSnapshot = entry.getValue().lastEntry().getValue();
            Integer current = latestSnapshot.getRating();
            int latestObservedRating = observations.get(observations.size() - 1).rating();
            int first = observations.get(0).rating();
            int high = observations.stream().mapToInt(RatingObservation::rating).max().orElse(latestObservedRating);
            int low = observations.stream().mapToInt(RatingObservation::rating).min().orElse(latestObservedRating);
            LocalDate firstDate = observations.get(0).date();
            LocalDate latestDate = observations.get(observations.size() - 1).date();
            dates.addAll(entry.getValue().keySet());
            observationCount += observations.size();
            for (PlatformStatSnapshot snapshot : entry.getValue().values()) {
                LocalDateTime updatedAt = snapshot.getStatsLastSyncedAt() != null
                        ? snapshot.getStatsLastSyncedAt() : snapshot.getSnapshottedAt();
                if (updatedAt != null && (lastUpdatedAt == null || updatedAt.isAfter(lastUpdatedAt))) {
                    lastUpdatedAt = updatedAt;
                }
            }
            platformHistories.add(new PlatformRatingHistory(entry.getKey(), observations.size(), current,
                    latestSnapshot.getSnapshotDate(), high, low, observations.size() < 2 ? null : latestObservedRating - first,
                    firstDate, latestDate, observations));
        }

        LocalDate oldest = dates.stream().min(Comparator.naturalOrder()).orElse(null);
        LocalDate latest = dates.stream().max(Comparator.naturalOrder()).orElse(null);
        return new DashboardContestIntelligenceResponse(LocalDateTime.now(ZoneOffset.UTC), true, false,
                CONTESTS_UNAVAILABLE, observationCount, oldest, latest, lastUpdatedAt, platformHistories);
    }

    private static boolean isReal(PlatformStatSnapshot row) {
        if (row.getSource() == null) return false;
        String source = row.getSource().toUpperCase(Locale.ROOT);
        return source.endsWith("_REAL") && !source.contains("MOCK");
    }

    private static PlatformStatSnapshot laterSnapshot(PlatformStatSnapshot left, PlatformStatSnapshot right) {
        LocalDateTime leftAt = left.getSnapshottedAt();
        LocalDateTime rightAt = right.getSnapshottedAt();
        if (leftAt == null) return right;
        if (rightAt == null) return left;
        int byTime = leftAt.compareTo(rightAt);
        if (byTime != 0) return byTime < 0 ? right : left;
        if (left.getId() == null || right.getId() == null) return right;
        return left.getId() <= right.getId() ? right : left;
    }
}

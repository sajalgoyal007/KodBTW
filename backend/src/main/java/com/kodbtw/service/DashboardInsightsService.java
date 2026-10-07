package com.kodbtw.service;

import com.kodbtw.dto.insights.DashboardInsightsResponse;
import com.kodbtw.dto.insights.DashboardInsightsResponse.DataAvailability;
import com.kodbtw.dto.insights.DashboardInsightsResponse.Insight;
import com.kodbtw.entity.PlatformStatSnapshot;
import com.kodbtw.repository.PlatformStatSnapshotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.LinkedHashMap;

@Service
public class DashboardInsightsService {
    private final PlatformStatSnapshotRepository snapshots;

    public DashboardInsightsService(PlatformStatSnapshotRepository snapshots) {
        this.snapshots = snapshots;
    }

    @Transactional(readOnly = true)
    public DashboardInsightsResponse getInsights(Long userId) {
        List<PlatformStatSnapshot> realRows = snapshots.findAllByUserId(userId).stream()
                .filter(DashboardInsightsService::isReal)
                .filter(row -> row.getPlatform() != null && row.getSnapshotDate() != null)
                .toList();
        TreeMap<String, TreeMap<LocalDate, PlatformStatSnapshot>> history = groupAndDeduplicate(realRows);
        List<PlatformStatSnapshot> currentRows = history.values().stream()
                .map(TreeMap::lastEntry)
                .filter(entry -> entry != null)
                .map(Map.Entry::getValue)
                .toList();

        List<Insight> strengths = new ArrayList<>();
        List<Insight> areas = new ArrayList<>();
        List<Insight> platformInsights = new ArrayList<>();
        List<Insight> progressInsights = new ArrayList<>();
        List<Insight> difficultyInsights = new ArrayList<>();
        List<Insight> ratingInsights = new ArrayList<>();
        List<Insight> streakInsights = new ArrayList<>();

        String summary = summary(realRows.size(), currentRows);
        for (Map.Entry<String, TreeMap<LocalDate, PlatformStatSnapshot>> platform : history.entrySet()) {
            String platformName = platform.getKey();
            TreeMap<LocalDate, PlatformStatSnapshot> observations = platform.getValue();
            PlatformStatSnapshot current = observations.lastEntry().getValue();

            platformInsights.add(platformInsight(platformName, current));
            addProgressInsights(platformName, observations, progressInsights, strengths);
            addDifficultyInsights(platformName, current, difficultyInsights, areas);
            addRatingInsight(platformName, observations, ratingInsights);
            addStreakInsights(platformName, observations, streakInsights);
        }
        addCrossPlatformSolvedInsight(currentRows, strengths);

        List<LocalDate> dates = history.values().stream()
                .flatMap(values -> values.keySet().stream())
                .distinct()
                .sorted()
                .toList();
        boolean hasComparison = history.values().stream().anyMatch(DashboardInsightsService::hasComparableHistory);
        DataAvailability availability = new DataAvailability(
                realRows.size(), history.size(), dates.isEmpty() ? null : dates.get(0),
                dates.isEmpty() ? null : dates.get(dates.size() - 1), hasComparison, true);

        return new DashboardInsightsResponse(LocalDateTime.now(ZoneOffset.UTC), availability, summary,
                strengths, areas, platformInsights, progressInsights, difficultyInsights, ratingInsights, streakInsights);
    }

    private static TreeMap<String, TreeMap<LocalDate, PlatformStatSnapshot>> groupAndDeduplicate(
            List<PlatformStatSnapshot> rows) {
        TreeMap<String, TreeMap<LocalDate, PlatformStatSnapshot>> grouped = new TreeMap<>();
        for (PlatformStatSnapshot row : rows) {
            grouped.computeIfAbsent(row.getPlatform(), ignored -> new TreeMap<>())
                    .merge(row.getSnapshotDate(), row, DashboardInsightsService::laterObservation);
        }
        return grouped;
    }

    private static boolean isReal(PlatformStatSnapshot row) {
        if (row.getSource() == null) return false;
        String source = row.getSource().toUpperCase(Locale.ROOT);
        return source.endsWith("_REAL") && !source.contains("MOCK");
    }

    private static PlatformStatSnapshot laterObservation(PlatformStatSnapshot left, PlatformStatSnapshot right) {
        LocalDateTime leftAt = left.getSnapshottedAt();
        LocalDateTime rightAt = right.getSnapshottedAt();
        if (leftAt == null) return right;
        if (rightAt == null) return left;
        int byTime = leftAt.compareTo(rightAt);
        if (byTime != 0) return byTime < 0 ? right : left;
        if (left.getId() == null || right.getId() == null) return right;
        return left.getId() <= right.getId() ? right : left;
    }

    private static String summary(int rawSnapshotCount, List<PlatformStatSnapshot> currentRows) {
        if (rawSnapshotCount == 0) return "No verified platform snapshots are available yet.";
        long total = 0;
        int platformsWithTotal = 0;
        for (PlatformStatSnapshot row : currentRows) {
            if (row.getTotalSolved() != null) {
                total += row.getTotalSolved();
                platformsWithTotal++;
            }
        }
        if (platformsWithTotal == 0) {
            return "Verified snapshots are available for " + currentRows.size()
                    + " platform(s), but no current total-solved value is recorded.";
        }
        String prefix = currentRows.size() == 1
                ? "Current verified baseline: "
                : "Latest verified snapshots report ";
        return prefix + total + " problems solved across " + platformsWithTotal
                + " platform(s). Values reflect each platform's latest recorded snapshot.";
    }

    private static Insight platformInsight(String platform, PlatformStatSnapshot row) {
        List<String> metrics = new ArrayList<>();
        Map<String, BigDecimal> supporting = new LinkedHashMap<>();
        if (row.getTotalSolved() != null) { metrics.add(row.getTotalSolved() + " problems solved"); supporting.put("totalSolved", decimal(row.getTotalSolved())); }
        if (row.getRating() != null) { metrics.add("rating " + row.getRating()); supporting.put("rating", decimal(row.getRating())); }
        if (row.getCurrentStreak() != null) { metrics.add("current streak " + row.getCurrentStreak() + " days"); supporting.put("currentStreak", decimal(row.getCurrentStreak())); }
        if (row.getLongestStreak() != null) supporting.put("longestStreak", decimal(row.getLongestStreak()));
        if (row.getRank() != null) { metrics.add("rank " + row.getRank()); supporting.put("rank", decimal(row.getRank())); }
        if (row.getContests() != null) supporting.put("contests", decimal(row.getContests()));
        String detail = metrics.isEmpty() ? "No supported current metrics are recorded." : String.join(", ", metrics) + ".";
        return insight("PLATFORM_CURRENT", "PLATFORM", "INFO", platform + " verified snapshot", detail,
                platform, "CURRENT_SNAPSHOT", decimal(row.getTotalSolved()), null, null,
                row.getSnapshotDate(), null, supporting);
    }

    private static void addProgressInsights(String platform, TreeMap<LocalDate, PlatformStatSnapshot> observations,
                                            List<Insight> target, List<Insight> strengths) {
        List<Map.Entry<LocalDate, PlatformStatSnapshot>> values = new ArrayList<>(observations.entrySet());
        Map.Entry<LocalDate, PlatformStatSnapshot> currentEntry = values.get(values.size() - 1);
        if (currentEntry.getValue().getTotalSolved() == null) return;
        Map.Entry<LocalDate, PlatformStatSnapshot> previousEntry = previousWith(values, currentEntry.getKey(), Metric.TOTAL_SOLVED);
        Integer current = currentEntry.getValue().getTotalSolved();
        if (previousEntry == null) {
            target.add(insight("PROGRESS_BASELINE", "PROGRESS", "INFO", platform + " solved baseline",
                    "Current verified baseline: " + current + " problems solved. A trend is unavailable until another observation is recorded.",
                    platform, "TOTAL_SOLVED", decimal(current), null, null, currentEntry.getKey(), null));
            return;
        }
        Integer previous = previousEntry.getValue().getTotalSolved();
        int change = current - previous;
        if (change == 0) {
            target.add(insight("PROGRESS_UNCHANGED", "PROGRESS", "INFO", platform + " solved count unchanged",
                    "The two latest comparable observations both report " + current + " problems solved.",
                    platform, "TOTAL_SOLVED", decimal(current), decimal(previous), decimal(change),
                    currentEntry.getKey(), previousEntry.getKey()));
        } else if (change > 0) {
            Insight item = insight("PROGRESS_INCREASE", "PROGRESS", "POSITIVE", platform + " recorded progress",
                    "Problems solved increased from " + previous + " to " + current + " (+" + change
                            + ") between recorded observations.", platform, "TOTAL_SOLVED", decimal(current),
                    decimal(previous), decimal(change), currentEntry.getKey(), previousEntry.getKey());
            target.add(item);
            strengths.add(item);
        } else {
            target.add(insight("PROGRESS_CORRECTION", "PROGRESS", "INFO", platform + " reported a lower count",
                    "Problems solved changed from " + previous + " to " + current + " (" + change
                            + ") between recorded observations. The provider correction is preserved as reported.",
                    platform, "TOTAL_SOLVED", decimal(current), decimal(previous), decimal(change),
                    currentEntry.getKey(), previousEntry.getKey()));
        }
    }

    private static void addDifficultyInsights(String platform, PlatformStatSnapshot row,
                                              List<Insight> target, List<Insight> areas) {
        Integer easy = row.getEasySolved(), medium = row.getMediumSolved(), hard = row.getHardSolved();
        if (easy == null && medium == null && hard == null) return;
        if (easy == null || medium == null || hard == null) {
            target.add(insight("DIFFICULTY_INCOMPLETE", "DIFFICULTY", "INFO", platform + " difficulty data is partial",
                    "A complete difficulty distribution is unavailable, so percentages and category comparisons are withheld.",
                    platform, "DIFFICULTY_DISTRIBUTION", null, null, null, row.getSnapshotDate(), null));
            return;
        }
        Integer total = row.getTotalSolved();
        if (total == null || total <= 0) {
            target.add(insight("DIFFICULTY_NO_DENOMINATOR", "DIFFICULTY", "INFO", platform + " difficulty percentages unavailable",
                    total != null && total == 0
                            ? "The latest snapshot reports zero total solved; percentages are not calculated."
                            : "Total solved is not available; percentages are not calculated.",
                    platform, "DIFFICULTY_DISTRIBUTION", null, null, null, row.getSnapshotDate(), null));
            return;
        }
        BigDecimal easyPct = percent(easy, total), mediumPct = percent(medium, total), hardPct = percent(hard, total);
        String distribution = "Latest recorded counts: easy " + easy + " (" + easyPct.toPlainString()
                + "%), medium " + medium + " (" + mediumPct.toPlainString() + "%), hard " + hard
                + " (" + hardPct.toPlainString() + "%).";
        Map<String, BigDecimal> distributionValues = new LinkedHashMap<>();
        distributionValues.put("totalSolved", decimal(total));
        distributionValues.put("easySolved", decimal(easy));
        distributionValues.put("mediumSolved", decimal(medium));
        distributionValues.put("hardSolved", decimal(hard));
        distributionValues.put("easyPercentage", easyPct);
        distributionValues.put("mediumPercentage", mediumPct);
        distributionValues.put("hardPercentage", hardPct);
        target.add(insight("DIFFICULTY_DISTRIBUTION", "DIFFICULTY", "INFO", platform + " difficulty distribution",
                distribution, platform, "DIFFICULTY_DISTRIBUTION", decimal(total), null, null,
                row.getSnapshotDate(), null, distributionValues));
        int maximum = Math.max(easy, Math.max(medium, hard));
        List<String> dominant = new ArrayList<>();
        if (easy == maximum) dominant.add("easy");
        if (medium == maximum) dominant.add("medium");
        if (hard == maximum) dominant.add("hard");
        String dominantLabel = String.join(" and ", dominant);
        target.add(insight("DIFFICULTY_MOST_RECORDED", "DIFFICULTY", "INFO", "Most represented difficulty",
                platform + " has the highest recorded count in " + dominantLabel + " (" + maximum + ").",
                platform, "DIFFICULTY_DISTRIBUTION", decimal(maximum), null, null, row.getSnapshotDate(), null));
        if (easy == 0) addZeroDifficultyArea(platform, "easy", row.getSnapshotDate(), areas);
        if (medium == 0) addZeroDifficultyArea(platform, "medium", row.getSnapshotDate(), areas);
        if (hard == 0) addZeroDifficultyArea(platform, "hard", row.getSnapshotDate(), areas);
    }

    private static void addZeroDifficultyArea(String platform, String difficulty, LocalDate date, List<Insight> areas) {
        areas.add(insight("DIFFICULTY_ZERO_RECORDED", "DIFFICULTY", "INFO", difficulty + " solves not recorded",
                "No " + difficulty + " solves are recorded in this complete " + platform + " difficulty snapshot.",
                platform, difficulty.toUpperCase(Locale.ROOT) + "_SOLVED", BigDecimal.ZERO, null, null, date, null));
    }

    private static void addRatingInsight(String platform, TreeMap<LocalDate, PlatformStatSnapshot> observations,
                                         List<Insight> target) {
        List<Map.Entry<LocalDate, PlatformStatSnapshot>> values = new ArrayList<>(observations.entrySet());
        Map.Entry<LocalDate, PlatformStatSnapshot> currentEntry = values.get(values.size() - 1);
        if (currentEntry.getValue().getRating() == null) return;
        Integer current = currentEntry.getValue().getRating();
        Map.Entry<LocalDate, PlatformStatSnapshot> previousEntry = previousWith(values, currentEntry.getKey(), Metric.RATING);
        if (previousEntry == null) {
            target.add(insight("RATING_BASELINE", "RATING", "INFO", platform + " rating baseline",
                    "Current recorded rating: " + current + ". No earlier rating observation is available for a trend.",
                    platform, "RATING", decimal(current), null, null, currentEntry.getKey(), null));
            return;
        }
        Integer previous = previousEntry.getValue().getRating();
        int change = current - previous;
        String direction = change > 0 ? "increased" : change < 0 ? "decreased" : "is unchanged";
        target.add(insight(change == 0 ? "RATING_UNCHANGED" : change > 0 ? "RATING_INCREASE" : "RATING_DECREASE",
                "RATING", change > 0 ? "POSITIVE" : "INFO", platform + " rating " + direction,
                "Rating " + direction + " from " + previous + " to " + current + " ("
                        + (change > 0 ? "+" : "") + change + ") between recorded observations.",
                platform, "RATING", decimal(current), decimal(previous), decimal(change),
                currentEntry.getKey(), previousEntry.getKey()));
    }

    private static void addStreakInsights(String platform, TreeMap<LocalDate, PlatformStatSnapshot> observations,
                                          List<Insight> target) {
        List<Map.Entry<LocalDate, PlatformStatSnapshot>> values = new ArrayList<>(observations.entrySet());
        Map.Entry<LocalDate, PlatformStatSnapshot> currentEntry = values.get(values.size() - 1);
        if (currentEntry.getValue().getCurrentStreak() != null) {
            Integer current = currentEntry.getValue().getCurrentStreak();
            Map.Entry<LocalDate, PlatformStatSnapshot> previousEntry = previousWith(values, currentEntry.getKey(), Metric.CURRENT_STREAK);
            if (previousEntry == null) {
                target.add(insight("STREAK_CURRENT", "STREAK", "INFO", platform + " current streak",
                        "Current recorded streak: " + current + " days.", platform, "CURRENT_STREAK",
                        decimal(current), null, null, currentEntry.getKey(), null));
            } else {
                Integer previous = previousEntry.getValue().getCurrentStreak();
                int change = current - previous;
                target.add(insight("STREAK_CHANGE", "STREAK", "INFO", platform + " recorded streak comparison",
                        "Recorded current streak changed from " + previous + " to " + current
                                + " days between observations.", platform, "CURRENT_STREAK", decimal(current),
                        decimal(previous), decimal(change), currentEntry.getKey(), previousEntry.getKey()));
            }
        }
        Map.Entry<LocalDate, PlatformStatSnapshot> longest = currentEntry;
        if (longest.getValue().getLongestStreak() != null) {
            target.add(insight("STREAK_LONGEST", "STREAK", "INFO", platform + " longest recorded streak",
                    "Longest recorded streak: " + longest.getValue().getLongestStreak() + " days.", platform,
                    "LONGEST_STREAK", decimal(longest.getValue().getLongestStreak()), null, null,
                    longest.getKey(), null));
        }
    }

    private static void addCrossPlatformSolvedInsight(List<PlatformStatSnapshot> currentRows, List<Insight> strengths) {
        List<PlatformStatSnapshot> withTotals = currentRows.stream().filter(row -> row.getTotalSolved() != null).toList();
        if (withTotals.size() < 2) return;
        int max = withTotals.stream().mapToInt(PlatformStatSnapshot::getTotalSolved).max().orElse(0);
        List<String> platforms = withTotals.stream().filter(row -> row.getTotalSolved() == max)
                .map(PlatformStatSnapshot::getPlatform).sorted().toList();
        String label = String.join(" and ", platforms);
        strengths.add(insight("PLATFORM_SOLVED_COMPARISON", "PLATFORM", "INFO", "Highest recorded solved count",
                label + " has the highest recorded total-solved count among your REAL platforms (" + max
                        + "). This compares solved counts only, not overall platform skill or rating.",
                platforms.size() == 1 ? platforms.get(0) : null, "TOTAL_SOLVED", decimal(max), null, null,
                withTotals.stream().filter(row -> platforms.contains(row.getPlatform()))
                        .map(PlatformStatSnapshot::getSnapshotDate).max(Comparator.naturalOrder()).orElse(null), null));
    }

    private static boolean hasComparableHistory(TreeMap<LocalDate, PlatformStatSnapshot> history) {
        List<PlatformStatSnapshot> rows = new ArrayList<>(history.values());
        for (int current = rows.size() - 1; current > 0; current--) {
            for (int previous = current - 1; previous >= 0; previous--) {
                if (hasCommonMetric(rows.get(current), rows.get(previous))) return true;
            }
        }
        return false;
    }

    private static boolean hasCommonMetric(PlatformStatSnapshot a, PlatformStatSnapshot b) {
        return both(a.getTotalSolved(), b.getTotalSolved()) || both(a.getEasySolved(), b.getEasySolved())
                || both(a.getMediumSolved(), b.getMediumSolved()) || both(a.getHardSolved(), b.getHardSolved())
                || both(a.getRating(), b.getRating()) || both(a.getCurrentStreak(), b.getCurrentStreak())
                || both(a.getLongestStreak(), b.getLongestStreak());
    }

    private static boolean both(Integer a, Integer b) { return a != null && b != null; }

    private static Map.Entry<LocalDate, PlatformStatSnapshot> previousWith(
            List<Map.Entry<LocalDate, PlatformStatSnapshot>> values, LocalDate before, Metric metric) {
        for (int i = values.size() - 1; i >= 0; i--) {
            Map.Entry<LocalDate, PlatformStatSnapshot> item = values.get(i);
            if (item.getKey().isBefore(before) && metric.read(item.getValue()) != null) return item;
        }
        return null;
    }

    private static BigDecimal percent(int value, int total) {
        if (total <= 0) return null;
        return BigDecimal.valueOf(value).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);
    }

    private static BigDecimal decimal(Integer value) { return value == null ? null : BigDecimal.valueOf(value); }

    private static Insight insight(String type, String category, String severity, String title, String message,
                                   String platform, String metric, BigDecimal value, BigDecimal previousValue,
                                   BigDecimal change, LocalDate date, LocalDate comparisonDate) {
        return new Insight(type, category, severity, title, message, platform, metric, value,
                previousValue, change, date, comparisonDate, Map.of());
    }

    private static Insight insight(String type, String category, String severity, String title, String message,
                                   String platform, String metric, BigDecimal value, BigDecimal previousValue,
                                   BigDecimal change, LocalDate date, LocalDate comparisonDate,
                                   Map<String, BigDecimal> supportingValues) {
        return new Insight(type, category, severity, title, message, platform, metric, value,
                previousValue, change, date, comparisonDate, Map.copyOf(supportingValues));
    }

    private enum Metric {
        TOTAL_SOLVED(PlatformStatSnapshot::getTotalSolved), RATING(PlatformStatSnapshot::getRating),
        CURRENT_STREAK(PlatformStatSnapshot::getCurrentStreak), LONGEST_STREAK(PlatformStatSnapshot::getLongestStreak);
        private final java.util.function.Function<PlatformStatSnapshot, Integer> reader;
        Metric(java.util.function.Function<PlatformStatSnapshot, Integer> reader) { this.reader = reader; }
        Integer read(PlatformStatSnapshot snapshot) { return reader.apply(snapshot); }
    }
}

import type { ContestAnalytics, DifficultyAnalytics, PlatformComparison } from '../types/dashboard';

export type AnalyticsPageState = 'loading' | 'error' | 'empty' | 'content' | 'refreshing' | 'stale-error';

export type HistoryViewState = 'loading' | 'error' | 'content' | 'refreshing' | 'stale-error';
export type RetainedDataViewState = 'loading' | 'error' | 'content' | 'refreshing' | 'stale-error';

export function getRetainedDataViewState(input: {
  loading: boolean;
  error: string | null;
  hasData: boolean;
}): RetainedDataViewState {
  if (input.hasData) {
    if (input.error) return 'stale-error';
    return input.loading ? 'refreshing' : 'content';
  }
  return input.loading ? 'loading' : 'error';
}

export function getHistoryViewState(input: {
  loading: boolean;
  error: string | null;
  hasCurrentRangeData: boolean;
}): HistoryViewState {
  if (input.hasCurrentRangeData) {
    if (input.error) return 'stale-error';
    return input.loading ? 'refreshing' : 'content';
  }
  if (input.loading) return 'loading';
  return 'error';
}

export function getAnalyticsMetricAvailability(
  platformComparison: PlatformComparison[],
  difficulty: DifficultyAnalytics | null | undefined,
  contests: ContestAnalytics | null | undefined,
) {
  const hasProfiles = platformComparison.length > 0;
  return {
    hasProfiles,
    hasSolvedMetrics: hasProfiles && (platformComparison.some((platform) => platform.totalSolved !== null)
      || (difficulty?.totalProblemsSolved !== null && difficulty?.totalProblemsSolved !== undefined)),
    hasRatingMetrics: hasProfiles && platformComparison.some((platform) => platform.rating !== null || platform.rank !== null),
    hasStreakMetrics: hasProfiles && platformComparison.some((platform) => platform.currentStreak !== null || platform.longestStreak !== null),
    hasContestMetrics: hasProfiles && !!contests && (contests.totalContests !== null || contests.platformBreakdown.some((platform) => platform.rating !== null || platform.contests !== null)),
  };
}

export function getAnalyticsPageState(input: {
  loading: boolean;
  error: string | null;
  hasData: boolean;
  hasMetrics: boolean;
}): AnalyticsPageState {
  if (!input.hasData) {
    if (input.loading) return 'loading';
    if (input.error) return 'error';
    return 'empty';
  }

  if (input.error) return 'stale-error';
  if (input.loading) return 'refreshing';
  return input.hasMetrics ? 'content' : 'empty';
}

export type DifficultyDistributionState = 'available' | 'zero-total' | 'incomplete';

export function getDifficultyDistributionState(
  difficulty: DifficultyAnalytics | null | undefined,
): DifficultyDistributionState {
  const total = difficulty?.totalProblemsSolved;
  const metrics = [difficulty?.easy, difficulty?.medium, difficulty?.hard];
  if (total === 0 && metrics.every((metric) => metric?.count === 0)) return 'zero-total';
  if (total === null || total === undefined || total <= 0) return 'incomplete';

  const counts = metrics.map((metric) => metric?.count);
  const percentages = metrics.map((metric) => metric?.percentage);
  if (counts.some((count) => count === null || count === undefined || !Number.isFinite(count) || count < 0)) return 'incomplete';
  if (percentages.some((percentage) => percentage === null || percentage === undefined || !Number.isFinite(percentage) || percentage < 0 || percentage > 100)) return 'incomplete';

  const countTotal = counts.reduce<number>((sum, count) => sum + (count ?? 0), 0);
  const percentageTotal = percentages.reduce<number>((sum, percentage) => sum + (percentage ?? 0), 0);
  const percentagesMatchCounts = counts.every((count, index) =>
    Math.abs((percentages[index] ?? 0) - ((count ?? 0) / total) * 100) <= 0.1);
  return countTotal === total && Math.abs(percentageTotal - 100) <= 0.1 && percentagesMatchCounts ? 'available' : 'incomplete';
}

export function getKnownStreakBarPercentage(value: number | null | undefined, maximum: number): number | null {
  if (value === null || value === undefined || !Number.isFinite(value) || value < 0 || maximum <= 0) return null;
  return Math.min((value / maximum) * 100, 100);
}

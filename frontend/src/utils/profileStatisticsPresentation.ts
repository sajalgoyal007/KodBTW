import type { PlatformAccountResponse } from '../types/platform';
import type { PlatformStats } from '../types/dashboard';
import type { PlatformMetric } from '../config/platformCapabilities';

const METRIC_LABELS: Record<PlatformMetric, string> = {
  totalProblemsSolved: 'Solved',
  easySolved: 'Easy',
  mediumSolved: 'Medium',
  hardSolved: 'Hard',
  rating: 'Current rating',
  maxRating: 'Highest rating',
  rank: 'Reported rank',
  contestsParticipated: 'Contests',
  currentStreak: 'Current streak',
  longestStreak: 'Longest streak',
};

export interface ProfileStatisticsMetric {
  key: PlatformMetric;
  label: string;
  value: number | null;
}

export function getSupportedProfileMetrics(
  stats: PlatformStats | undefined,
  supportedMetrics: readonly PlatformMetric[],
  sourceEligible: boolean,
): ProfileStatisticsMetric[] {
  if (!stats || !sourceEligible) return [];

  return supportedMetrics.map((key) => ({
    key,
    label: METRIC_LABELS[key],
    value: stats[key] ?? null,
  }));
}

export function getProfileSyncMessage(
  account: PlatformAccountResponse,
  hasSavedSnapshot: boolean,
): string {
  if (account.sourceStatus === 'LINK_ONLY') {
    return 'Profile link saved. AlgoMaster statistics are not fetched or tracked.';
  }

  if (account.syncStatus === 'RUNNING') {
    return account.lastSuccessAt
      ? 'A refresh is running; the last successful snapshot remains visible.'
      : 'The first statistics sync is running.';
  }

  if (account.syncStatus === 'FAILED') {
    return account.lastSuccessAt
      ? 'The latest refresh failed. Previously saved statistics are retained and may be stale.'
      : 'The latest refresh failed. No successful statistics snapshot is available yet.';
  }

  if (account.sourceStatus === 'SOURCE_PENDING') {
    return hasSavedSnapshot
      ? 'Live sync is unavailable; the saved snapshot is retained and may be stale.'
      : 'This platform has no enabled live statistics source.';
  }

  if (account.syncStatus === 'NEVER_SYNCED') return 'No successful statistics sync has been recorded yet.';
  if (account.syncStatus === 'SUCCEEDED' && !account.fresh) {
    return 'The last successful sync is outside the freshness window; these statistics may be stale.';
  }
  if (account.syncStatus === 'SUCCEEDED' && account.fresh) {
    return 'The last successful statistics sync is within the freshness window.';
  }

  return 'Sync freshness is unavailable.';
}

export interface ProfileAchievement {
  id: string;
  title: string;
  description: string;
  unlocked: boolean;
  evidence: string | null;
}

export interface EligiblePlatformObservation {
  displayName: string;
  totalProblemsSolved: number | null;
}

export function getProfileAchievements(
  platformStats: EligiblePlatformObservation[],
  connectedPlatformCount: number,
): ProfileAchievement[] {
  const firstSolve = platformStats.find((stats) => (stats.totalProblemsSolved ?? 0) > 0);
  const hundredSolve = platformStats.find((stats) => (stats.totalProblemsSolved ?? 0) >= 100);

  return [
    {
      id: 'first-profile',
      title: 'First profile connected',
      description: 'Save a coding platform profile to KodBTW.',
      unlocked: connectedPlatformCount > 0,
      evidence: connectedPlatformCount > 0 ? `${connectedPlatformCount} profile${connectedPlatformCount === 1 ? '' : 's'} saved` : null,
    },
    {
      id: 'first-reported-solve',
      title: 'First solve reported',
      description: 'A supported statistics source reports at least one solved problem.',
      unlocked: firstSolve !== undefined,
      evidence: firstSolve ? `${firstSolve.displayName} reports ${firstSolve.totalProblemsSolved} solved` : null,
    },
    {
      id: 'hundred-on-one-platform',
      title: '100 on one platform',
      description: 'Reach 100 reported solves on a single platform profile.',
      unlocked: hundredSolve !== undefined,
      evidence: hundredSolve ? `${hundredSolve.displayName} reports ${hundredSolve.totalProblemsSolved} solved` : null,
    },
  ];
}


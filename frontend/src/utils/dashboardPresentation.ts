import type { DifficultyAnalytics, PlatformComparison, PlatformDifficultyBreakdown, PlatformStats } from '../types/dashboard';
import type { PlatformAccountResponse } from '../types/platform';

const PLATFORM_METRIC_KEYS: Array<keyof PlatformStats> = [
  'totalProblemsSolved',
  'easySolved',
  'mediumSolved',
  'hardSolved',
  'rating',
  'maxRating',
  'rank',
  'contestsParticipated',
  'currentStreak',
  'longestStreak',
];

export type DashboardDisplayState =
  | 'loading'
  | 'error'
  | 'no-accounts'
  | 'accounts-unconfirmed'
  | 'connected-unavailable'
  | 'ready';

export function getDashboardDisplayState(input: {
  loading: boolean;
  error: string | null;
  hasStatsResponse: boolean;
  hasAvailableStats: boolean;
  hasConnectedAccounts: boolean;
  accountsLoaded: boolean;
  accountsLoadError?: boolean;
}): DashboardDisplayState {
  if (input.loading) return 'loading';
  if (input.error && !input.hasStatsResponse) return 'error';
  if (input.hasAvailableStats) return 'ready';
  if (input.hasConnectedAccounts) return 'connected-unavailable';
  if (input.accountsLoaded && !input.accountsLoadError) return 'no-accounts';
  return 'accounts-unconfirmed';
}

export function hasAvailablePlatformStats(platform: PlatformStats): boolean {
  return PLATFORM_METRIC_KEYS.some((key) => platform[key] !== null && platform[key] !== undefined);
}

export function hasPartialPlatformStats(platforms: PlatformStats[], connectedPlatformCount = platforms.length): boolean {
  return platforms.length < connectedPlatformCount
    || platforms.some((platform) => PLATFORM_METRIC_KEYS.some((key) => platform[key] === null || platform[key] === undefined));
}

/**
 * Dashboard analytics currently emits 0% when a platform's solved count is
 * unknown. A null totalSolved lets the UI conservatively present that share
 * as unavailable while preserving a confirmed numeric zero.
 */
export function getKnownPlatformSharePercentage(comparison: PlatformComparison): number | null {
  if (comparison.totalSolved === null || comparison.totalSolved === undefined) return null;
  if (comparison.sharePercentage === null || comparison.sharePercentage === undefined || !Number.isFinite(comparison.sharePercentage)) {
    return null;
  }
  return comparison.sharePercentage;
}

export function getKnownDifficultyPercentage(
  metric: { count: number | null; percentage: number | null } | null | undefined,
): number | null {
  if (metric?.count === null || metric?.count === undefined) return null;
  if (metric.percentage === null || metric.percentage === undefined || !Number.isFinite(metric.percentage)) return null;
  return metric.percentage;
}

export function getDifficultyCoverage(
  difficulty: DifficultyAnalytics | null | undefined,
  connectedAccountCount: number,
): { complete: boolean; message: string } {
  const rows: PlatformDifficultyBreakdown[] = difficulty?.platformBreakdown ?? [];
  if (rows.length === 0) {
    return { complete: false, message: 'Difficulty details are not available yet.' };
  }

  const incompleteRows = rows.filter((row) =>
    row.easy === null || row.easy === undefined
    || row.medium === null || row.medium === undefined
    || row.hard === null || row.hard === undefined);
  const missingRows = Math.max(connectedAccountCount - rows.length, 0);
  if (incompleteRows.length > 0 || missingRows > 0) {
    return { complete: false, message: 'Partial breakdown: one or more connected profiles do not provide all difficulty counts.' };
  }

  return {
    complete: true,
    message: `Difficulty counts are available for all ${rows.length} connected profile${rows.length === 1 ? '' : 's'}.`,
  };
}

export type PlatformSyncPresentation = { label: string; detail: string; tone: 'neutral' | 'success' | 'warning' | 'error' };

export function getPlatformSyncPresentation(
  account: Pick<PlatformAccountResponse, 'syncStatus' | 'sourceStatus' | 'fresh' | 'lastSuccessAt'> | undefined,
): PlatformSyncPresentation {
  if (!account) return { label: 'Sync status unavailable', detail: 'Account status could not be loaded.', tone: 'neutral' };

  if (account.syncStatus === 'RUNNING') {
    return {
      label: 'Sync in progress',
      detail: account.lastSuccessAt ? 'Showing the last successful stats until this sync finishes.' : 'Waiting for the first successful stats sync.',
      tone: 'neutral',
    };
  }

  if (account.syncStatus === 'FAILED') {
    return account.lastSuccessAt
      ? { label: 'Latest sync failed', detail: 'Showing previously saved stats; they may be stale.', tone: 'warning' }
      : { label: 'Sync failed', detail: 'No successful statistics have been saved yet.', tone: 'error' };
  }

  if (account.sourceStatus === 'SOURCE_PENDING') {
    return {
      label: 'Live statistics unavailable',
      detail: account.lastSuccessAt
        ? 'Previously saved stats may be stale.'
        : account.syncStatus === 'NEVER_SYNCED'
          ? 'This platform has no live statistics source, and no successful sync is recorded.'
          : 'This platform has no available live statistics source.',
      tone: 'neutral',
    };
  }

  if (account.syncStatus === 'NEVER_SYNCED') {
    return { label: 'Never synced', detail: 'Refresh this profile to request its first statistics sync.', tone: 'neutral' };
  }

  if (account.syncStatus === 'SUCCEEDED' && account.lastSuccessAt) {
    return account.fresh
      ? { label: 'Recently synced', detail: 'Last successful statistics are within the backend freshness window.', tone: 'success' }
      : { label: 'May be stale', detail: 'Showing the last successful statistics; freshness is not guaranteed.', tone: 'warning' };
  }

  return { label: 'Sync status unavailable', detail: 'A successful sync timestamp is not available.', tone: 'neutral' };
}

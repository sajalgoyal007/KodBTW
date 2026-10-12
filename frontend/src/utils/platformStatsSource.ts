import type { PlatformAccountResponse } from '../types/platform';
import type { PlatformSyncResponse } from '../services/api/platformAccountApi';

export type PlatformStatsSourceKind = 'live' | 'third-party' | 'mock' | 'pending' | 'unknown';

const LIVE_STATS_SOURCES = new Set(['LEETCODE_REAL', 'CODEFORCES_REAL']);
const THIRD_PARTY_STATS_SOURCES = new Set(['CODECHEF_THIRD_PARTY']);

export function getPlatformStatsSourceKind(source: string | null | undefined): PlatformStatsSourceKind {
  const normalized = source?.trim().toUpperCase();
  if (!normalized) return 'unknown';
  if (LIVE_STATS_SOURCES.has(normalized)) return 'live';
  if (THIRD_PARTY_STATS_SOURCES.has(normalized)) return 'third-party';
  if (normalized === 'MOCK') return 'mock';
  if (normalized === 'SOURCE_PENDING' || normalized === 'UNSYNCED') return 'pending';
  return 'unknown';
}

/**
 * Compatibility helper used by profile/stat views to decide whether values
 * came from a supported live source. It does not mean account ownership was verified.
 */
export function isVerifiedStatsSource(source: string | null | undefined): boolean {
  const kind = getPlatformStatsSourceKind(source);
  return kind === 'live' || kind === 'third-party';
}

export function getPlatformStatsSourceLabel(
  source: string | null | undefined,
  lastSyncErrorCategory?: string | null,
): string {
  const normalized = source?.trim().toUpperCase();
  if (normalized === 'UNSYNCED' && lastSyncErrorCategory === 'LIVE_SYNC_UNAVAILABLE') return 'Live sync unavailable';
  switch (getPlatformStatsSourceKind(normalized)) {
    case 'live': return 'Live Public Data';
    case 'third-party': return 'Third-party Live Data';
    case 'mock': return 'Mock Sandbox';
    case 'pending': return normalized === 'UNSYNCED' ? 'Not synced' : 'Live statistics unavailable';
    default: return 'Source unknown';
  }
}

export function getPlatformStatsSourceBadgeClass(source: string | null | undefined): string {
  switch (getPlatformStatsSourceKind(source)) {
    case 'live': return 'badge-success';
    case 'third-party': return 'badge-primary';
    case 'mock': return 'badge-muted';
    default: return 'badge-muted';
  }
}

export interface DashboardRefreshFeedback {
  success: boolean;
  message: string;
}

export function getDashboardRefreshFeedback(
  result: Omit<PlatformSyncResponse, 'currentStats'> & { currentStats: PlatformSyncResponse['currentStats'] | null },
): DashboardRefreshFeedback {
  const { syncStatus, currentStats, cooldownApplied } = result;
  if (syncStatus.status === 'FAILED') {
    return { success: false, message: syncStatus.failureMessage || 'Refresh failed.' };
  }
  if (syncStatus.status === 'RUNNING') {
    return { success: false, message: 'Stats refresh is already in progress.' };
  }

  const hasMetric = currentStats != null && [
    currentStats.totalProblemsSolved,
    currentStats.easySolved,
    currentStats.mediumSolved,
    currentStats.hardSolved,
    currentStats.rating,
    currentStats.maxRating,
    currentStats.rank,
    currentStats.contestsParticipated,
    currentStats.currentStreak,
    currentStats.longestStreak,
  ].some((value) => value !== null && value !== undefined);
  const sourceAvailable = currentStats != null
    && isVerifiedStatsSource(currentStats.source)
    && currentStats.source.toUpperCase() !== 'MOCK';

  if (syncStatus.status !== 'SUCCEEDED' || !sourceAvailable || !hasMetric) {
    return { success: false, message: 'Live statistics are unavailable; no successful sync was completed.' };
  }
  if (cooldownApplied) {
    return { success: true, message: 'Recently synced; showing saved stats.' };
  }
  return { success: true, message: 'Stats refreshed successfully.' };
}

/** Keep a previous availability state when a response does not confirm usable stats. */
export function getPlatformSyncSourceStatus(
  currentStatus: PlatformAccountResponse['sourceStatus'],
  result: Parameters<typeof getDashboardRefreshFeedback>[0],
): PlatformAccountResponse['sourceStatus'] {
  if (currentStatus === 'SOURCE_PENDING') return currentStatus;
  if (result.syncStatus.status === 'RUNNING') return 'SYNCING';
  if (result.syncStatus.status === 'FAILED') return 'SYNC_FAILED';
  return getDashboardRefreshFeedback(result).success ? 'SYNCED' : currentStatus;
}

export function getPlatformAvailabilityLabel(
  platform: string,
  account?: Pick<PlatformAccountResponse, 'sourceStatus'>,
): string {
  if (account) {
    switch (account.sourceStatus) {
      case 'SOURCE_PENDING': return 'Live statistics unavailable';
      case 'SYNCING': return 'Sync in progress';
      case 'SYNCED': return 'Live statistics synced';
      case 'SYNC_FAILED': return 'Last sync failed';
      case 'REAL_AVAILABLE': return 'Live statistics available';
      default: return 'Availability unknown';
    }
  }

  switch (platform.toUpperCase()) {
    case 'CODECHEF': return 'Connect an account to check availability';
    case 'GEEKSFORGEEKS':
    case 'HACKERRANK': return 'Live statistics unavailable';
    case 'LEETCODE':
    case 'CODEFORCES': return 'Live statistics available';
    default: return 'Availability unknown';
  }
}

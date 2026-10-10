import type { PlatformAccountResponse } from '../types/platform';
import type { PlatformSyncResponse } from '../services/api/platformAccountApi';

export function isVerifiedStatsSource(source: string | null | undefined): boolean {
  if (!source) return false;
  const normalized = source.trim().toUpperCase();
  return normalized === 'CODECHEF_THIRD_PARTY'
    || (normalized.endsWith('_REAL') && !normalized.includes('MOCK'));
}

export function getPlatformStatsSourceLabel(
  source: string | null | undefined,
  lastSyncErrorCategory?: string | null,
): string {
  const normalized = source?.trim().toUpperCase();
  if (normalized === 'UNSYNCED' && lastSyncErrorCategory === 'LIVE_SYNC_UNAVAILABLE') return 'Live sync unavailable';
  if (normalized === 'UNSYNCED') return 'Not synced';
  if (normalized === 'SOURCE_PENDING') return 'Live statistics unavailable';
  if (normalized === 'CODECHEF_THIRD_PARTY') return 'Third-party Live Data';
  if (isVerifiedStatsSource(normalized)) return 'Live Public Data';
  if (normalized === 'MOCK') return 'Mock Sandbox';
  return 'Statistics unavailable';
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
    }
  }

  switch (platform.toUpperCase()) {
    case 'CODECHEF': return 'Connect an account to check availability';
    case 'GEEKSFORGEEKS':
    case 'HACKERRANK': return 'Live statistics unavailable';
    default: return 'Live statistics available';
  }
}

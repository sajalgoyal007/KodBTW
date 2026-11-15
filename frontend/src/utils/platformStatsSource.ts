import type { PlatformAccountResponse } from '../types/platform';
import type { PlatformSyncResponse } from '../services/api/platformAccountApi';
import { getPlatformCapability, getStatsSourceKind } from '../config/platformCapabilities';

export type PlatformStatsSourceKind = 'live' | 'third-party' | 'link-only' | 'mock' | 'pending' | 'unknown';

export function getPlatformStatsSourceKind(source: string | null | undefined): PlatformStatsSourceKind {
  return getStatsSourceKind(source);
}

/**
 * Compatibility helper used by profile/stat views to decide whether values
 * came from a supported live source. It does not mean account ownership was verified.
 */
export function isVerifiedStatsSource(source: string | null | undefined): boolean {
  const kind = getPlatformStatsSourceKind(source);
  return kind === 'live' || kind === 'third-party';
}

export function getUnsupportedStatsMessage(platform: string | null | undefined): string | null {
  return platform?.toUpperCase() === 'CODEFORCES'
    ? 'The current Codeforces integration does not provide solved counts or difficulty breakdowns.'
    : null;
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
    case 'link-only': return 'Profile link only';
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

  if (syncStatus.status === 'SUCCEEDED'
      && currentStats?.platform?.toUpperCase() === 'CODEFORCES'
      && (!sourceAvailable || !hasMetric)) {
    return {
      success: false,
      message: 'Sync completed, but no Codeforces statistics are currently available. The current integration does not provide solved counts or difficulty breakdowns.',
    };
  }

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
  if (currentStatus === 'SOURCE_PENDING' || currentStatus === 'LINK_ONLY') return currentStatus;
  if (result.syncStatus.status === 'RUNNING') return 'SYNCING';
  if (result.syncStatus.status === 'FAILED') return 'SYNC_FAILED';
  return getDashboardRefreshFeedback(result).success ? 'SYNCED' : currentStatus;
}

export function getPlatformAvailabilityLabel(
  platform: string,
  account?: Pick<PlatformAccountResponse, 'sourceStatus'>,
): string {
  const capability = getPlatformCapability(platform);
  if (account) {
    switch (account.sourceStatus) {
      case 'LINK_ONLY': return 'Profile link saved · statistics not tracked';
      case 'SOURCE_PENDING': return capability?.synchronizationEnabled
        ? `No successful sync yet. ${capability.availabilityMessage}`
        : capability?.availabilityMessage ?? 'Live statistics unavailable.';
      case 'SYNCING': return 'Sync in progress';
      case 'SYNCED': return `Stats synced · ${capability?.availabilityMessage ?? 'Source details unavailable.'}`;
      case 'SYNC_FAILED': return 'Last sync failed';
      case 'REAL_AVAILABLE': return capability?.availabilityMessage ?? 'Live statistics available';
      default: return 'Availability unknown';
    }
  }
  return capability?.availabilityMessage ?? 'Availability unknown; no platform integration is configured.';
}

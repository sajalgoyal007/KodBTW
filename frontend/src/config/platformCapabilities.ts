import type { PlatformType } from '../types/platform';

export type PlatformMetric =
  | 'totalProblemsSolved'
  | 'easySolved'
  | 'mediumSolved'
  | 'hardSolved'
  | 'rating'
  | 'maxRating'
  | 'rank'
  | 'contestsParticipated'
  | 'currentStreak'
  | 'longestStreak';

export interface PlatformCapability {
  id: string;
  displayName: string;
  accentColor: string;
  accountPlatform: PlatformType | null;
  accountConnectionAvailable: boolean;
  liveStatisticsAvailable: boolean;
  synchronizationEnabled: boolean;
  supportedMetrics: readonly PlatformMetric[];
  statisticsSourceTag: string | null;
  sourceApprovalStatus: 'documented-public-api' | 'unverified' | 'unavailable';
  sourceDescription: string;
  availabilityMessage: string;
  profileUrlFor: ((username: string) => string) | null;
}

const safeHandle = (username: string) => encodeURIComponent(username.trim());

export const PLATFORM_CAPABILITIES: readonly PlatformCapability[] = [
  {
    id: 'LEETCODE',
    displayName: 'LeetCode',
    accentColor: '#f89f1b',
    accountPlatform: 'LEETCODE',
    accountConnectionAvailable: true,
    liveStatisticsAvailable: true,
    synchronizationEnabled: true,
    supportedMetrics: ['totalProblemsSolved', 'easySolved', 'mediumSolved', 'hardSolved', 'rank', 'currentStreak', 'rating', 'contestsParticipated'],
    statisticsSourceTag: 'LEETCODE_REAL',
    sourceApprovalStatus: 'unverified',
    sourceDescription: 'Undocumented public GraphQL query; authorization and contract stability are unverified.',
    availabilityMessage: 'Live stats use an undocumented public endpoint; its authorization and stability are unverified.',
    profileUrlFor: (username) => `https://leetcode.com/u/${safeHandle(username)}/`,
  },
  {
    id: 'CODEFORCES',
    displayName: 'Codeforces',
    accentColor: '#1f8acb',
    accountPlatform: 'CODEFORCES',
    accountConnectionAvailable: true,
    liveStatisticsAvailable: true,
    synchronizationEnabled: true,
    supportedMetrics: ['rating', 'maxRating'],
    statisticsSourceTag: 'CODEFORCES_REAL',
    sourceApprovalStatus: 'documented-public-api',
    sourceDescription: 'Official public API. The current user-info integration provides rating and max rating only.',
    availabilityMessage: 'Official API sync provides rating and max rating; solved counts and numeric global rank are unavailable.',
    profileUrlFor: (username) => `https://codeforces.com/profile/${safeHandle(username)}`,
  },
  {
    id: 'CODECHEF',
    displayName: 'CodeChef',
    accentColor: '#8b4513',
    accountPlatform: 'CODECHEF',
    accountConnectionAvailable: true,
    liveStatisticsAvailable: true,
    synchronizationEnabled: true,
    supportedMetrics: ['totalProblemsSolved', 'rating', 'maxRating', 'rank'],
    statisticsSourceTag: 'CODECHEF_THIRD_PARTY',
    sourceApprovalStatus: 'unverified',
    sourceDescription: 'Unofficial third-party provider; authorization and reliability are unverified.',
    availabilityMessage: 'Third-party stats are integrated, but provider permission and reliability are unverified.',
    profileUrlFor: (username) => `https://www.codechef.com/users/${safeHandle(username)}`,
  },
  {
    id: 'GEEKSFORGEEKS',
    displayName: 'GeeksforGeeks',
    accentColor: '#2f8d46',
    accountPlatform: 'GEEKSFORGEEKS',
    accountConnectionAvailable: true,
    liveStatisticsAvailable: false,
    synchronizationEnabled: false,
    supportedMetrics: [],
    statisticsSourceTag: null,
    sourceApprovalStatus: 'unverified',
    sourceDescription: 'Third-party prototype exists but normal synchronization is disabled.',
    availabilityMessage: 'A handle can be saved, but live statistics and synchronization are unavailable.',
    profileUrlFor: (username) => `https://www.geeksforgeeks.org/profile/${safeHandle(username)}`,
  },
  {
    id: 'HACKERRANK',
    displayName: 'HackerRank',
    accentColor: '#2ec866',
    accountPlatform: 'HACKERRANK',
    accountConnectionAvailable: true,
    liveStatisticsAvailable: false,
    synchronizationEnabled: false,
    supportedMetrics: [],
    statisticsSourceTag: null,
    sourceApprovalStatus: 'unverified',
    sourceDescription: 'Third-party prototype exists but normal synchronization is disabled.',
    availabilityMessage: 'A handle can be saved, but live statistics and synchronization are unavailable.',
    profileUrlFor: (username) => `https://www.hackerrank.com/profile/${safeHandle(username)}`,
  },
  {
    id: 'ATCODER',
    displayName: 'AtCoder',
    accentColor: 'var(--color-border-subtle)',
    accountPlatform: null,
    accountConnectionAvailable: false,
    liveStatisticsAvailable: false,
    synchronizationEnabled: false,
    supportedMetrics: [],
    statisticsSourceTag: null,
    sourceApprovalStatus: 'unavailable',
    sourceDescription: 'No approved integration is configured.',
    availabilityMessage: 'Account connection and statistics are not available.',
    profileUrlFor: null,
  },
  {
    id: 'INTERVIEWBIT',
    displayName: 'InterviewBit',
    accentColor: 'var(--color-border-subtle)',
    accountPlatform: null,
    accountConnectionAvailable: false,
    liveStatisticsAvailable: false,
    synchronizationEnabled: false,
    supportedMetrics: [],
    statisticsSourceTag: null,
    sourceApprovalStatus: 'unavailable',
    sourceDescription: 'No approved integration is configured.',
    availabilityMessage: 'Account connection and statistics are not available.',
    profileUrlFor: null,
  },
  {
    id: 'CODESTUDIO',
    displayName: 'CodeStudio',
    accentColor: 'var(--color-border-subtle)',
    accountPlatform: null,
    accountConnectionAvailable: false,
    liveStatisticsAvailable: false,
    synchronizationEnabled: false,
    supportedMetrics: [],
    statisticsSourceTag: null,
    sourceApprovalStatus: 'unavailable',
    sourceDescription: 'No approved integration is configured.',
    availabilityMessage: 'Account connection and statistics are not available.',
    profileUrlFor: null,
  },
];

export function getPlatformCapability(platform: string | null | undefined): PlatformCapability | undefined {
  if (!platform) return undefined;
  return PLATFORM_CAPABILITIES.find((capability) => capability.id === platform.trim().toUpperCase());
}

export function getAccountConnectablePlatforms(): Array<PlatformCapability & { accountPlatform: PlatformType }> {
  return PLATFORM_CAPABILITIES.filter(
    (capability): capability is PlatformCapability & { accountPlatform: PlatformType } =>
      capability.accountConnectionAvailable && capability.accountPlatform !== null,
  );
}

export function getUnavailablePlatforms(): PlatformCapability[] {
  return PLATFORM_CAPABILITIES.filter((capability) => !capability.accountConnectionAvailable);
}

export function getStatsSourceKind(source: string | null | undefined): 'live' | 'third-party' | 'mock' | 'pending' | 'unknown' {
  const normalized = source?.trim().toUpperCase();
  if (!normalized) return 'unknown';
  const sourceCapability = PLATFORM_CAPABILITIES.find((capability) =>
    capability.synchronizationEnabled && capability.statisticsSourceTag === normalized,
  );
  if (sourceCapability?.id === 'CODECHEF') return 'third-party';
  if (sourceCapability) return 'live';
  if (normalized === 'MOCK') return 'mock';
  if (normalized === 'SOURCE_PENDING' || normalized === 'UNSYNCED') return 'pending';
  return 'unknown';
}

export function getPlatformConnectionStatus(
  platform: string,
  sourceStatus?: 'REAL_AVAILABLE' | 'SOURCE_PENDING' | 'SYNCING' | 'SYNCED' | 'SYNC_FAILED',
): string {
  const capability = getPlatformCapability(platform);
  if (!capability?.accountConnectionAvailable) return 'Unavailable';
  if (!sourceStatus) return 'Not connected';
  if (sourceStatus === 'SOURCE_PENDING' || !capability.synchronizationEnabled) {
    return 'Handle saved · live statistics unavailable';
  }
  if (sourceStatus === 'SYNCING') return 'Sync in progress';
  if (sourceStatus === 'SYNC_FAILED') return 'Last sync failed';
  if (sourceStatus === 'SYNCED') return 'Stats synced';
  return 'Handle saved · not synced yet';
}

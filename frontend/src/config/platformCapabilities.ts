import type { PlatformAccountResponse, PlatformType } from '../types/platform';

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

export type PlatformSourceApprovalStatus = 'documented-public-api' | 'unverified' | 'unavailable';

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
  sourceApprovalStatus: PlatformSourceApprovalStatus;
  sourceDescription: string;
  availabilityMessage: string;
  profileUrlFor: ((username: string) => string) | null;
}

const safeHandle = (username: string) => encodeURIComponent(username.trim());

/**
 * Sources that come from a third-party provider or public HTML rather than an official
 * platform API. They render with the third-party badge instead of the live badge.
 */
const THIRD_PARTY_SOURCE_TAGS = new Set([
  'CODECHEF_THIRD_PARTY',
  'GEEKSFORGEEKS_THIRD_PARTY',
  'CODE360_THIRD_PARTY',
]);

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
    id: 'ATCODER',
    displayName: 'AtCoder',
    accentColor: '#2f3640',
    accountPlatform: 'ATCODER',
    accountConnectionAvailable: true,
    liveStatisticsAvailable: true,
    synchronizationEnabled: true,
    supportedMetrics: ['totalProblemsSolved', 'rating', 'maxRating', 'rank', 'contestsParticipated', 'currentStreak', 'longestStreak'],
    statisticsSourceTag: 'ATCODER_REAL',
    sourceApprovalStatus: 'unverified',
    sourceDescription: 'AtCoder public rating history plus Kenkoooo’s public accepted-count and submissions APIs.',
    availabilityMessage: 'Live stats combine AtCoder public history with Kenkoooo’s public API; endpoint stability is unverified.',
    profileUrlFor: (username) => `https://atcoder.jp/users/${safeHandle(username)}`,
  },
  {
    id: 'GEEKSFORGEEKS',
    displayName: 'GeeksforGeeks',
    accentColor: '#2f8d46',
    accountPlatform: 'GEEKSFORGEEKS',
    accountConnectionAvailable: true,
    liveStatisticsAvailable: true,
    synchronizationEnabled: true,
    supportedMetrics: ['totalProblemsSolved', 'easySolved', 'mediumSolved', 'hardSolved'],
    statisticsSourceTag: 'GEEKSFORGEEKS_THIRD_PARTY',
    sourceApprovalStatus: 'unverified',
    sourceDescription: 'Unofficial third-party stats provider; authorization and reliability are unverified.',
    availabilityMessage: 'Third-party stats are integrated, but provider permission and reliability are unverified.',
    profileUrlFor: (username) => `https://www.geeksforgeeks.org/profile/${safeHandle(username)}`,
  },
  {
    id: 'CODE360',
    displayName: 'Code360 (CodeStudio)',
    accentColor: '#5a67d8',
    accountPlatform: 'CODE360',
    accountConnectionAvailable: true,
    liveStatisticsAvailable: true,
    synchronizationEnabled: true,
    supportedMetrics: ['totalProblemsSolved', 'rating', 'contestsParticipated', 'currentStreak', 'longestStreak'],
    statisticsSourceTag: 'CODE360_THIRD_PARTY',
    sourceApprovalStatus: 'unverified',
    sourceDescription: 'Parses the public Naukri Code360 profile page; no documented stats API is available.',
    availabilityMessage: 'Live stats parse the public Code360 profile page; markup changes can interrupt sync.',
    profileUrlFor: (username) => `https://www.naukri.com/code360/profile/${safeHandle(username)}`,
  },
  {
    id: 'HACKERRANK',
    displayName: 'HackerRank',
    accentColor: '#2ec866',
    accountPlatform: 'HACKERRANK',
    accountConnectionAvailable: true,
    liveStatisticsAvailable: true,
    synchronizationEnabled: true,
    supportedMetrics: ['totalProblemsSolved', 'rating', 'maxRating'],
    statisticsSourceTag: 'HACKERRANK_REAL',
    sourceApprovalStatus: 'unverified',
    sourceDescription: 'Undocumented public HackerRank badge and ELO-score REST endpoints.',
    availabilityMessage: 'Live stats use undocumented public HackerRank REST endpoints; availability and response shapes may change.',
    profileUrlFor: (username) => `https://www.hackerrank.com/profile/${safeHandle(username)}`,
  },
  {
    id: 'INTERVIEWBIT',
    displayName: 'InterviewBit',
    accentColor: '#3b82f6',
    accountPlatform: 'INTERVIEWBIT',
    accountConnectionAvailable: true,
    liveStatisticsAvailable: true,
    synchronizationEnabled: true,
    supportedMetrics: ['totalProblemsSolved', 'rating', 'rank', 'currentStreak'],
    statisticsSourceTag: 'INTERVIEWBIT_REAL',
    sourceApprovalStatus: 'unverified',
    sourceDescription: 'Parses the public InterviewBit profile page; no documented stats API is available.',
    availabilityMessage: 'Live stats parse the public InterviewBit profile page; markup changes can interrupt sync.',
    profileUrlFor: (username) => `https://www.interviewbit.com/profile/${safeHandle(username)}/`,
  },
  {
    id: 'HACKEREARTH',
    displayName: 'HackerEarth',
    accentColor: '#2c3e8f',
    accountPlatform: 'HACKEREARTH',
    accountConnectionAvailable: true,
    liveStatisticsAvailable: true,
    synchronizationEnabled: true,
    supportedMetrics: ['totalProblemsSolved', 'rating', 'contestsParticipated'],
    statisticsSourceTag: 'HACKEREARTH_REAL',
    sourceApprovalStatus: 'unverified',
    sourceDescription: 'Parses the public HackerEarth profile page; no documented stats API is available.',
    availabilityMessage: 'Live stats parse the public HackerEarth profile page; markup changes can interrupt sync.',
    profileUrlFor: (username) => `https://www.hackerearth.com/@${safeHandle(username)}`,
  },
  {
    id: 'ALGOMASTER',
    displayName: 'AlgoMaster',
    accentColor: '#7c3aed',
    accountPlatform: 'ALGOMASTER',
    accountConnectionAvailable: true,
    liveStatisticsAvailable: false,
    synchronizationEnabled: false,
    supportedMetrics: [],
    statisticsSourceTag: null,
    sourceApprovalStatus: 'unavailable',
    sourceDescription: 'Link-only integration. AlgoMaster does not publish public user statistics.',
    availabilityMessage: 'AlgoMaster is link-only: save a public profile URL to display it; statistics are not fetched.',
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

export function getStatsSourceKind(
  source: string | null | undefined,
): 'live' | 'third-party' | 'link-only' | 'mock' | 'pending' | 'unknown' {
  const normalized = source?.trim().toUpperCase();
  if (!normalized) return 'unknown';
  if (THIRD_PARTY_SOURCE_TAGS.has(normalized)) return 'third-party';
  if (normalized === 'LINK_ONLY' || normalized === 'ALGOMASTER_LINK_ONLY') return 'link-only';
  const sourceCapability = PLATFORM_CAPABILITIES.find((capability) =>
    capability.synchronizationEnabled && capability.statisticsSourceTag === normalized,
  );
  if (sourceCapability) return 'live';
  if (normalized === 'MOCK') return 'mock';
  if (normalized === 'SOURCE_PENDING' || normalized === 'UNSYNCED') return 'pending';
  return 'unknown';
}

export function getPlatformConnectionStatus(
  platform: string,
  sourceStatus?: PlatformAccountResponse['sourceStatus'],
): string {
  const capability = getPlatformCapability(platform);
  if (!capability?.accountConnectionAvailable) return 'Unavailable';
  if (!sourceStatus) return 'Not connected';
  if (sourceStatus === 'LINK_ONLY' || !capability.synchronizationEnabled) {
    return 'Profile link saved · statistics not tracked';
  }
  if (sourceStatus === 'SOURCE_PENDING') {
    return 'Handle saved · live statistics unavailable';
  }
  if (sourceStatus === 'SYNCING') return 'Sync in progress';
  if (sourceStatus === 'SYNC_FAILED') return 'Last sync failed';
  if (sourceStatus === 'SYNCED') return 'Stats synced';
  return 'Handle saved · not synced yet';
}

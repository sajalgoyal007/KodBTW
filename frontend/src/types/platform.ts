export type PlatformType = 'LEETCODE' | 'CODECHEF' | 'CODEFORCES' | 'GEEKSFORGEEKS' | 'HACKERRANK';

export interface PlatformAccountResponse {
  id: number;
  userId: number;
  platform: PlatformType;
  username: string;
  profileUrl: string | null;
  verified: boolean;
  connectedAt: string;
  updatedAt: string;
  syncStatus: 'NEVER_SYNCED' | 'RUNNING' | 'SUCCEEDED' | 'FAILED';
  lastAttemptAt: string | null;
  lastSuccessAt: string | null;
  lastFailureAt: string | null;
  lastSyncErrorCategory: string | null;
  lastSyncErrorMessage: string | null;
  fresh: boolean;
}

export interface PlatformAccountRequest {
  platform: PlatformType;
  username: string;
  profileUrl?: string | null;
}

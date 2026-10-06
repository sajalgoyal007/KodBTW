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
}

export interface PlatformAccountRequest {
  platform: PlatformType;
  username: string;
  profileUrl?: string | null;
}

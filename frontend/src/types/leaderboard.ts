export type LeaderboardSort = 'score' | 'solved' | 'rating';
export type LeaderboardDataFilter = 'real' | 'all';

export interface LeaderboardEntry {
  rank: number;
  userId: number;
  displayName: string | null;
  college: string | null;
  totalSolved: number;
  weightedScore: number;
  bestRating: number | null;
  bestRatingPlatform: string | null;
  totalContests: number;
  hasMockData: boolean;
  realDataOnly: boolean;
  isCurrentUser: boolean;
}

export interface LeaderboardResponse {
  page: number;
  size: number;
  total: number;
  lastSyncedAt: string | null;
  entries: LeaderboardEntry[];
  myRank: number | null;
}

export interface MyRankResponse {
  userId: number;
  displayName: string | null;
  globalRank: number | null;
  collegeRank: number | null;
  college: string | null;
  totalSolved: number;
  weightedScore: number;
  bestRating: number | null;
  bestRatingPlatform: string | null;
  hasMockData: boolean;
  realDataOnly: boolean;
  lastSyncedAt: string | null;
}

export interface LeaderboardQueryParams {
  page?: number;
  size?: number;
  sort?: LeaderboardSort;
  dataFilter?: LeaderboardDataFilter;
  college?: string;
}

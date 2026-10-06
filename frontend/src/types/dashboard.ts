// ─── Stats API: GET /api/dashboard/stats ──────────────────────────────────────

export interface DashboardOverview {
  totalProblemsSolved: number | null;
  easySolved: number | null;
  mediumSolved: number | null;
  hardSolved: number | null;
  contestsParticipated: number | null;
  currentStreak: number | null;
  longestStreak: number | null;
  connectedPlatformsCount: number;
}

export interface PlatformStats {
  platform: string;         // matches Platform enum name e.g. "LEETCODE", "CODEFORCES"
  username: string;
  profileUrl: string | null;
  totalProblemsSolved: number | null;
  easySolved: number | null;
  mediumSolved: number | null;
  hardSolved: number | null;
  rating: number | null;
  rank: number | null;
  contestsParticipated: number | null;
  currentStreak: number | null;
  longestStreak: number | null;
  lastSyncedAt: string | null;  // ISO LocalDateTime serialized as string
  source: string;               // e.g. "LEETCODE_REAL", "CODEFORCES_REAL", "MOCK"
}

export interface DashboardStatsResponse {
  overview: DashboardOverview;
  platforms: PlatformStats[];
}

// ─── Analytics API: GET /api/dashboard/analytics ─────────────────────────────

export interface DifficultyMetric {
  count: number | null;
  percentage: number | null;
}

export interface PlatformDifficultyBreakdown {
  platform: string;
  easy: number | null;
  medium: number | null;
  hard: number | null;
  total: number | null;
}

export interface DifficultyAnalytics {
  totalProblemsSolved: number | null;
  easy: DifficultyMetric;
  medium: DifficultyMetric;
  hard: DifficultyMetric;
  platformBreakdown: PlatformDifficultyBreakdown[];
}

export interface PlatformComparison {
  platform: string;
  username: string;
  totalSolved: number | null;
  sharePercentage: number | null;
  rating: number | null;
  rank: number | null;
  contestsParticipated: number | null;
  currentStreak: number | null;
  longestStreak: number | null;
  source: string;
}

export interface PlatformContestBreakdown {
  platform: string;
  contests: number | null;
  rating: number | null;
  rank: number | null;
}

export interface ContestAnalytics {
  totalContests: number | null;
  platformBreakdown: PlatformContestBreakdown[];
}

export interface DashboardAnalyticsResponse {
  difficulty: DifficultyAnalytics;
  platformComparison: PlatformComparison[];
  contests: ContestAnalytics;
}

export type HistoryRange = '7d' | '30d' | '90d' | '1y';

export interface OverallHistoryPoint {
  date: string;
  totalSolved: number | null;
  easySolved: number | null;
  mediumSolved: number | null;
  hardSolved: number | null;
  observedPlatforms: number;
  platformsWithTotalSolved: number;
  platformsWithEasySolved: number;
  platformsWithMediumSolved: number;
  platformsWithHardSolved: number;
}

export interface PlatformHistoryPoint {
  date: string;
  totalSolved: number | null;
  totalSolvedChange: number | null;
  easySolved: number | null;
  easySolvedChange: number | null;
  mediumSolved: number | null;
  mediumSolvedChange: number | null;
  hardSolved: number | null;
  hardSolvedChange: number | null;
  rating: number | null;
}

export interface PlatformHistorySeries {
  platform: string;
  points: PlatformHistoryPoint[];
}

export interface HistoryFreshness {
  snapshotCount: number;
  latestSnapshotDate: string | null;
  lastUpdatedAt: string | null;
  ageHours: number | null;
}

export interface DashboardHistoryResponse {
  range: HistoryRange;
  snapshotDates: string[];
  overallSolved: OverallHistoryPoint[];
  platforms: PlatformHistorySeries[];
  freshness: HistoryFreshness;
}

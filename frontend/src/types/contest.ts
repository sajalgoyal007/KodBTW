import { PlatformContestBreakdown, ContestAnalytics } from './dashboard';

export interface ContestOverviewMetric {
  totalContests: number | null;
  highestRating: number | null;
  highestRatingPlatform: string | null;
  activeContestPlatformsCount: number;
}

export type { PlatformContestBreakdown, ContestAnalytics };

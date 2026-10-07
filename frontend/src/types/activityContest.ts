export interface DashboardActivityResponse {
  generatedAt: string;
  available: boolean;
  realDataOnly: boolean;
  message: string;
  platformsWithRealSnapshots: string[];
}

export interface RatingObservation {
  date: string;
  rating: number;
}

export interface PlatformRatingHistory {
  platform: string;
  observationCount: number;
  currentRating: number | null;
  latestSnapshotDate: string;
  highestObservedRating: number;
  lowestObservedRating: number;
  change: number | null;
  firstObservationDate: string;
  latestObservationDate: string;
  observations: RatingObservation[];
}

export interface DashboardContestIntelligenceResponse {
  generatedAt: string;
  realDataOnly: boolean;
  contestHistoryAvailable: boolean;
  contestHistoryMessage: string;
  ratingObservationCount: number;
  oldestObservationDate: string | null;
  latestObservationDate: string | null;
  lastUpdatedAt: string | null;
  platforms: PlatformRatingHistory[];
}

export interface InsightItem {
  type: string;
  category: string;
  severity: string;
  title: string;
  message: string;
  platform: string | null;
  metric: string | null;
  value: number | null;
  previousValue: number | null;
  change: number | null;
  date: string | null;
  comparisonDate: string | null;
  supportingValues: Record<string, number>;
}

export interface InsightsDataAvailability {
  snapshotCount: number;
  platformCount: number;
  oldestSnapshotDate: string | null;
  latestSnapshotDate: string | null;
  hasHistoricalComparison: boolean;
  realDataOnly: boolean;
}

export interface DashboardInsightsResponse {
  generatedAt: string;
  dataAvailability: InsightsDataAvailability;
  summary: string;
  strengths: InsightItem[];
  areasToImprove: InsightItem[];
  platformInsights: InsightItem[];
  progressInsights: InsightItem[];
  difficultyInsights: InsightItem[];
  ratingInsights: InsightItem[];
  streakInsights: InsightItem[];
}

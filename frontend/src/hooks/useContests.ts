import { useState, useEffect, useCallback, useRef } from 'react';
import { DashboardStatsResponse, DashboardAnalyticsResponse, PlatformStats } from '../types/dashboard';
import { ContestOverviewMetric } from '../types/contest';
import { getStats, getAnalytics } from '../services/api/dashboardApi';

interface UseContestsResult {
  stats: DashboardStatsResponse | null;
  analytics: DashboardAnalyticsResponse | null;
  overviewMetrics: ContestOverviewMetric;
  loading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
}

export function useContests(): UseContestsResult {
  const [stats, setStats] = useState<DashboardStatsResponse | null>(null);
  const [analytics, setAnalytics] = useState<DashboardAnalyticsResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const isFetchingRef = useRef<boolean>(false);

  const fetchData = useCallback(async () => {
    if (isFetchingRef.current) return;
    isFetchingRef.current = true;
    setLoading(true);
    setError(null);

    try {
      const [statsRes, analyticsRes] = await Promise.all([
        getStats(),
        getAnalytics(),
      ]);
      setStats(statsRes);
      setAnalytics(analyticsRes);
    } catch (err: any) {
      setError(err?.message || 'Failed to fetch contest tracking data.');
    } finally {
      setLoading(false);
      isFetchingRef.current = false;
    }
  }, []);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  // Compute derived contest metrics without fabricating values
  const platforms: PlatformStats[] = stats?.platforms || [];

  let highestRating: number | null = null;
  let highestRatingPlatform: string | null = null;
  let activeContestPlatformsCount = 0;

  platforms.forEach((p) => {
    const hasContestData = p.rating !== null || p.contestsParticipated !== null;
    if (hasContestData) {
      activeContestPlatformsCount++;
    }

    if (p.rating !== null && p.rating !== undefined) {
      if (highestRating === null || p.rating > highestRating) {
        highestRating = p.rating;
        highestRatingPlatform = p.platform;
      }
    }
  });

  const totalContests = analytics?.contests?.totalContests ?? stats?.overview?.contestsParticipated ?? null;

  const overviewMetrics: ContestOverviewMetric = {
    totalContests,
    highestRating,
    highestRatingPlatform,
    activeContestPlatformsCount,
  };

  return {
    stats,
    analytics,
    overviewMetrics,
    loading,
    error,
    refetch: fetchData,
  };
}

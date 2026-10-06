import { useState, useEffect, useCallback, useRef } from 'react';
import { DashboardStatsResponse, DashboardAnalyticsResponse } from '../types/dashboard';
import { getStats, getAnalytics } from '../services/api/dashboardApi';

interface UseDashboardResult {
  stats: DashboardStatsResponse | null;
  analytics: DashboardAnalyticsResponse | null;
  loading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
}

export function useDashboard(): UseDashboardResult {
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
      setError(err?.message || 'Failed to fetch dashboard data. Please try again.');
    } finally {
      setLoading(false);
      isFetchingRef.current = false;
    }
  }, []);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  return {
    stats,
    analytics,
    loading,
    error,
    refetch: fetchData,
  };
}

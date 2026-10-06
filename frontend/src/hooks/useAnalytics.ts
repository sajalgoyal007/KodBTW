import { useState, useEffect, useCallback, useRef } from 'react';
import { DashboardAnalyticsResponse } from '../types/dashboard';
import { fetchAnalytics } from '../services/api/analyticsApi';

interface UseAnalyticsResult {
  data: DashboardAnalyticsResponse | null;
  loading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
}

export function useAnalytics(): UseAnalyticsResult {
  const [data, setData] = useState<DashboardAnalyticsResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const isFetchingRef = useRef<boolean>(false);

  const loadData = useCallback(async () => {
    if (isFetchingRef.current) return;
    isFetchingRef.current = true;
    setLoading(true);
    setError(null);

    try {
      const res = await fetchAnalytics();
      setData(res);
    } catch (err: any) {
      setError(err?.message || 'Failed to fetch analytics data. Please try again.');
    } finally {
      setLoading(false);
      isFetchingRef.current = false;
    }
  }, []);

  useEffect(() => {
    loadData();
  }, [loadData]);

  return {
    data,
    loading,
    error,
    refetch: loadData,
  };
}

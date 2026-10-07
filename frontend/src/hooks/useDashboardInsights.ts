import { useCallback, useEffect, useState } from 'react';
import { DashboardInsightsResponse } from '../types/insights';
import { getDashboardInsights } from '../services/api/insightsApi';

export function useDashboardInsights() {
  const [data, setData] = useState<DashboardInsightsResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [retryCount, setRetryCount] = useState(0);

  const refetch = useCallback(() => setRetryCount((count) => count + 1), []);

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError(null);
    getDashboardInsights().then((response) => {
      if (active) setData(response);
    }).catch((err: any) => {
      if (active) setError(err?.message || 'Unable to load your insights. Please try again.');
    }).finally(() => {
      if (active) setLoading(false);
    });
    return () => { active = false; };
  }, [retryCount]);

  return { data, loading, error, refetch };
}

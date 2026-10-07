import { useCallback, useEffect, useState } from 'react';
import { DashboardHistoryResponse, HistoryRange } from '../types/history';
import { getDashboardHistory } from '../services/api/historyApi';

export function useDashboardHistory(range: HistoryRange) {
  const [data, setData] = useState<DashboardHistoryResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [retryCount, setRetryCount] = useState(0);

  const refetch = useCallback(() => setRetryCount((count) => count + 1), []);

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError(null);
    getDashboardHistory(range).then((result) => {
      if (active) setData(result);
    }).catch((err: any) => {
      if (active) setError(err?.message || 'Unable to load progress history. Please try again.');
    }).finally(() => {
      if (active) setLoading(false);
    });
    return () => { active = false; };
  }, [range, retryCount]);

  return { data, loading, error, refetch };
}

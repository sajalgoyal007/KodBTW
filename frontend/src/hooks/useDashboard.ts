import { useState, useEffect, useCallback, useRef } from 'react';
import { DashboardStatsResponse, DashboardAnalyticsResponse } from '../types/dashboard';
import { getStats, getAnalytics } from '../services/api/dashboardApi';
import { getAll, sync as syncAccount } from '../services/api/platformAccountApi';
import { PlatformAccountResponse } from '../types/platform';
import { getDashboardRefreshFeedback } from '../utils/platformStatsSource';

interface UseDashboardResult {
  stats: DashboardStatsResponse | null;
  analytics: DashboardAnalyticsResponse | null;
  accounts: PlatformAccountResponse[];
  loading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
  refreshAccount: (accountId: number) => Promise<boolean>;
  refreshingAccountId: number | null;
  refreshFeedback: { accountId: number; success: boolean; message: string } | null;
}

export function useDashboard(): UseDashboardResult {
  const [stats, setStats] = useState<DashboardStatsResponse | null>(null);
  const [analytics, setAnalytics] = useState<DashboardAnalyticsResponse | null>(null);
  const [accounts, setAccounts] = useState<PlatformAccountResponse[]>([]);
  const [refreshingAccountId, setRefreshingAccountId] = useState<number | null>(null);
  const [refreshFeedback, setRefreshFeedback] = useState<UseDashboardResult['refreshFeedback']>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const isFetchingRef = useRef<boolean>(false);

  const fetchData = useCallback(async (showLoading = true) => {
    if (isFetchingRef.current) return;
    isFetchingRef.current = true;
    if (showLoading) setLoading(true);
    setError(null);

    const results = await Promise.allSettled([getStats(), getAnalytics(), getAll()]);
    const [statsResult, analyticsResult, accountsResult] = results;
    if (statsResult.status === 'fulfilled') setStats(statsResult.value);
    if (analyticsResult.status === 'fulfilled') setAnalytics(analyticsResult.value);
    if (accountsResult.status === 'fulfilled') setAccounts(accountsResult.value);
    const failedCount = results.filter((result) => result.status === 'rejected').length;
    setError(failedCount === 3
      ? 'Dashboard data could not be loaded. Check your connection and retry.'
      : failedCount > 0 ? 'Some dashboard sections could not be updated. Available data is still shown.' : null);
    if (showLoading) setLoading(false);
    isFetchingRef.current = false;
  }, []);

  const refreshAccount = useCallback(async (accountId: number): Promise<boolean> => {
    setRefreshingAccountId(accountId);
    setRefreshFeedback(null);
    try {
      const result = await syncAccount(accountId);
      const feedback = getDashboardRefreshFeedback(result);
      setRefreshFeedback({
        accountId,
        ...feedback,
      });
      await fetchData(false);
      return feedback.success;
    } catch (err: any) {
      setRefreshFeedback({ accountId, success: false, message: err?.message || 'Unable to refresh stats.' });
      return false;
    } finally {
      setRefreshingAccountId(null);
    }
  }, [fetchData]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  return {
    stats,
    analytics,
    accounts,
    loading,
    error,
    refetch: fetchData,
    refreshAccount,
    refreshingAccountId,
    refreshFeedback,
  };
}

import { useState, useEffect, useCallback, useRef } from 'react';
import { DashboardStatsResponse, DashboardAnalyticsResponse } from '../types/dashboard';
import { getStats, getAnalytics } from '../services/api/dashboardApi';
import { getAll, sync as syncAccount } from '../services/api/platformAccountApi';
import { PlatformAccountResponse } from '../types/platform';

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

    try {
      const [statsRes, analyticsRes, accountsRes] = await Promise.all([
        getStats(),
        getAnalytics(),
        getAll(),
      ]);
      setStats(statsRes);
      setAnalytics(analyticsRes);
      setAccounts(accountsRes);
    } catch (err: any) {
      setError(err?.message || 'Failed to fetch dashboard data. Please try again.');
    } finally {
      if (showLoading) setLoading(false);
      isFetchingRef.current = false;
    }
  }, []);

  const refreshAccount = useCallback(async (accountId: number): Promise<boolean> => {
    setRefreshingAccountId(accountId);
    setRefreshFeedback(null);
    try {
      const result = await syncAccount(accountId);
      const failed = result.syncStatus.status === 'FAILED';
      const alreadyRunning = result.syncStatus.status === 'RUNNING';
      setRefreshFeedback({
        accountId,
        success: !failed,
        message: failed
          ? (result.syncStatus.failureMessage || 'Refresh failed.')
          : alreadyRunning ? 'Stats refresh is already in progress.' : 'Stats refreshed successfully.',
      });
      await fetchData(false);
      return !failed;
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

import { useState, useEffect, useCallback, useRef } from 'react';
import { DashboardStatsResponse, DashboardAnalyticsResponse } from '../types/dashboard';
import { getStats, getAnalytics } from '../services/api/dashboardApi';
import { getAll, sync as syncAccount } from '../services/api/platformAccountApi';
import { ApiError } from '../services/api/apiClient';
import { PlatformAccountResponse } from '../types/platform';
import { getDashboardRefreshFeedback } from '../utils/platformStatsSource';
import { createQueuedRefreshRunner } from '../utils/queuedRefresh';
import { createSingleFlightGate } from '../utils/requestCoordination';

interface UseDashboardResult {
  stats: DashboardStatsResponse | null;
  analytics: DashboardAnalyticsResponse | null;
  accounts: PlatformAccountResponse[];
  accountsLoaded: boolean;
  accountsLoadError: boolean;
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
  const [accountsLoaded, setAccountsLoaded] = useState(false);
  const [accountsLoadError, setAccountsLoadError] = useState(false);
  const [refreshingAccountId, setRefreshingAccountId] = useState<number | null>(null);
  const [refreshFeedback, setRefreshFeedback] = useState<UseDashboardResult['refreshFeedback']>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchRunnerRef = useRef(createQueuedRefreshRunner());
  const accountRefreshGateRef = useRef(createSingleFlightGate());

  const fetchData = useCallback((showLoading = true) => fetchRunnerRef.current(async () => {
    if (showLoading) setLoading(true);
    setError(null);

    try {
      const results = await Promise.allSettled([getStats(), getAnalytics(), getAll()]);
      const [statsResult, analyticsResult, accountsResult] = results;
      if (statsResult.status === 'fulfilled') setStats(statsResult.value);
      if (analyticsResult.status === 'fulfilled') setAnalytics(analyticsResult.value);
      if (accountsResult.status === 'fulfilled') {
        setAccounts(accountsResult.value);
        setAccountsLoaded(true);
        setAccountsLoadError(false);
      } else {
        setAccountsLoadError(true);
      }
      const failedCount = results.filter((result) => result.status === 'rejected').length;
      setError(failedCount === 3
        ? 'Dashboard data could not be loaded. Check your connection and retry.'
        : failedCount > 0 ? 'Some dashboard sections could not be updated. Available data is still shown.' : null);
    } finally {
      if (showLoading) setLoading(false);
    }
  }, !showLoading), []);

  const refreshAccount = useCallback(async (accountId: number): Promise<boolean> => {
    if (!accountRefreshGateRef.current.tryEnter()) return false;
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
    } catch (error) {
      const message = error instanceof ApiError ? error.message : 'Unable to refresh stats. Please try again.';
      setRefreshFeedback({ accountId, success: false, message });
      return false;
    } finally {
      accountRefreshGateRef.current.leave();
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
    accountsLoaded,
    accountsLoadError,
    loading,
    error,
    refetch: fetchData,
    refreshAccount,
    refreshingAccountId,
    refreshFeedback,
  };
}

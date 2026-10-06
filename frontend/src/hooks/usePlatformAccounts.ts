import { useState, useEffect, useCallback } from 'react';
import {
  PlatformAccountResponse,
  PlatformAccountRequest,
} from '../types/platform';
import { PlatformStats } from '../types/dashboard';
import {
  getAll,
  create,
  update,
  deleteAccount as apiDeleteAccount,
  getStats as apiGetStats,
} from '../services/api/platformAccountApi';

interface UsePlatformAccountsResult {
  accounts: PlatformAccountResponse[];
  loading: boolean;
  actionLoading: boolean;
  error: string | null;
  fetchAccounts: () => Promise<void>;
  createAccount: (data: PlatformAccountRequest) => Promise<PlatformAccountResponse>;
  updateAccount: (id: number, data: PlatformAccountRequest) => Promise<PlatformAccountResponse>;
  deleteAccount: (id: number) => Promise<void>;
  fetchStats: (id: number) => Promise<PlatformStats>;
}

export function usePlatformAccounts(): UsePlatformAccountsResult {
  const [accounts, setAccounts] = useState<PlatformAccountResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [actionLoading, setActionLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const fetchAccounts = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getAll();
      setAccounts(data);
    } catch (err: any) {
      setError(err?.message || 'Failed to fetch platform accounts.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchAccounts();
  }, [fetchAccounts]);

  const createAccount = async (
    data: PlatformAccountRequest
  ): Promise<PlatformAccountResponse> => {
    setActionLoading(true);
    try {
      const newAcc = await create(data);
      setAccounts((prev) => [...prev, newAcc]);
      return newAcc;
    } catch (err: any) {
      throw err;
    } finally {
      setActionLoading(false);
    }
  };

  const updateAccount = async (
    id: number,
    data: PlatformAccountRequest
  ): Promise<PlatformAccountResponse> => {
    setActionLoading(true);
    try {
      const updated = await update(id, data);
      setAccounts((prev) => prev.map((acc) => (acc.id === id ? updated : acc)));
      return updated;
    } catch (err: any) {
      throw err;
    } finally {
      setActionLoading(false);
    }
  };

  const deleteAccount = async (id: number): Promise<void> => {
    setActionLoading(true);
    try {
      await apiDeleteAccount(id);
      setAccounts((prev) => prev.filter((acc) => acc.id !== id));
    } catch (err: any) {
      throw err;
    } finally {
      setActionLoading(false);
    }
  };

  const fetchStats = async (id: number): Promise<PlatformStats> => {
    return apiGetStats(id);
  };

  return {
    accounts,
    loading,
    actionLoading,
    error,
    fetchAccounts,
    createAccount,
    updateAccount,
    deleteAccount,
    fetchStats,
  };
}

import { useState, useEffect, useCallback, useRef } from 'react';
import {
  LeaderboardResponse,
  MyRankResponse,
  LeaderboardSort,
  LeaderboardDataFilter,
} from '../types/leaderboard';
import { getLeaderboard, getMyRank } from '../services/api/leaderboardApi';
import { ApiError } from '../services/api/apiClient';
import { createLatestRequestTracker } from '../utils/requestCoordination';

export interface UseLeaderboardResult {
  leaderboard: LeaderboardResponse | null;
  myRank: MyRankResponse | null;
  loading: boolean;
  error: string | null;
  page: number;
  size: number;
  sort: LeaderboardSort;
  dataFilter: LeaderboardDataFilter;
  college: string;
  setPage: (page: number) => void;
  setSort: (sort: LeaderboardSort) => void;
  setDataFilter: (filter: LeaderboardDataFilter) => void;
  setCollege: (college: string) => void;
  refetch: () => Promise<void>;
}

export function useLeaderboard(): UseLeaderboardResult {
  const [leaderboard, setLeaderboard] = useState<LeaderboardResponse | null>(null);
  const [myRank, setMyRank] = useState<MyRankResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const [page, setPage] = useState<number>(0);
  const [size] = useState<number>(25);
  const [sort, setSort] = useState<LeaderboardSort>('score');
  const [dataFilter, setDataFilter] = useState<LeaderboardDataFilter>('real');
  const [college, setCollege] = useState<string>('');

  const requestTrackerRef = useRef(createLatestRequestTracker());

  const fetchData = useCallback(async () => {
    const requestVersion = requestTrackerRef.current.begin();
    setLoading(true);
    setError(null);

    try {
      const [lbRes, myRankRes] = await Promise.all([
        getLeaderboard({
          page,
          size,
          sort,
          dataFilter,
          college: college.trim() || undefined,
        }),
        getMyRank().catch(() => null),
      ]);
      if (!requestTrackerRef.current.isCurrent(requestVersion)) return;
      setLeaderboard(lbRes);
      if (myRankRes) {
        setMyRank(myRankRes);
      }
    } catch (err: unknown) {
      if (!requestTrackerRef.current.isCurrent(requestVersion)) return;
      setError(err instanceof ApiError ? err.message : 'Failed to fetch leaderboard data.');
    } finally {
      if (requestTrackerRef.current.isCurrent(requestVersion)) setLoading(false);
    }
  }, [page, size, sort, dataFilter, college]);

  useEffect(() => {
    fetchData();
    return () => requestTrackerRef.current.invalidate();
  }, [fetchData]);

  const handleSetSort = (newSort: LeaderboardSort) => {
    setSort(newSort);
    setPage(0);
  };

  const handleSetDataFilter = (newFilter: LeaderboardDataFilter) => {
    setDataFilter(newFilter);
    setPage(0);
  };

  const handleSetCollege = (newCollege: string) => {
    setCollege(newCollege);
    setPage(0);
  };

  return {
    leaderboard,
    myRank,
    loading,
    error,
    page,
    size,
    sort,
    dataFilter,
    college,
    setPage,
    setSort: handleSetSort,
    setDataFilter: handleSetDataFilter,
    setCollege: handleSetCollege,
    refetch: fetchData,
  };
}

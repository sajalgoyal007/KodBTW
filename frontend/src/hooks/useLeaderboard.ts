import { useState, useEffect, useCallback, useRef } from 'react';
import {
  LeaderboardResponse,
  MyRankResponse,
  LeaderboardSort,
  LeaderboardDataFilter,
} from '../types/leaderboard';
import { getLeaderboard, getMyRank } from '../services/api/leaderboardApi';

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

  const isFetchingRef = useRef<boolean>(false);

  const fetchData = useCallback(async () => {
    if (isFetchingRef.current) return;
    isFetchingRef.current = true;
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
      setLeaderboard(lbRes);
      if (myRankRes) {
        setMyRank(myRankRes);
      }
    } catch (err: any) {
      setError(err?.message || 'Failed to fetch leaderboard data.');
    } finally {
      setLoading(false);
      isFetchingRef.current = false;
    }
  }, [page, size, sort, dataFilter, college]);

  useEffect(() => {
    fetchData();
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

import React from 'react';
import { AppNavbar } from '../components/layout/AppNavbar';
import { useLeaderboard } from '../hooks/useLeaderboard';
import { MyRankCard } from '../components/leaderboard/MyRankCard';
import { LeaderboardFilters } from '../components/leaderboard/LeaderboardFilters';
import { LeaderboardTable } from '../components/leaderboard/LeaderboardTable';
import { Trophy, RefreshCw, AlertCircle, Clock } from 'lucide-react';

export const LeaderboardPage: React.FC = () => {
  const {
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
    setSort,
    setDataFilter,
    setCollege,
    refetch,
  } = useLeaderboard();

  const entries = (leaderboard?.entries || []).filter((entry) => entry.realDataOnly && !entry.hasMockData);
  const safeMyRank = myRank?.realDataOnly && !myRank.hasMockData ? myRank : null;
  const total = leaderboard?.total || 0;
  const lastSyncedAt = leaderboard?.lastSyncedAt;

  // Check if data is stale (> 24 hours old)
  const isStale = React.useMemo(() => {
    if (!lastSyncedAt) return false;
    const syncedTime = new Date(lastSyncedAt).getTime();
    const now = Date.now();
    return now - syncedTime > 24 * 60 * 60 * 1000;
  }, [lastSyncedAt]);

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <AppNavbar />

      <main
        style={{
          flex: 1,
          maxWidth: '1280px',
          width: '100%',
          margin: '0 auto',
          padding: '2rem 1.5rem',
          display: 'flex',
          flexDirection: 'column',
          gap: '2rem',
        }}
      >
        {/* Top Header */}
        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            flexWrap: 'wrap',
            gap: '1rem',
          }}
        >
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.625rem' }}>
              <h1 style={{ fontSize: '1.75rem', fontWeight: 800, letterSpacing: '-0.02em', margin: 0 }}>
                Leaderboard
              </h1>
              {!loading && (
                <div className="badge badge-primary">
                  <Trophy size={12} />
                  <span>{total} Developer{total === 1 ? '' : 's'}</span>
                </div>
              )}
            </div>
            <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.875rem', margin: '0.375rem 0 0 0' }}>
                Rankings use live statistics from LeetCode and Codeforces. Total platform solves may count the same problem more than once; platform ratings use different scales.
            </p>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => refetch()}
              disabled={loading}
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '0.5rem',
                fontSize: '0.8125rem',
                padding: '0.5rem 0.875rem',
              }}
            >
              <RefreshCw size={14} className={loading ? 'animate-spin' : ''} />
              <span>Refresh</span>
            </button>
          </div>
        </div>

        {/* Stale Data Warning Banner */}
        {isStale && (
          <div
            style={{
              padding: '0.75rem 1rem',
              backgroundColor: 'rgba(234, 179, 8, 0.1)',
              border: '1px solid rgba(234, 179, 8, 0.3)',
              borderRadius: 'var(--radius-md)',
              display: 'flex',
              alignItems: 'center',
              gap: '0.625rem',
              color: 'var(--color-warning)',
              fontSize: '0.8125rem',
            }}
          >
            <Clock size={16} style={{ flexShrink: 0 }} />
            <span>
              The leaderboard data was last updated more than 24 hours ago ({new Date(lastSyncedAt!).toLocaleString()}).
              Scheduled sync will refresh standings during the next nightly run.
            </span>
          </div>
        )}

        {/* Error Alert */}
        {error && (
          <div
            style={{
              padding: '1rem',
              backgroundColor: 'rgba(239, 68, 68, 0.1)',
              border: '1px solid rgba(239, 68, 68, 0.3)',
              borderRadius: 'var(--radius-md)',
              display: 'flex',
              alignItems: 'center',
              gap: '0.75rem',
              color: 'var(--color-error)',
              fontSize: '0.875rem',
            }}
          >
            <AlertCircle size={18} style={{ flexShrink: 0 }} />
            <div>
              <strong>Failed to load leaderboard:</strong> {error}
            </div>
          </div>
        )}

        {/* Personal Standing Card */}
        <MyRankCard myRank={safeMyRank} loading={loading} />

        {/* Filter Controls */}
        <LeaderboardFilters
          sort={sort}
          dataFilter={dataFilter}
          college={college}
          onSortChange={setSort}
          onDataFilterChange={setDataFilter}
          onCollegeChange={setCollege}
        />

        {/* Leaderboard Table with Pagination */}
        <LeaderboardTable
          entries={entries}
          total={total}
          page={page}
          size={size}
          loading={loading}
          onPageChange={setPage}
        />
      </main>
    </div>
  );
};

import React from 'react';
import { useNavigate } from 'react-router-dom';
import { AppNavbar } from '../components/layout/AppNavbar';
import { useContests } from '../hooks/useContests';
import { ContestOverviewCards } from '../components/contests/ContestOverviewCards';
import { ContestPlatformTable } from '../components/contests/ContestPlatformTable';
import { ContestRatingComparison } from '../components/contests/ContestRatingComparison';
import {
  Trophy,
  RefreshCw,
  Layers,
  AlertCircle,
  PlusCircle,
} from 'lucide-react';

export const ContestsPage: React.FC = () => {
  const navigate = useNavigate();
  const { stats, overviewMetrics, loading, error, refetch } = useContests();

  const platforms = stats?.platforms || [];
  const isEmpty = !loading && !error && platforms.length === 0;

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
              <h1 style={{ fontSize: '1.75rem', fontWeight: 800, letterSpacing: '-0.02em' }}>
                Contests & Tracking
              </h1>
              {!loading && !isEmpty && (
                <div className="badge badge-primary">
                  <Trophy size={12} />
                  <span>{overviewMetrics.activeContestPlatformsCount} Competitive Platform{overviewMetrics.activeContestPlatformsCount > 1 ? 's' : ''}</span>
                </div>
              )}
            </div>
            <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.9375rem', marginTop: '0.25rem' }}>
              Review platform-specific contest ratings and saved rating snapshots. Contest event history is currently unavailable.
            </p>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <button
              onClick={() => refetch()}
              disabled={loading}
              className="btn btn-secondary"
              style={{ padding: '0.5rem 0.875rem', fontSize: '0.8125rem' }}
              title="Refresh Contests"
            >
              <RefreshCw size={14} className={loading ? 'spinner' : ''} />
              <span>Refresh</span>
            </button>
          </div>
        </div>

        {/* Loading Skeletons */}
        {loading && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1.75rem' }}>
            <div
              style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
                gap: '1rem',
              }}
            >
              {[1, 2, 3].map((i) => (
                <div key={i} className="card skeleton" style={{ height: '110px' }} />
              ))}
            </div>
            <div className="card skeleton" style={{ height: '240px' }} />
            <div className="card skeleton" style={{ height: '200px' }} />
          </div>
        )}

        {/* Error State */}
        {!loading && error && (
          <div
            className="card"
            style={{
              padding: '2.5rem',
              textAlign: 'center',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: '1rem',
              borderColor: 'rgba(239, 68, 68, 0.4)',
              backgroundColor: 'rgba(239, 68, 68, 0.05)',
            }}
          >
            <div
              style={{
                width: '48px',
                height: '48px',
                borderRadius: '50%',
                backgroundColor: 'rgba(239, 68, 68, 0.15)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: 'var(--color-error)',
              }}
            >
              <AlertCircle size={28} />
            </div>
            <div>
              <h3 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '0.375rem' }}>
                Unable to Load Contest Data
              </h3>
              <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.9375rem', maxWidth: '480px' }}>
                {error}
              </p>
            </div>
            <button
              onClick={() => refetch()}
              className="btn btn-primary"
              style={{ marginTop: '0.5rem' }}
            >
              <RefreshCw size={16} /> Retry
            </button>
          </div>
        )}

        {/* Empty State */}
        {isEmpty && (
          <div
            className="card"
            style={{
              padding: '4rem 2rem',
              textAlign: 'center',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: '1.25rem',
              maxWidth: '560px',
              margin: '2rem auto',
            }}
          >
            <div
              style={{
                width: '56px',
                height: '56px',
                borderRadius: 'var(--radius-lg)',
                backgroundColor: 'rgba(235, 115, 18, 0.1)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: 'var(--color-primary)',
              }}
            >
              <Layers size={28} />
            </div>
            <div>
              <h2 style={{ fontSize: '1.5rem', fontWeight: 700, marginBottom: '0.5rem' }}>
                No Contest Profiles Connected
              </h2>
              <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.9375rem', lineHeight: 1.5 }}>
                Contest statistics and ratings become available after connecting your competitive programming profiles like LeetCode or Codeforces.
              </p>
            </div>
            <button
              onClick={() => navigate('/coding-profiles')}
              className="btn btn-primary"
              style={{ padding: '0.75rem 1.5rem', fontSize: '0.9375rem' }}
            >
              <PlusCircle size={18} /> Connect Platforms
            </button>
          </div>
        )}

        {/* Successful Contest Content */}
        {!loading && !error && !isEmpty && (
          <>
            {/* Overview Metrics Cards */}
            <ContestOverviewCards metrics={overviewMetrics} />

            {/* Platform Contest Comparison Table */}
            <ContestPlatformTable platforms={platforms} />

            {/* Relative Rating Comparison Visualization */}
            <ContestRatingComparison platforms={platforms} />
          </>
        )}
      </main>
    </div>
  );
};

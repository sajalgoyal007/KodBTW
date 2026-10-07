import React from 'react';
import { useNavigate } from 'react-router-dom';
import { AppNavbar } from '../components/layout/AppNavbar';
import { useAnalytics } from '../hooks/useAnalytics';
import { DifficultyDistributionCard } from '../components/analytics/DifficultyDistributionCard';
import { PlatformDifficultyTable } from '../components/analytics/PlatformDifficultyTable';
import { PlatformShareCard } from '../components/analytics/PlatformShareCard';
import { PlatformStandingTable } from '../components/analytics/PlatformStandingTable';
import { StreakComparisonCard } from '../components/analytics/StreakComparisonCard';
import { DashboardHistorySection } from '../components/analytics/DashboardHistorySection';
import { DashboardInsightsSection } from '../components/analytics/DashboardInsightsSection';
import { ActivityContestIntelligenceSection } from '../components/analytics/ActivityContestIntelligenceSection';
import { ContestSummary } from '../components/dashboard/ContestSummary';
import {
  RefreshCw,
  Layers,
  AlertCircle,
  PlusCircle,
  TrendingUp,
} from 'lucide-react';

export const AnalyticsPage: React.FC = () => {
  const navigate = useNavigate();
  const { data, loading, error, refetch } = useAnalytics();

  const difficulty = data?.difficulty;
  const platformComparison = data?.platformComparison || [];
  const contests = data?.contests || null;

  const totalProblems = difficulty?.totalProblemsSolved ?? 0;
  const isEmpty =
    !loading &&
    !error &&
    (!data || platformComparison.length === 0 || totalProblems === 0);

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
                Analytics & Insights
              </h1>
              {!loading && !isEmpty && (
                <div className="badge badge-primary">
                  <TrendingUp size={12} />
                  <span>{platformComparison.length} Active Platform{platformComparison.length > 1 ? 's' : ''}</span>
                </div>
              )}
            </div>
            <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.9375rem', marginTop: '0.25rem' }}>
              Deep comparative metrics, difficulty distributions, and competitive standings
            </p>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <button
              onClick={() => refetch()}
              disabled={loading}
              className="btn btn-secondary"
              style={{ padding: '0.5rem 0.875rem', fontSize: '0.8125rem' }}
              title="Refresh Analytics"
            >
              <RefreshCw size={14} className={loading ? 'spinner' : ''} />
              <span>Refresh</span>
            </button>
          </div>
        </div>

        {/* Loading Skeletons */}
        {loading && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1.75rem' }}>
            {/* Row 1 Skeletons */}
            <div
              style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(360px, 1fr))',
                gap: '1.5rem',
              }}
            >
              <div className="card skeleton" style={{ height: '260px' }} />
              <div className="card skeleton" style={{ height: '260px' }} />
            </div>

            {/* Row 2 Skeleton */}
            <div className="card skeleton" style={{ height: '220px' }} />

            {/* Row 3 Skeletons */}
            <div
              style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(360px, 1fr))',
                gap: '1.5rem',
              }}
            >
              <div className="card skeleton" style={{ height: '260px' }} />
              <div className="card skeleton" style={{ height: '260px' }} />
            </div>

            {/* Row 4 Skeleton */}
            <div className="card skeleton" style={{ height: '240px' }} />
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
                Unable to Load Analytics
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
                No Analytics Available
              </h2>
              <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.9375rem', lineHeight: 1.5 }}>
                Connect your competitive programming accounts to unlock deep difficulty breakdowns, platform shares, and cross-platform performance comparisons.
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

        {/* Analytics Content */}
        {!loading && !error && !isEmpty && data && (
          <>
            {/* Row 1: Difficulty Distribution + Platform Problem Share */}
            <div
              style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(360px, 1fr))',
                gap: '1.5rem',
              }}
            >
              <DifficultyDistributionCard difficulty={difficulty || null} />
              <PlatformShareCard platformComparison={platformComparison} />
            </div>

            {/* Row 2: Platform Difficulty Breakdown Table */}
            {difficulty?.platformBreakdown && (
              <PlatformDifficultyTable platformBreakdown={difficulty.platformBreakdown} />
            )}

            {/* Row 3: Contest Summary + Streak Comparison */}
            <div
              style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(360px, 1fr))',
                gap: '1.5rem',
              }}
            >
              <ContestSummary contests={contests} />
              <StreakComparisonCard platformComparison={platformComparison} />
            </div>

            {/* Row 4: Comprehensive Platform Standing Table */}
            <PlatformStandingTable platformComparison={platformComparison} />
          </>
        )}

        <DashboardInsightsSection />
        <DashboardHistorySection />
        <ActivityContestIntelligenceSection />
      </main>
    </div>
  );
};

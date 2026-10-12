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
import { getAnalyticsMetricAvailability, getAnalyticsPageState } from '../utils/analyticsPresentation';
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
  const { hasProfiles, hasSolvedMetrics, hasRatingMetrics, hasStreakMetrics, hasContestMetrics } =
    getAnalyticsMetricAvailability(platformComparison, difficulty, contests);
  const hasMetrics = hasSolvedMetrics || hasRatingMetrics || hasStreakMetrics || hasContestMetrics;
  const viewState = getAnalyticsPageState({ loading, error, hasData: data !== null, hasMetrics });

  return (
    <div className="analytics-page">
      <AppNavbar />

      <main className="analytics-main">
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
            <div className="analytics-title-row">
              <h1>
                Analytics & Insights
              </h1>
              {data && (
                <div className="badge badge-primary">
                  <TrendingUp size={12} />
                  <span>{platformComparison.length} Profile{platformComparison.length === 1 ? '' : 's'} compared</span>
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
        {viewState === 'loading' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1.75rem' }}>
            {/* Row 1 Skeletons */}
            <div
              style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(min(100%, 320px), 1fr))',
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
                gridTemplateColumns: 'repeat(auto-fit, minmax(min(100%, 320px), 1fr))',
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
        {viewState === 'error' && error && (
          <div
            className="card"
            style={{
              padding: '2.5rem',
              textAlign: 'center',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: '1rem',
              borderColor: 'var(--color-error-border)',
              backgroundColor: 'var(--color-error-soft)',
            }}
          >
            <div
              style={{
                width: '48px',
                height: '48px',
                borderRadius: '50%',
                backgroundColor: 'var(--color-error-soft)',
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
        {viewState === 'empty' && (
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
                backgroundColor: 'var(--color-primary-soft)',
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
                {hasProfiles
                  ? 'Your connected profiles do not have comparable statistics available yet. Review their sync status in Coding Profiles and try again after a supported sync.'
                  : 'Connect your competitive programming accounts to unlock difficulty breakdowns, platform shares, and cross-platform performance comparisons.'}
              </p>
            </div>
            <button
              onClick={() => navigate('/coding-profiles')}
              className="btn btn-primary"
              style={{ padding: '0.75rem 1.5rem', fontSize: '0.9375rem' }}
            >
              <PlusCircle size={18} /> {hasProfiles ? 'View Coding Profiles' : 'Connect Platforms'}
            </button>
          </div>
        )}

        {/* Analytics Content */}
        {data && viewState !== 'empty' && viewState !== 'loading' && (
          <>
            {viewState === 'refreshing' && (
              <p className="analytics-data-notice" role="status" aria-live="polite">Updating analytics. Your last loaded results remain visible until the refresh finishes.</p>
            )}
            {viewState === 'stale-error' && error && (
              <div className="analytics-data-notice analytics-data-notice--error" role="alert">
                <span>{error} Showing the last loaded results.</span>
                <button type="button" className="btn btn-secondary" onClick={() => void refetch()}>Retry</button>
              </div>
            )}
            {/* Row 1: Difficulty Distribution + Platform Problem Share */}
            {hasSolvedMetrics && <div
              style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(min(100%, 320px), 1fr))',
                gap: '1.5rem',
              }}
            >
              <DifficultyDistributionCard difficulty={difficulty || null} />
              <PlatformShareCard platformComparison={platformComparison} />
            </div>}

            {/* Row 2: Platform Difficulty Breakdown Table */}
            {difficulty?.platformBreakdown?.length && (
              <PlatformDifficultyTable platformBreakdown={difficulty.platformBreakdown} />
            )}

            {/* Row 3: Contest Summary + Streak Comparison */}
            {(hasContestMetrics || hasStreakMetrics) && <div
              style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(min(100%, 320px), 1fr))',
                gap: '1.5rem',
              }}
            >
              {hasContestMetrics && <ContestSummary contests={contests} />}
              {hasStreakMetrics && <StreakComparisonCard platformComparison={platformComparison} />}
            </div>}

            {/* Row 4: Comprehensive Platform Standing Table */}
            {platformComparison.length > 0 && <PlatformStandingTable platformComparison={platformComparison} />}
          </>
        )}

        <DashboardInsightsSection />
        <DashboardHistorySection />
        <ActivityContestIntelligenceSection />
      </main>
    </div>
  );
};

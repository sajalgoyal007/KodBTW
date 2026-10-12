import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useDashboard } from '../hooks/useDashboard';
import { AppNavbar } from '../components/layout/AppNavbar';
import { MetricCard } from '../components/dashboard/MetricCard';
import { DifficultyBreakdown } from '../components/dashboard/DifficultyBreakdown';
import { PlatformCard } from '../components/dashboard/PlatformCard';
import { PlatformShare } from '../components/dashboard/PlatformShare';
import { ContestSummary } from '../components/dashboard/ContestSummary';
import { getDashboardDisplayState, hasAvailablePlatformStats, hasPartialPlatformStats } from '../utils/dashboardPresentation';
import { AlertCircle, CheckCircle2, Flame, Layers, PlusCircle, RefreshCw, Trophy } from 'lucide-react';

export const DashboardPage: React.FC = () => {
  const navigate = useNavigate();
  const { stats, analytics, accounts, accountsLoaded, accountsLoadError, loading, error, refetch, refreshAccount, refreshingAccountId, refreshFeedback } = useDashboard();

  const overview = stats?.overview;
  const platforms = stats?.platforms ?? [];
  const hasAvailableStats = platforms.some(hasAvailablePlatformStats);
  const hasConnectedAccounts = accounts.length > 0 || (overview?.connectedPlatformsCount ?? 0) > 0;
  const displayState = getDashboardDisplayState({
    loading,
    error,
    hasStatsResponse: stats !== null,
    hasAvailableStats,
    hasConnectedAccounts,
    accountsLoaded,
    accountsLoadError,
  });
  const accountByPlatform = new Map<string, (typeof accounts)[number]>(accounts.map((account) => [account.platform, account]));
  const partialStats = hasPartialPlatformStats(platforms, Math.max(overview?.connectedPlatformsCount ?? 0, accounts.length));

  const platformCards = platforms.map((platform) => {
    const account = accountByPlatform.get(platform.platform);
    const feedback = refreshFeedback && refreshFeedback.accountId === account?.id
      ? { success: refreshFeedback.success, message: refreshFeedback.message }
      : null;
    return (
      <PlatformCard
        key={`${platform.platform}-${platform.username}`}
        platform={platform}
        account={account}
        refreshing={account?.id === refreshingAccountId}
        refreshDisabled={refreshingAccountId !== null}
        feedback={feedback}
        onRefresh={(accountId) => { void refreshAccount(accountId); }}
      />
    );
  });

  return (
    <div className="dashboard-page">
      <AppNavbar />
      <main className="dashboard-main" aria-label="Dashboard" aria-busy={loading}>
        {loading && (
          <div className="dashboard-loading" role="status" aria-live="polite">
            <span className="sr-only">Loading your dashboard</span>
            <div className="dashboard-skeleton-metrics" aria-hidden="true">
              {[1, 2, 3, 4, 5].map((item) => <div key={item} className="card skeleton" />)}
            </div>
            <div className="dashboard-skeleton-panels" aria-hidden="true">
              <div className="card skeleton" />
              <div className="card skeleton" />
            </div>
            <div className="dashboard-skeleton-platforms" aria-hidden="true">
              {[1, 2, 3].map((item) => <div key={item} className="card skeleton" />)}
            </div>
          </div>
        )}

        {displayState === 'error' && (
          <section className="card dashboard-state dashboard-state-error" role="alert">
            <span className="dashboard-state-icon"><AlertCircle size={25} aria-hidden="true" /></span>
            <div>
              <h1>Unable to load your dashboard</h1>
              <p>{error}</p>
            </div>
            <button onClick={() => void refetch()} className="btn btn-primary"><RefreshCw size={16} aria-hidden="true" /> Retry</button>
          </section>
        )}

        {!loading && error && displayState !== 'error' && (
          <div className="inline-notice" role="status"><AlertCircle size={16} aria-hidden="true" />{error}<button className="btn btn-secondary" onClick={() => void refetch()}>Retry</button></div>
        )}

        {displayState === 'no-accounts' && (
          <section className="card dashboard-state dashboard-state-empty">
            <span className="dashboard-state-icon"><Layers size={27} aria-hidden="true" /></span>
            <div>
              <h1>No coding profiles yet</h1>
              <p>Connect a platform profile to bring your coding progress together. Only statistics the source provides will appear here.</p>
            </div>
            <button className="btn btn-primary" onClick={() => navigate('/coding-profiles')}><PlusCircle size={18} aria-hidden="true" /> Connect a profile</button>
          </section>
        )}

        {displayState === 'accounts-unconfirmed' && !loading && (
          <section className="card dashboard-state" role="status">
            <span className="dashboard-state-icon"><Layers size={25} aria-hidden="true" /></span>
            <div><h1>{error ? 'Your profile list could not be confirmed' : 'Checking your connected profiles'}</h1><p>{error ?? 'Your profile list has not finished loading yet.'}</p></div>
            <button className="btn btn-secondary" onClick={() => void refetch()}><RefreshCw size={16} aria-hidden="true" /> Retry</button>
          </section>
        )}

        {displayState === 'connected-unavailable' && (
          <section className="card dashboard-state dashboard-state-unavailable" role="status">
            <span className="dashboard-state-icon"><AlertCircle size={25} aria-hidden="true" /></span>
            <div>
              <h1>Profiles connected; statistics unavailable</h1>
              <p>Your connected accounts are still here. One or more sources may not provide live statistics yet, or the first sync has not completed. No missing metric is treated as zero.</p>
            </div>
            <div className="dashboard-state-actions">
              <button className="btn btn-secondary" onClick={() => void refetch()}><RefreshCw size={16} aria-hidden="true" /> Reload dashboard</button>
              <button className="btn btn-ghost" onClick={() => navigate('/coding-profiles')}>Manage profiles</button>
            </div>
          </section>
        )}

        {!loading && displayState === 'ready' && overview && (
          <>
            <section className="dashboard-section" aria-labelledby="dashboard-overview-title">
              <div className="dashboard-section-heading">
                <div>
                  <h1 id="dashboard-overview-title">Your coding overview</h1>
                  <p>A clear view of the progress your connected platforms report.</p>
                </div>
                <span className="badge badge-muted"><CheckCircle2 size={13} aria-hidden="true" />{overview.connectedPlatformsCount} connected profile{overview.connectedPlatformsCount !== 1 ? 's' : ''}</span>
              </div>
              <div className="dashboard-metrics-grid">
                <MetricCard title="Total platform solves" value={overview.totalProblemsSolved} subtitle="The same problem may count on more than one platform." icon={<CheckCircle2 size={20} />} accentColor="var(--color-primary)" />
                <MetricCard title="Contest participations" value={overview.contestsParticipated} subtitle="Reported across connected platforms" icon={<Trophy size={20} />} accentColor="var(--color-chart-2)" />
                <MetricCard title="Current streak" value={overview.currentStreak !== null ? `${overview.currentStreak} days` : null} subtitle="Active daily solving streak" icon={<Flame size={20} />} accentColor="var(--color-primary)" />
                <MetricCard title="Longest streak" value={overview.longestStreak !== null ? `${overview.longestStreak} days` : null} subtitle="All-time personal record" icon={<Flame size={20} />} accentColor="var(--color-warning)" />
                <MetricCard title="Platforms" value={overview.connectedPlatformsCount} subtitle="Connected coding profiles" icon={<Layers size={20} />} accentColor="var(--color-success)" />
              </div>
            </section>

            {partialStats && <p className="dashboard-data-note">A dash means a platform has not supplied that metric; it does not mean zero. Totals include only values the connected sources reported.</p>}

            {analytics && (
              <section className="dashboard-analytics-grid" aria-label="Progress breakdowns">
                <DifficultyBreakdown difficulty={analytics.difficulty} connectedPlatformCount={accounts.length || overview.connectedPlatformsCount} />
                <PlatformShare comparisons={analytics.platformComparison ?? []} />
              </section>
            )}

            {analytics?.contests && <ContestSummary contests={analytics.contests} />}

            <section className="dashboard-section" aria-labelledby="dashboard-platforms-title">
              <div className="dashboard-section-heading">
                <div><h2 id="dashboard-platforms-title">Connected platform profiles</h2><p>Each card shows what that platform has actually reported.</p></div>
                <span className="dashboard-count">{platforms.length} account{platforms.length === 1 ? '' : 's'}</span>
              </div>
              <div className="dashboard-platform-grid">{platformCards}</div>
            </section>
          </>
        )}

        {!loading && displayState === 'connected-unavailable' && platforms.length > 0 && (
          <section className="dashboard-section" aria-labelledby="dashboard-platforms-title">
            <div className="dashboard-section-heading"><div><h2 id="dashboard-platforms-title">Connected platform profiles</h2><p>These accounts are connected; metrics will appear when a source provides them.</p></div></div>
            <div className="dashboard-platform-grid">{platformCards}</div>
          </section>
        )}
      </main>
    </div>
  );
};

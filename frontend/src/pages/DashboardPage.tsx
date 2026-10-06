import React from 'react';
import { useAuth } from '../hooks/useAuth';
import { useDashboard } from '../hooks/useDashboard';
import { MetricCard } from '../components/dashboard/MetricCard';
import { DifficultyBreakdown } from '../components/dashboard/DifficultyBreakdown';
import { PlatformCard } from '../components/dashboard/PlatformCard';
import { PlatformShare } from '../components/dashboard/PlatformShare';
import { ContestSummary } from '../components/dashboard/ContestSummary';
import {
  Code2,
  LogOut,
  User as UserIcon,
  RefreshCw,
  Flame,
  Trophy,
  CheckCircle2,
  Layers,
  AlertCircle,
  PlusCircle,
} from 'lucide-react';

export const DashboardPage: React.FC = () => {
  const { user, logout } = useAuth();
  const { stats, analytics, loading, error, refetch } = useDashboard();

  const overview = stats?.overview;
  const platforms = stats?.platforms || [];
  const isEmpty = !loading && !error && (!overview || overview.connectedPlatformsCount === 0 || platforms.length === 0);

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      {/* Top Header */}
      <header
        style={{
          borderBottom: '1px solid var(--color-border-subtle)',
          backgroundColor: 'var(--color-bg-card)',
          padding: '1rem 2rem',
          position: 'sticky',
          top: 0,
          zIndex: 20,
          backdropFilter: 'blur(8px)',
        }}
      >
        <div
          style={{
            maxWidth: '1280px',
            margin: '0 auto',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            flexWrap: 'wrap',
            gap: '1rem',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <div
              style={{
                width: '34px',
                height: '34px',
                borderRadius: 'var(--radius-md)',
                backgroundColor: 'var(--color-primary)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#0b0f19',
                fontWeight: 800,
              }}
            >
              <Code2 size={22} />
            </div>
            <div>
              <span style={{ fontWeight: 800, fontSize: '1.25rem', letterSpacing: '-0.02em' }}>
                Kod<span style={{ color: 'var(--color-primary)' }}>BTW</span>
              </span>
              <span
                style={{
                  marginLeft: '0.625rem',
                  fontSize: '0.6875rem',
                  textTransform: 'uppercase',
                  letterSpacing: '0.08em',
                  color: 'var(--color-text-muted)',
                }}
              >
                Dashboard
              </span>
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <button
              onClick={() => refetch()}
              disabled={loading}
              className="btn btn-ghost"
              style={{ padding: '0.5rem 0.75rem', fontSize: '0.8125rem' }}
              title="Refresh statistics"
            >
              <RefreshCw size={15} className={loading ? 'spinner' : ''} />
              <span style={{ display: 'none', md: 'inline' } as any}>Refresh</span>
            </button>

            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '0.5rem',
                color: 'var(--color-text-secondary)',
                fontSize: '0.875rem',
                padding: '0.375rem 0.75rem',
                backgroundColor: 'var(--color-bg-base)',
                borderRadius: 'var(--radius-md)',
                border: '1px solid var(--color-border-subtle)',
              }}
            >
              <UserIcon size={16} />
              <span style={{ fontWeight: 500 }}>{user?.name}</span>
            </div>

            <button
              onClick={logout}
              className="btn btn-secondary"
              style={{ padding: '0.5rem 0.875rem', fontSize: '0.8125rem' }}
            >
              <LogOut size={15} />
              <span>Sign Out</span>
            </button>
          </div>
        </div>
      </header>

      {/* Main Container */}
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
        {/* Loading State: Skeletons */}
        {loading && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
            {/* Top metric skeletons */}
            <div
              style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
                gap: '1rem',
              }}
            >
              {[1, 2, 3, 4, 5].map((i) => (
                <div key={i} className="card skeleton" style={{ height: '110px' }} />
              ))}
            </div>

            {/* Middle analytics skeletons */}
            <div
              style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(340px, 1fr))',
                gap: '1.5rem',
              }}
            >
              <div className="card skeleton" style={{ height: '240px' }} />
              <div className="card skeleton" style={{ height: '240px' }} />
            </div>

            {/* Bottom cards skeletons */}
            <div
              style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))',
                gap: '1.5rem',
              }}
            >
              {[1, 2, 3].map((i) => (
                <div key={i} className="card skeleton" style={{ height: '260px' }} />
              ))}
            </div>
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
                Unable to Load Dashboard
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
              margin: '3rem auto',
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
                No coding platforms connected yet
              </h2>
              <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.9375rem', lineHeight: 1.5 }}>
                Connect your LeetCode, Codeforces, or other competitive profiles to sync your solved problems, contest ratings, and streaks into one unified dashboard.
              </p>
            </div>
            <button
              className="btn btn-primary"
              style={{ padding: '0.75rem 1.5rem', fontSize: '0.9375rem' }}
              onClick={() => {
                alert('Platform connection dialog will be available in Phase 7C.');
              }}
            >
              <PlusCircle size={18} /> Connect Account
            </button>
          </div>
        )}

        {/* Data Loaded Successfully */}
        {!loading && !error && !isEmpty && overview && (
          <>
            {/* Overview Metric Row */}
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                <h2 style={{ fontSize: '1.25rem', fontWeight: 700, letterSpacing: '-0.01em' }}>
                  Unified Overview
                </h2>
                <div className="badge badge-muted">
                  <CheckCircle2 size={12} color="var(--color-success)" />
                  <span>{overview.connectedPlatformsCount} Platform{overview.connectedPlatformsCount > 1 ? 's' : ''} Connected</span>
                </div>
              </div>

              <div
                style={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(auto-fit, minmax(210px, 1fr))',
                  gap: '1rem',
                }}
              >
                <MetricCard
                  title="Total Solved"
                  value={overview.totalProblemsSolved}
                  subtitle="Aggregated across all profiles"
                  icon={<CheckCircle2 size={20} />}
                  accentColor="var(--color-primary)"
                />
                <MetricCard
                  title="Contests"
                  value={overview.contestsParticipated}
                  subtitle="Competitive events attended"
                  icon={<Trophy size={20} />}
                  accentColor="#3b82f6"
                />
                <MetricCard
                  title="Current Streak"
                  value={overview.currentStreak !== null ? `${overview.currentStreak} days` : null}
                  subtitle="Active daily solving streak"
                  icon={<Flame size={20} />}
                  accentColor="#eb7312"
                />
                <MetricCard
                  title="Longest Streak"
                  value={overview.longestStreak !== null ? `${overview.longestStreak} days` : null}
                  subtitle="All-time personal record"
                  icon={<Flame size={20} />}
                  accentColor="#f59e0b"
                />
                <MetricCard
                  title="Platforms"
                  value={overview.connectedPlatformsCount}
                  subtitle="Active connected sources"
                  icon={<Layers size={20} />}
                  accentColor="#10b981"
                />
              </div>
            </div>

            {/* Analytics Section: Difficulty & Platform Share */}
            {analytics && (
              <div
                style={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(auto-fit, minmax(360px, 1fr))',
                  gap: '1.5rem',
                }}
              >
                <DifficultyBreakdown difficulty={analytics.difficulty} />
                <PlatformShare comparisons={analytics.platformComparison || []} />
              </div>
            )}

            {/* Contest Analytics Summary */}
            {analytics?.contests && (
              <div>
                <ContestSummary contests={analytics.contests} />
              </div>
            )}

            {/* Connected Platform Cards Grid */}
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                <h3 style={{ fontSize: '1.125rem', fontWeight: 600 }}>Connected Platform Profiles</h3>
                <span style={{ fontSize: '0.8125rem', color: 'var(--color-text-muted)' }}>
                  {platforms.length} account{platforms.length > 1 ? 's' : ''}
                </span>
              </div>

              <div
                style={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))',
                  gap: '1.25rem',
                }}
              >
                {platforms.map((p) => (
                  <PlatformCard key={`${p.platform}-${p.username}`} platform={p} />
                ))}
              </div>
            </div>
          </>
        )}
      </main>
    </div>
  );
};

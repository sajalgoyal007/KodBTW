import React from 'react';
import { PlatformStats } from '../../types/dashboard';
import { ExternalLink, Calendar, Flame, Trophy, RefreshCw } from 'lucide-react';
import { PlatformAccountResponse } from '../../types/platform';
import { getPlatformStatsSourceBadgeClass, getPlatformStatsSourceLabel } from '../../utils/platformStatsSource';
import { getPlatformSyncPresentation } from '../../utils/dashboardPresentation';

interface PlatformCardProps {
  platform: PlatformStats;
  account?: PlatformAccountResponse;
  refreshing: boolean;
  refreshDisabled?: boolean;
  feedback: { success: boolean; message: string } | null;
  onRefresh: (accountId: number) => void;
}

export const PlatformCard: React.FC<PlatformCardProps> = ({ platform, account, refreshing, refreshDisabled = false, feedback, onRefresh }) => {
  const formatMetric = (val: number | string | null | undefined) =>
    val !== null && val !== undefined ? val.toLocaleString() : '—';
  const isCodeforces = platform.platform.toUpperCase() === 'CODEFORCES';

  const formatDateTime = (isoString: string | null) => {
    if (!isoString) return '—';
    try {
      const d = new Date(isoString);
      return isNaN(d.getTime()) ? 'Date unavailable' : d.toLocaleString(undefined, {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        timeZoneName: 'short',
      });
    } catch {
      return 'Date unavailable';
    }
  };

  const getSourceBadge = (source: string, account?: PlatformAccountResponse) => {
    const label = getPlatformStatsSourceLabel(source, account?.lastSyncErrorCategory);
    return (
      <span
        className={`badge ${getPlatformStatsSourceBadgeClass(source)}`}
        style={{
          fontSize: '0.6875rem',
        }}
      >
        {label}
      </span>
    );
  };

  const getPlatformBrandColor = (pName: string) => {
    switch (pName.toUpperCase()) {
      case 'LEETCODE':
        return '#f89f1b';
      case 'CODEFORCES':
        return '#1f8acb';
      case 'CODECHEF':
        return '#8b4513';
      case 'GEEKSFORGEEKS':
        return '#2f8d46';
      case 'HACKERRANK':
        return '#2ec866';
      default:
        return 'var(--color-primary)';
    }
  };

  const brandColor = getPlatformBrandColor(platform.platform);
  const syncPresentation = getPlatformSyncPresentation(account);

  return (
    <div
      className="card dashboard-platform-card"
      style={{
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'space-between',
        position: 'relative',
        overflow: 'hidden',
      }}
    >
      <div style={{ position: 'absolute', top: 0, left: 0, right: 0, height: '3px', backgroundColor: brandColor }} />

      {/* Header */}
      <div>
        <div className="dashboard-platform-header">
          <div className="dashboard-platform-header-content">
            <div className="dashboard-platform-heading">
              <h4 className="dashboard-platform-name" style={{ fontSize: '1.0625rem', fontWeight: 700, letterSpacing: '-0.01em' }}>
                {platform.platform}
              </h4>
              {getSourceBadge(platform.source, account)}
            </div>

            <div className="dashboard-platform-account">
              <span className="mono dashboard-platform-username" style={{ fontSize: '0.8125rem', color: 'var(--color-text-secondary)' }}>
                @{platform.username}
              </span>
              {platform.profileUrl && (
                <a
                  href={platform.profileUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  style={{ color: 'var(--color-text-muted)', display: 'inline-flex', alignItems: 'center' }}
                  title="View Profile"
                  aria-label={`View ${platform.platform} profile (opens in a new tab)`}
                >
                  <ExternalLink size={12} />
                </a>
              )}
            </div>
          </div>
        </div>

        {/* Primary stats row */}
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(3, minmax(0, 1fr))',
            gap: '0.5rem',
            margin: '1rem 0',
            backgroundColor: 'var(--color-bg-base)',
            padding: '0.75rem',
            borderRadius: 'var(--radius-md)',
            border: '1px solid var(--color-border-subtle)',
            textAlign: 'center',
          }}
        >
          <div className="dashboard-platform-stat">
            <div className="dashboard-platform-stat-label">Solved</div>
            <div className="mono" style={{ fontSize: '1.125rem', fontWeight: 700, color: 'var(--color-text-primary)' }}>
              {formatMetric(platform.totalProblemsSolved)}
            </div>
          </div>
          <div className="dashboard-platform-stat">
            <div className="dashboard-platform-stat-label">Rating</div>
            <div className="mono" style={{ fontSize: '1.125rem', fontWeight: 700, color: 'var(--color-text-primary)' }}>
              {formatMetric(platform.rating)}
            </div>
          </div>
          <div className="dashboard-platform-stat">
            <div className="dashboard-platform-stat-label">
              {isCodeforces ? 'Max Rating' : 'Rank'}
            </div>
            <div className="mono" style={{ fontSize: '1.125rem', fontWeight: 700, color: 'var(--color-text-primary)' }}>
              {formatMetric(isCodeforces ? platform.maxRating : platform.rank)}
            </div>
          </div>
        </div>

        {/* Sub-metrics: Difficulty pills */}
        <div className="dashboard-difficulty-pills" style={{ display: 'flex', justifyContent: 'space-between', gap: '0.5rem', marginBottom: '1rem', fontSize: '0.75rem' }}>
          <div style={{ flex: '1 1 4.5rem', minWidth: 0, backgroundColor: 'var(--color-success-soft)', padding: '0.375rem 0.5rem', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-success-border)' }}>
            <span style={{ color: 'var(--color-success)', fontWeight: 600 }}>Easy: </span>
            <span className="mono">{formatMetric(platform.easySolved)}</span>
          </div>
          <div style={{ flex: '1 1 4.5rem', minWidth: 0, backgroundColor: 'var(--color-warning-soft)', padding: '0.375rem 0.5rem', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-warning-border)' }}>
            <span style={{ color: 'var(--color-warning)', fontWeight: 600 }}>Medium: </span>
            <span className="mono">{formatMetric(platform.mediumSolved)}</span>
          </div>
          <div style={{ flex: '1 1 4.5rem', minWidth: 0, backgroundColor: 'var(--color-error-soft)', padding: '0.375rem 0.5rem', borderRadius: 'var(--radius-sm)', border: '1px solid var(--color-error-border)' }}>
            <span style={{ color: 'var(--color-error)', fontWeight: 600 }}>Hard: </span>
            <span className="mono">{formatMetric(platform.hardSolved)}</span>
          </div>
        </div>

        {/* Contests & Streaks */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', fontSize: '0.8125rem', color: 'var(--color-text-secondary)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span style={{ display: 'flex', alignItems: 'center', gap: '0.375rem' }}>
              <Trophy size={14} style={{ color: 'var(--color-text-muted)' }} />
              Contests
            </span>
            <span className="mono" style={{ color: 'var(--color-text-primary)' }}>
              {formatMetric(platform.contestsParticipated)}
            </span>
          </div>

          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span style={{ display: 'flex', alignItems: 'center', gap: '0.375rem' }}>
              <Flame size={14} style={{ color: 'var(--color-primary)' }} />
              Current Streak
            </span>
            <span className="mono" style={{ color: 'var(--color-text-primary)' }}>
              {platform.currentStreak !== null && platform.currentStreak !== undefined ? `${platform.currentStreak}d` : '—'}
            </span>
          </div>

          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span style={{ display: 'flex', alignItems: 'center', gap: '0.375rem' }}>
              <Flame size={14} style={{ color: 'var(--color-text-muted)' }} />
              Longest Streak
            </span>
            <span className="mono" style={{ color: 'var(--color-text-primary)' }}>
              {platform.longestStreak !== null && platform.longestStreak !== undefined ? `${platform.longestStreak}d` : '—'}
            </span>
          </div>
        </div>
      </div>

      {/* Footer / Last Synced */}
      <div className="dashboard-platform-footer">
        <div className="dashboard-platform-sync">
          <span className={`dashboard-sync-label dashboard-sync-label--${syncPresentation.tone}`}>
            <Calendar size={12} aria-hidden="true" /> {syncPresentation.label}
          </span>
          <span className="dashboard-sync-detail">{syncPresentation.detail}</span>
          {account?.lastSuccessAt && <span className="dashboard-sync-date">Last successful: {formatDateTime(account.lastSuccessAt)}</span>}
          {account?.lastSyncErrorMessage && <span role="status" className="dashboard-sync-error">{account.lastSyncErrorMessage}</span>}
          {feedback && <span role={feedback.success ? 'status' : 'alert'} className={feedback.success ? 'dashboard-sync-feedback success' : 'dashboard-sync-feedback error'}>{feedback.message}</span>}
        </div>
        <button
          type="button"
          disabled={!account || refreshing || refreshDisabled}
          onClick={() => account && onRefresh(account.id)}
          className="btn btn-secondary dashboard-refresh-button"
          aria-label={`Refresh ${platform.platform} statistics`}
        >
          <RefreshCw size={13} className={refreshing ? 'spin' : ''} /> {refreshing ? 'Refreshing…' : 'Refresh Stats'}
        </button>
      </div>
    </div>
  );
};

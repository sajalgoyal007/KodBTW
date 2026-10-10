import React from 'react';
import { PlatformStats } from '../../types/dashboard';
import { ExternalLink, Calendar, Flame, Trophy, RefreshCw } from 'lucide-react';
import { PlatformAccountResponse } from '../../types/platform';
import { getPlatformStatsSourceLabel, isVerifiedStatsSource } from '../../utils/platformStatsSource';

interface PlatformCardProps {
  platform: PlatformStats;
  account?: PlatformAccountResponse;
  refreshing: boolean;
  feedback: { success: boolean; message: string } | null;
  onRefresh: (accountId: number) => void;
}

export const PlatformCard: React.FC<PlatformCardProps> = ({ platform, account, refreshing, feedback, onRefresh }) => {
  const formatMetric = (val: number | string | null | undefined) =>
    val !== null && val !== undefined ? val.toLocaleString() : '—';
  const isCodeforces = platform.platform.toUpperCase() === 'CODEFORCES';

  const formatDateTime = (isoString: string | null) => {
    if (!isoString) return '—';
    try {
      const d = new Date(isoString);
      return isNaN(d.getTime()) ? isoString : d.toLocaleDateString(undefined, {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return isoString;
    }
  };

  const getSourceBadge = (source: string, account?: PlatformAccountResponse) => {
    const label = getPlatformStatsSourceLabel(source, account?.lastSyncErrorCategory);
    const verified = isVerifiedStatsSource(source);
    const thirdParty = source.toUpperCase() === 'CODECHEF_THIRD_PARTY';
    const mock = source.toUpperCase() === 'MOCK';
    return (
      <span
        className={`badge${verified || thirdParty || mock ? '' : ' badge-muted'}`}
        style={{
          backgroundColor: verified ? 'rgba(16, 185, 129, 0.12)' : thirdParty || mock ? 'rgba(235, 115, 18, 0.12)' : undefined,
          color: verified ? 'var(--color-success)' : thirdParty || mock ? 'var(--color-primary)' : undefined,
          borderColor: verified ? 'rgba(16, 185, 129, 0.3)' : thirdParty || mock ? 'rgba(235, 115, 18, 0.3)' : undefined,
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

  return (
    <div
      className="card"
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
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.75rem', gap: '0.5rem' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <h4 style={{ fontSize: '1.0625rem', fontWeight: 700, letterSpacing: '-0.01em' }}>
                {platform.platform}
              </h4>
              {getSourceBadge(platform.source, account)}
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '0.375rem', marginTop: '0.25rem' }}>
              <span className="mono" style={{ fontSize: '0.8125rem', color: 'var(--color-text-secondary)' }}>
                @{platform.username}
              </span>
              {platform.profileUrl && (
                <a
                  href={platform.profileUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  style={{ color: 'var(--color-text-muted)', display: 'inline-flex', alignItems: 'center' }}
                  title="View Profile"
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
            gridTemplateColumns: 'repeat(3, 1fr)',
            gap: '0.5rem',
            margin: '1rem 0',
            backgroundColor: 'var(--color-bg-base)',
            padding: '0.75rem',
            borderRadius: 'var(--radius-md)',
            border: '1px solid var(--color-border-subtle)',
            textAlign: 'center',
          }}
        >
          <div>
            <div style={{ fontSize: '0.6875rem', color: 'var(--color-text-muted)', textTransform: 'uppercase' }}>Solved</div>
            <div className="mono" style={{ fontSize: '1.125rem', fontWeight: 700, color: 'var(--color-text-primary)' }}>
              {formatMetric(platform.totalProblemsSolved)}
            </div>
          </div>
          <div>
            <div style={{ fontSize: '0.6875rem', color: 'var(--color-text-muted)', textTransform: 'uppercase' }}>Rating</div>
            <div className="mono" style={{ fontSize: '1.125rem', fontWeight: 700, color: 'var(--color-text-primary)' }}>
              {formatMetric(platform.rating)}
            </div>
          </div>
          <div>
            <div style={{ fontSize: '0.6875rem', color: 'var(--color-text-muted)', textTransform: 'uppercase' }}>
              {isCodeforces ? 'Max Rating' : 'Rank'}
            </div>
            <div className="mono" style={{ fontSize: '1.125rem', fontWeight: 700, color: 'var(--color-text-primary)' }}>
              {formatMetric(isCodeforces ? platform.maxRating : platform.rank)}
            </div>
          </div>
        </div>

        {/* Sub-metrics: Difficulty pills */}
        <div style={{ display: 'flex', justifyContent: 'space-between', gap: '0.5rem', marginBottom: '1rem', fontSize: '0.75rem' }}>
          <div style={{ flex: 1, backgroundColor: 'rgba(16, 185, 129, 0.08)', padding: '0.375rem 0.5rem', borderRadius: 'var(--radius-sm)', border: '1px solid rgba(16, 185, 129, 0.2)' }}>
            <span style={{ color: 'var(--color-success)', fontWeight: 600 }}>E: </span>
            <span className="mono">{formatMetric(platform.easySolved)}</span>
          </div>
          <div style={{ flex: 1, backgroundColor: 'rgba(245, 158, 11, 0.08)', padding: '0.375rem 0.5rem', borderRadius: 'var(--radius-sm)', border: '1px solid rgba(245, 158, 11, 0.2)' }}>
            <span style={{ color: 'var(--color-warning)', fontWeight: 600 }}>M: </span>
            <span className="mono">{formatMetric(platform.mediumSolved)}</span>
          </div>
          <div style={{ flex: 1, backgroundColor: 'rgba(239, 68, 68, 0.08)', padding: '0.375rem 0.5rem', borderRadius: 'var(--radius-sm)', border: '1px solid rgba(239, 68, 68, 0.2)' }}>
            <span style={{ color: 'var(--color-error)', fontWeight: 600 }}>H: </span>
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
      <div
        style={{
          marginTop: '1.25rem',
          paddingTop: '0.75rem',
          borderTop: '1px solid var(--color-border-subtle)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          flexWrap: 'wrap',
          gap: '0.5rem',
          fontSize: '0.6875rem',
          color: 'var(--color-text-muted)',
        }}
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem', flex: '1 1 160px', minWidth: 0 }}>
          <span style={{ display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
            <Calendar size={12} /> {account?.syncStatus?.replace('_', ' ') || 'Sync status'}
          </span>
          <span className="mono">{account?.fresh ? 'Fresh · ' : 'Last successful: '}{formatDateTime(account?.lastSuccessAt ?? null)}</span>
          {account?.lastSyncErrorMessage && <span role="status" style={{ color: 'var(--color-error)', maxWidth: '220px' }}>{account.lastSyncErrorMessage}</span>}
          {feedback && <span role="status" style={{ color: feedback.success ? 'var(--color-success)' : 'var(--color-error)', maxWidth: '220px' }}>{feedback.message}</span>}
        </div>
        <button
          type="button"
          className="btn btn-secondary"
          disabled={!account || refreshing}
          onClick={() => account && onRefresh(account.id)}
          style={{ padding: '0.4rem 0.6rem', fontSize: '0.75rem', whiteSpace: 'nowrap', flexShrink: 0 }}
        >
          <RefreshCw size={13} className={refreshing ? 'spin' : ''} /> {refreshing ? 'Refreshing…' : 'Refresh Stats'}
        </button>
      </div>
    </div>
  );
};

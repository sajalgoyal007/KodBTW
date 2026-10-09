import React, { useState, useEffect } from 'react';
import { PlatformStats } from '../../types/dashboard';
import { getStats } from '../../services/api/platformAccountApi';
import { X, Calendar, Flame, Trophy, ExternalLink } from 'lucide-react';

interface PlatformStatsModalProps {
  isOpen: boolean;
  accountId: number | null;
  platformName: string;
  username: string;
  onClose: () => void;
}

export const PlatformStatsModal: React.FC<PlatformStatsModalProps> = ({
  isOpen,
  accountId,
  platformName,
  username,
  onClose,
}) => {
  const [stats, setStats] = useState<PlatformStats | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (isOpen && accountId !== null) {
      setLoading(true);
      setError(null);
      getStats(accountId)
        .then((data) => setStats(data))
        .catch((err) => setError(err?.message || 'Failed to fetch platform statistics.'))
        .finally(() => setLoading(false));
    } else {
      setStats(null);
    }
  }, [isOpen, accountId]);

  if (!isOpen) return null;

  const formatMetric = (val: number | string | null | undefined) =>
    val !== null && val !== undefined ? val.toLocaleString() : '—';

  const formatDateTime = (isoString: string | null | undefined) => {
    if (!isoString) return '—';
    try {
      const d = new Date(isoString);
      return isNaN(d.getTime()) ? isoString : d.toLocaleDateString(undefined, {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return isoString;
    }
  };

  const getSourceBadge = (source: string | undefined) => {
    if (source === 'SOURCE_PENDING') {
      return <span className="badge badge-muted" style={{ fontSize: '0.6875rem' }}>Statistics unavailable</span>;
    }
    if (source === 'LEETCODE_REAL' || source === 'CODEFORCES_REAL') {
      return (
        <span
          className="badge"
          style={{
            backgroundColor: 'rgba(16, 185, 129, 0.12)',
            color: 'var(--color-success)',
            borderColor: 'rgba(16, 185, 129, 0.3)',
            fontSize: '0.6875rem',
          }}
        >
          Live Public Data
        </span>
      );
    }
    return (
      <span
        className="badge"
        style={{
          backgroundColor: 'rgba(235, 115, 18, 0.12)',
          color: 'var(--color-primary)',
          borderColor: 'rgba(235, 115, 18, 0.3)',
          fontSize: '0.6875rem',
        }}
      >
        Mock Sandbox
      </span>
    );
  };

  return (
    <div
      style={{
        position: 'fixed',
        inset: 0,
        backgroundColor: 'rgba(0, 0, 0, 0.7)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        zIndex: 50,
        padding: '1rem',
        backdropFilter: 'blur(4px)',
      }}
      onClick={onClose}
    >
      <div
        className="card"
        style={{
          width: '100%',
          maxWidth: '520px',
          padding: '2rem',
          backgroundColor: 'var(--color-bg-card)',
          borderRadius: 'var(--radius-lg)',
          border: '1px solid var(--color-border-subtle)',
          boxShadow: 'var(--shadow-card)',
        }}
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1.5rem' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <h3 style={{ fontSize: '1.25rem', fontWeight: 700 }}>{platformName} Stats</h3>
              {stats && getSourceBadge(stats.source)}
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.375rem', marginTop: '0.25rem' }}>
              <span className="mono" style={{ fontSize: '0.875rem', color: 'var(--color-text-secondary)' }}>
                @{username}
              </span>
              {stats?.profileUrl && (
                <a
                  href={stats.profileUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  style={{ color: 'var(--color-text-muted)', display: 'inline-flex', alignItems: 'center' }}
                >
                  <ExternalLink size={12} />
                </a>
              )}
            </div>
          </div>
          <button
            onClick={onClose}
            className="btn btn-ghost"
            style={{ padding: '0.25rem', color: 'var(--color-text-muted)' }}
          >
            <X size={18} />
          </button>
        </div>

        {/* Content */}
        {loading && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', padding: '1rem 0' }}>
            <div className="card skeleton" style={{ height: '80px' }} />
            <div className="card skeleton" style={{ height: '60px' }} />
            <div className="card skeleton" style={{ height: '80px' }} />
          </div>
        )}

        {!loading && error && (
          <div className="alert alert-error" style={{ marginBottom: '1rem' }}>
            <span>{error}</span>
          </div>
        )}

        {!loading && !error && stats && (stats.source === 'SOURCE_PENDING' || !stats.source?.toUpperCase().includes('REAL') ? (
          <div style={{ padding: '1.25rem', borderRadius: 'var(--radius-md)', border: '1px solid var(--color-border-subtle)', backgroundColor: 'var(--color-bg-base)', color: 'var(--color-text-secondary)', textAlign: 'center' }}>
            {stats.source === 'SOURCE_PENDING' ? 'Connected — live statistics currently unavailable.' : 'No verified live statistics are available for this platform.'}
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
            {/* Top metrics grid */}
            <div
              style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(3, 1fr)',
                gap: '0.75rem',
                backgroundColor: 'var(--color-bg-base)',
                padding: '1rem',
                borderRadius: 'var(--radius-md)',
                border: '1px solid var(--color-border-subtle)',
                textAlign: 'center',
              }}
            >
              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', textTransform: 'uppercase' }}>Solved</div>
                <div className="mono" style={{ fontSize: '1.375rem', fontWeight: 700, color: 'var(--color-text-primary)' }}>
                  {formatMetric(stats.totalProblemsSolved)}
                </div>
              </div>
              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', textTransform: 'uppercase' }}>Rating</div>
                <div className="mono" style={{ fontSize: '1.375rem', fontWeight: 700, color: 'var(--color-text-primary)' }}>
                  {formatMetric(stats.rating)}
                </div>
              </div>
              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', textTransform: 'uppercase' }}>
                  {stats.platform?.toUpperCase() === 'CODEFORCES' ? 'Max Rating' : 'Rank'}
                </div>
                <div className="mono" style={{ fontSize: '1.375rem', fontWeight: 700, color: 'var(--color-text-primary)' }}>
                  {formatMetric(stats.platform?.toUpperCase() === 'CODEFORCES' ? stats.maxRating : stats.rank)}
                </div>
              </div>
            </div>

            {/* Difficulty Breakdown */}
            <div style={{ display: 'flex', justifyContent: 'space-between', gap: '0.75rem' }}>
              <div
                style={{
                  flex: 1,
                  backgroundColor: 'rgba(16, 185, 129, 0.08)',
                  padding: '0.625rem 0.75rem',
                  borderRadius: 'var(--radius-md)',
                  border: '1px solid rgba(16, 185, 129, 0.2)',
                  textAlign: 'center',
                }}
              >
                <div style={{ color: 'var(--color-success)', fontSize: '0.75rem', fontWeight: 600 }}>Easy</div>
                <div className="mono" style={{ fontSize: '1.125rem', fontWeight: 700 }}>{formatMetric(stats.easySolved)}</div>
              </div>
              <div
                style={{
                  flex: 1,
                  backgroundColor: 'rgba(245, 158, 11, 0.08)',
                  padding: '0.625rem 0.75rem',
                  borderRadius: 'var(--radius-md)',
                  border: '1px solid rgba(245, 158, 11, 0.2)',
                  textAlign: 'center',
                }}
              >
                <div style={{ color: 'var(--color-warning)', fontSize: '0.75rem', fontWeight: 600 }}>Medium</div>
                <div className="mono" style={{ fontSize: '1.125rem', fontWeight: 700 }}>{formatMetric(stats.mediumSolved)}</div>
              </div>
              <div
                style={{
                  flex: 1,
                  backgroundColor: 'rgba(239, 68, 68, 0.08)',
                  padding: '0.625rem 0.75rem',
                  borderRadius: 'var(--radius-md)',
                  border: '1px solid rgba(239, 68, 68, 0.2)',
                  textAlign: 'center',
                }}
              >
                <div style={{ color: 'var(--color-error)', fontSize: '0.75rem', fontWeight: 600 }}>Hard</div>
                <div className="mono" style={{ fontSize: '1.125rem', fontWeight: 700 }}>{formatMetric(stats.hardSolved)}</div>
              </div>
            </div>

            {/* Contests & Streaks list */}
            <div
              style={{
                display: 'flex',
                flexDirection: 'column',
                gap: '0.625rem',
                fontSize: '0.875rem',
                backgroundColor: 'var(--color-bg-base)',
                padding: '0.875rem 1rem',
                borderRadius: 'var(--radius-md)',
                border: '1px solid var(--color-border-subtle)',
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--color-text-secondary)' }}>
                  <Trophy size={15} style={{ color: 'var(--color-text-muted)' }} /> Contests Participated
                </span>
                <span className="mono" style={{ fontWeight: 600 }}>{formatMetric(stats.contestsParticipated)}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--color-text-secondary)' }}>
                  <Flame size={15} style={{ color: 'var(--color-primary)' }} /> Current Streak
                </span>
                <span className="mono" style={{ fontWeight: 600 }}>
                  {stats.currentStreak !== null && stats.currentStreak !== undefined ? `${stats.currentStreak} days` : '—'}
                </span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--color-text-secondary)' }}>
                  <Flame size={15} style={{ color: 'var(--color-text-muted)' }} /> Longest Streak
                </span>
                <span className="mono" style={{ fontWeight: 600 }}>
                  {stats.longestStreak !== null && stats.longestStreak !== undefined ? `${stats.longestStreak} days` : '—'}
                </span>
              </div>
            </div>

            {/* Footer / Last Synced */}
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                fontSize: '0.75rem',
                color: 'var(--color-text-muted)',
                paddingTop: '0.5rem',
                borderTop: '1px solid var(--color-border-subtle)',
              }}
            >
              <span style={{ display: 'flex', alignItems: 'center', gap: '0.375rem' }}>
                <Calendar size={13} /> Last Synced
              </span>
              <span className="mono">{formatDateTime(stats.lastSyncedAt)}</span>
            </div>
          </div>
        ))}

        <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '1.5rem' }}>
          <button type="button" className="btn btn-secondary" onClick={onClose}>
            Close
          </button>
        </div>
      </div>
    </div>
  );
};

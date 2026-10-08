import React from 'react';
import { Link } from 'react-router-dom';
import { LeaderboardEntry } from '../../types/leaderboard';
import {
  Trophy,
  CheckCircle2,
  AlertTriangle,
  GraduationCap,
  ChevronLeft,
  ChevronRight,
} from 'lucide-react';

interface LeaderboardTableProps {
  entries: LeaderboardEntry[];
  total: number;
  page: number;
  size: number;
  loading: boolean;
  onPageChange: (newPage: number) => void;
}

export const LeaderboardTable: React.FC<LeaderboardTableProps> = ({
  entries,
  total,
  page,
  size,
  loading,
  onPageChange,
}) => {
  const totalPages = Math.ceil(total / size);

  const renderRankBadge = (rank: number) => {
    if (rank === 1) {
      return (
        <span
          style={{
            display: 'inline-flex',
            alignItems: 'center',
            justifyContent: 'center',
            width: '28px',
            height: '28px',
            borderRadius: '50%',
            backgroundColor: 'rgba(234, 179, 8, 0.2)',
            color: 'var(--color-warning)',
            fontWeight: 800,
            fontSize: '0.875rem',
            border: '1px solid rgba(234, 179, 8, 0.4)',
          }}
          title="1st Place"
        >
          1
        </span>
      );
    }
    if (rank === 2) {
      return (
        <span
          style={{
            display: 'inline-flex',
            alignItems: 'center',
            justifyContent: 'center',
            width: '28px',
            height: '28px',
            borderRadius: '50%',
            backgroundColor: 'rgba(148, 163, 184, 0.2)',
            color: '#94a3b8',
            fontWeight: 800,
            fontSize: '0.875rem',
            border: '1px solid rgba(148, 163, 184, 0.4)',
          }}
          title="2nd Place"
        >
          2
        </span>
      );
    }
    if (rank === 3) {
      return (
        <span
          style={{
            display: 'inline-flex',
            alignItems: 'center',
            justifyContent: 'center',
            width: '28px',
            height: '28px',
            borderRadius: '50%',
            backgroundColor: 'rgba(180, 83, 9, 0.2)',
            color: '#b45309',
            fontWeight: 800,
            fontSize: '0.875rem',
            border: '1px solid rgba(180, 83, 9, 0.4)',
          }}
          title="3rd Place"
        >
          3
        </span>
      );
    }
    return (
      <span
        style={{
          color: 'var(--color-text-muted)',
          fontWeight: 600,
          fontSize: '0.875rem',
          paddingLeft: '0.25rem',
        }}
      >
        #{rank}
      </span>
    );
  };

  return (
    <div
      className="card"
      style={{
        padding: '0',
        overflow: 'hidden',
        border: '1px solid var(--color-border-subtle)',
      }}
    >
      <div style={{ overflowX: 'auto' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
          <thead>
            <tr
              style={{
                borderBottom: '1px solid var(--color-border-subtle)',
                backgroundColor: 'var(--color-bg-subtle)',
                fontSize: '0.8125rem',
                color: 'var(--color-text-secondary)',
                fontWeight: 600,
                textTransform: 'uppercase',
                letterSpacing: '0.05em',
              }}
            >
              <th style={{ padding: '0.875rem 1rem', width: '70px', textAlign: 'center' }}>Rank</th>
              <th style={{ padding: '0.875rem 1.25rem' }}>Developer</th>
              <th style={{ padding: '0.875rem 1rem' }}>College</th>
              <th style={{ padding: '0.875rem 1rem', textAlign: 'right' }}>Solved</th>
              <th style={{ padding: '0.875rem 1rem', textAlign: 'right' }}>Score (WDS)</th>
              <th style={{ padding: '0.875rem 1rem', textAlign: 'right' }}>Rating · Platform</th>
              <th style={{ padding: '0.875rem 1rem', textAlign: 'right' }}>Contests</th>
              <th style={{ padding: '0.875rem 1.25rem', textAlign: 'center', width: '130px' }}>Status</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              // 10 loading skeleton rows
              Array.from({ length: 10 }).map((_, idx) => (
                <tr key={idx} style={{ borderBottom: '1px solid var(--color-border-subtle)' }}>
                  <td style={{ padding: '1rem', textAlign: 'center' }}>
                    <div className="skeleton" style={{ height: '24px', width: '28px', margin: '0 auto', borderRadius: '50%' }} />
                  </td>
                  <td style={{ padding: '1rem 1.25rem' }}>
                    <div className="skeleton" style={{ height: '18px', width: '140px' }} />
                  </td>
                  <td style={{ padding: '1rem' }}>
                    <div className="skeleton" style={{ height: '16px', width: '110px' }} />
                  </td>
                  <td style={{ padding: '1rem', textAlign: 'right' }}>
                    <div className="skeleton" style={{ height: '18px', width: '50px', marginLeft: 'auto' }} />
                  </td>
                  <td style={{ padding: '1rem', textAlign: 'right' }}>
                    <div className="skeleton" style={{ height: '18px', width: '60px', marginLeft: 'auto' }} />
                  </td>
                  <td style={{ padding: '1rem', textAlign: 'right' }}>
                    <div className="skeleton" style={{ height: '18px', width: '55px', marginLeft: 'auto' }} />
                  </td>
                  <td style={{ padding: '1rem', textAlign: 'right' }}>
                    <div className="skeleton" style={{ height: '18px', width: '40px', marginLeft: 'auto' }} />
                  </td>
                  <td style={{ padding: '1rem 1.25rem', textAlign: 'center' }}>
                    <div className="skeleton" style={{ height: '22px', width: '80px', margin: '0 auto', borderRadius: 'var(--radius-sm)' }} />
                  </td>
                </tr>
              ))
            ) : entries.length === 0 ? (
              // Empty state
              <tr>
                <td colSpan={8} style={{ padding: '4rem 2rem', textAlign: 'center' }}>
                  <div
                    style={{
                      display: 'flex',
                      flexDirection: 'column',
                      alignItems: 'center',
                      gap: '1rem',
                      color: 'var(--color-text-muted)',
                    }}
                  >
                    <Trophy size={48} style={{ strokeWidth: 1.5, opacity: 0.5 }} />
                    <div style={{ maxWidth: '400px' }}>
                      <p style={{ fontWeight: 600, fontSize: '1rem', color: 'var(--color-text-primary)', margin: '0 0 0.5rem 0' }}>
                        No Leaderboard Entries Found
                      </p>
                      <p style={{ fontSize: '0.875rem', margin: 0 }}>
                        No profiles matched your filter, or users have not synced their platforms yet. The leaderboard updates nightly at 2:00 AM.
                      </p>
                    </div>
                  </div>
                </td>
              </tr>
            ) : (
              // Rendered rows
              entries.map((entry) => {
                const isCurrent = entry.isCurrentUser;
                return (
                  <tr
                    key={entry.userId}
                    style={{
                      borderBottom: '1px solid var(--color-border-subtle)',
                      backgroundColor: isCurrent ? 'rgba(249, 115, 22, 0.08)' : 'transparent',
                      outline: isCurrent ? '1px solid rgba(249, 115, 22, 0.4)' : 'none',
                      outlineOffset: '-1px',
                      transition: 'background-color 0.15s ease',
                    }}
                  >
                    {/* Rank */}
                    <td style={{ padding: '0.875rem 1rem', textAlign: 'center' }}>
                      {renderRankBadge(entry.rank)}
                    </td>

                    {/* Developer Name + Current User Badge */}
                    <td style={{ padding: '0.875rem 1.25rem' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                        {entry.username ? (
                          <Link
                            to={`/u/${encodeURIComponent(entry.username)}`}
                            style={{
                              fontWeight: isCurrent ? 700 : 500,
                              color: isCurrent ? 'var(--color-primary)' : 'var(--color-text-primary)',
                              fontSize: '0.9375rem',
                              textDecoration: 'none',
                            }}
                          >
                            {entry.displayName || `User #${entry.userId}`}
                          </Link>
                        ) : (
                          <span style={{ fontWeight: isCurrent ? 700 : 500, color: isCurrent ? 'var(--color-primary)' : 'var(--color-text-primary)', fontSize: '0.9375rem' }}>
                            {entry.displayName || `User #${entry.userId}`}
                          </span>
                        )}
                        {isCurrent && (
                          <span
                            className="badge badge-primary"
                            style={{ fontSize: '0.6875rem', padding: '0.15rem 0.4rem' }}
                          >
                            You
                          </span>
                        )}
                      </div>
                    </td>

                    {/* College */}
                    <td style={{ padding: '0.875rem 1rem' }}>
                      {entry.college ? (
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.375rem', fontSize: '0.8125rem', color: 'var(--color-text-secondary)' }}>
                          <GraduationCap size={13} style={{ flexShrink: 0 }} />
                          <span style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', maxWidth: '200px' }}>
                            {entry.college}
                          </span>
                        </div>
                      ) : (
                        <span style={{ color: 'var(--color-text-muted)', fontSize: '0.8125rem' }}>—</span>
                      )}
                    </td>

                    {/* Total platform solves */}
                    <td style={{ padding: '0.875rem 1rem', textAlign: 'right', fontWeight: 600 }}>
                      {entry.totalSolved.toLocaleString()}
                    </td>

                    {/* Weighted Score (WDS) */}
                    <td
                      style={{
                        padding: '0.875rem 1rem',
                        textAlign: 'right',
                        fontWeight: 700,
                        color: 'var(--color-primary)',
                      }}
                    >
                      {Math.round(entry.weightedScore).toLocaleString()}
                    </td>

                    {/* Highest reported platform rating; rating scales vary. */}
                    <td style={{ padding: '0.875rem 1rem', textAlign: 'right' }}>
                      {entry.bestRating != null ? (
                        <div style={{ display: 'inline-flex', flexDirection: 'column', alignItems: 'flex-end' }}>
                          <span style={{ fontWeight: 600 }}>{entry.bestRating}</span>
                          {entry.bestRatingPlatform && (
                            <span style={{ fontSize: '0.6875rem', color: 'var(--color-text-muted)' }}>
                              {entry.bestRatingPlatform}
                            </span>
                          )}
                        </div>
                      ) : (
                        <span style={{ color: 'var(--color-text-muted)', fontSize: '0.8125rem' }}>—</span>
                      )}
                    </td>

                    {/* Contests */}
                    <td style={{ padding: '0.875rem 1rem', textAlign: 'right', color: 'var(--color-text-secondary)' }}>
                      {entry.totalContests > 0 ? entry.totalContests : '—'}
                    </td>

                    {/* Verification Status Badge */}
                    <td style={{ padding: '0.875rem 1.25rem', textAlign: 'center' }}>
                      {entry.realDataOnly ? (
                        <span
                          className="badge badge-primary"
                          style={{
                            fontSize: '0.6875rem',
                            padding: '0.2rem 0.5rem',
                            display: 'inline-flex',
                            gap: '0.25rem',
                          }}
                          title="Ranked with live platform statistics"
                        >
                          <CheckCircle2 size={11} />
                          <span>Live stats</span>
                        </span>
                      ) : (
                        <span
                          className="badge"
                          style={{
                            fontSize: '0.6875rem',
                            padding: '0.2rem 0.5rem',
                            display: 'inline-flex',
                            gap: '0.25rem',
                            backgroundColor: 'rgba(234, 179, 8, 0.15)',
                            color: 'var(--color-warning)',
                            border: '1px solid rgba(234, 179, 8, 0.3)',
                          }}
                          title="This entry is excluded from the live statistics leaderboard"
                        >
                          <AlertTriangle size={11} />
                          <span>Unverified</span>
                        </span>
                      )}
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination Controls */}
      {!loading && total > 0 && (
        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            padding: '0.875rem 1.25rem',
            borderTop: '1px solid var(--color-border-subtle)',
            backgroundColor: 'var(--color-bg-subtle)',
            fontSize: '0.8125rem',
            color: 'var(--color-text-secondary)',
            flexWrap: 'wrap',
            gap: '0.75rem',
          }}
        >
          <div>
            Showing <strong style={{ color: 'var(--color-text-primary)' }}>{entries.length > 0 ? page * size + 1 : 0}</strong> to{' '}
            <strong style={{ color: 'var(--color-text-primary)' }}>{Math.min((page + 1) * size, total)}</strong> of{' '}
            <strong style={{ color: 'var(--color-text-primary)' }}>{total}</strong> developers
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => onPageChange(page - 1)}
              disabled={page === 0}
              style={{
                padding: '0.35rem 0.75rem',
                fontSize: '0.75rem',
                display: 'inline-flex',
                alignItems: 'center',
                gap: '0.25rem',
                cursor: page === 0 ? 'not-allowed' : 'pointer',
                opacity: page === 0 ? 0.5 : 1,
              }}
            >
              <ChevronLeft size={14} />
              <span>Previous</span>
            </button>

            <span style={{ padding: '0 0.5rem', fontWeight: 600 }}>
              Page {page + 1} of {Math.max(1, totalPages)}
            </span>

            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => onPageChange(page + 1)}
              disabled={page >= totalPages - 1}
              style={{
                padding: '0.35rem 0.75rem',
                fontSize: '0.75rem',
                display: 'inline-flex',
                alignItems: 'center',
                gap: '0.25rem',
                cursor: page >= totalPages - 1 ? 'not-allowed' : 'pointer',
                opacity: page >= totalPages - 1 ? 0.5 : 1,
              }}
            >
              <span>Next</span>
              <ChevronRight size={14} />
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

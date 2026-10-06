import React from 'react';
import { PlatformStats } from '../../types/dashboard';

interface ContestPlatformTableProps {
  platforms: PlatformStats[];
}

export const ContestPlatformTable: React.FC<ContestPlatformTableProps> = ({ platforms }) => {
  const formatMetric = (val: number | string | null | undefined) =>
    val !== null && val !== undefined ? val.toLocaleString() : '—';

  const getSourceBadge = (source: string) => {
    if (source === 'LEETCODE_REAL' || source === 'CODEFORCES_REAL') {
      return (
        <span
          className="badge"
          style={{
            backgroundColor: 'rgba(16, 185, 129, 0.12)',
            color: '#10b981',
            borderColor: 'rgba(16, 185, 129, 0.3)',
            fontSize: '0.6875rem',
          }}
        >
          Verified Public API
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

  return (
    <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
      <div>
        <h3 style={{ fontSize: '1.125rem', fontWeight: 600 }}>Platform Contest Standing</h3>
        <p style={{ fontSize: '0.8125rem', color: 'var(--color-text-secondary)', marginTop: '0.125rem' }}>
          Official contest metrics, active ratings, and competitive rankings across accounts
        </p>
      </div>

      {platforms.length === 0 ? (
        <div style={{ color: 'var(--color-text-muted)', fontSize: '0.875rem', textAlign: 'center', padding: '1.5rem' }}>
          No platform accounts connected
        </div>
      ) : (
        <div style={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.875rem' }}>
            <thead>
              <tr
                style={{
                  borderBottom: '1px solid var(--color-border-subtle)',
                  textAlign: 'left',
                  color: 'var(--color-text-muted)',
                  fontSize: '0.75rem',
                  textTransform: 'uppercase',
                  letterSpacing: '0.05em',
                }}
              >
                <th style={{ padding: '0.75rem 1rem' }}>Platform</th>
                <th style={{ padding: '0.75rem 1rem' }}>Handle</th>
                <th style={{ padding: '0.75rem 1rem', textAlign: 'center' }}>Contests Participated</th>
                <th style={{ padding: '0.75rem 1rem', textAlign: 'center' }}>Rating</th>
                <th style={{ padding: '0.75rem 1rem', textAlign: 'center' }}>Rank</th>
                <th style={{ padding: '0.75rem 1rem', textAlign: 'right' }}>Source</th>
              </tr>
            </thead>
            <tbody>
              {platforms.map((row) => {
                const brandColor = getPlatformBrandColor(row.platform);
                return (
                  <tr
                    key={row.platform}
                    style={{
                      borderBottom: '1px solid var(--color-border-subtle)',
                      transition: 'background-color 0.15s ease',
                    }}
                  >
                    <td style={{ padding: '0.875rem 1rem', fontWeight: 600 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                        <span style={{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: brandColor }} />
                        <span>{row.platform}</span>
                      </div>
                    </td>
                    <td className="mono" style={{ padding: '0.875rem 1rem', color: 'var(--color-text-secondary)' }}>
                      @{row.username}
                    </td>
                    <td className="mono" style={{ padding: '0.875rem 1rem', textAlign: 'center' }}>
                      {/* Codeforces does not provide contestsParticipated in user.info, properly outputs "—" */}
                      {formatMetric(row.contestsParticipated)}
                    </td>
                    <td
                      className="mono"
                      style={{
                        padding: '0.875rem 1rem',
                        textAlign: 'center',
                        color: row.rating ? 'var(--color-primary)' : undefined,
                        fontWeight: row.rating ? 700 : undefined,
                      }}
                    >
                      {formatMetric(row.rating)}
                    </td>
                    <td className="mono" style={{ padding: '0.875rem 1rem', textAlign: 'center', color: 'var(--color-text-secondary)' }}>
                      {formatMetric(row.rank)}
                    </td>
                    <td style={{ padding: '0.875rem 1rem', textAlign: 'right' }}>
                      {getSourceBadge(row.source)}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

import React from 'react';
import { PlatformDifficultyBreakdown } from '../../types/dashboard';

interface PlatformDifficultyTableProps {
  platformBreakdown: PlatformDifficultyBreakdown[];
}

export const PlatformDifficultyTable: React.FC<PlatformDifficultyTableProps> = ({ platformBreakdown }) => {
  const formatMetric = (val: number | null | undefined) =>
    val !== null && val !== undefined ? val.toLocaleString() : '—';

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
        <h3 style={{ fontSize: '1.125rem', fontWeight: 600 }}>Platform Difficulty Breakdown</h3>
        <p style={{ fontSize: '0.8125rem', color: 'var(--color-text-secondary)', marginTop: '0.125rem' }}>
          Detailed problem distribution across each connected platform
        </p>
      </div>

      {platformBreakdown.length === 0 ? (
        <div style={{ color: 'var(--color-text-muted)', fontSize: '0.875rem', textAlign: 'center', padding: '1.5rem' }}>
          No platform breakdown available
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
                <th style={{ padding: '0.75rem 1rem', textAlign: 'center' }}>
                  <span style={{ color: '#10b981' }}>Easy</span>
                </th>
                <th style={{ padding: '0.75rem 1rem', textAlign: 'center' }}>
                  <span style={{ color: '#f59e0b' }}>Medium</span>
                </th>
                <th style={{ padding: '0.75rem 1rem', textAlign: 'center' }}>
                  <span style={{ color: '#ef4444' }}>Hard</span>
                </th>
                <th style={{ padding: '0.75rem 1rem', textAlign: 'right' }}>Total Solved</th>
              </tr>
            </thead>
            <tbody>
              {platformBreakdown.map((row) => {
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
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.625rem' }}>
                        <span style={{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: brandColor }} />
                        <span>{row.platform}</span>
                      </div>
                    </td>
                    <td className="mono" style={{ padding: '0.875rem 1rem', textAlign: 'center' }}>
                      {formatMetric(row.easy)}
                    </td>
                    <td className="mono" style={{ padding: '0.875rem 1rem', textAlign: 'center' }}>
                      {formatMetric(row.medium)}
                    </td>
                    <td className="mono" style={{ padding: '0.875rem 1rem', textAlign: 'center' }}>
                      {formatMetric(row.hard)}
                    </td>
                    <td className="mono" style={{ padding: '0.875rem 1rem', textAlign: 'right', fontWeight: 700, color: 'var(--color-text-primary)' }}>
                      {formatMetric(row.total)}
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

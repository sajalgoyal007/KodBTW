import React from 'react';
import { PlatformComparison } from '../../types/dashboard';

interface PlatformShareProps {
  comparisons: PlatformComparison[];
}

export const PlatformShare: React.FC<PlatformShareProps> = ({ comparisons }) => {
  const getBrandColor = (platform: string) => {
    switch (platform.toUpperCase()) {
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

  const formatPct = (val: number | null | undefined) =>
    val !== null && val !== undefined ? `${val.toFixed(1)}%` : '—';

  const formatSolved = (val: number | null | undefined) =>
    val !== null && val !== undefined ? val.toLocaleString() : '—';

  return (
    <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
      <div>
        <h3 style={{ fontSize: '1.125rem', fontWeight: 600 }}>Platform Contribution</h3>
        <p style={{ fontSize: '0.8125rem', color: 'var(--color-text-secondary)', marginTop: '0.125rem' }}>
          Share of solved problems by platform
        </p>
      </div>

      {/* Stacked contribution bar */}
      <div
        style={{
          display: 'flex',
          height: '10px',
          borderRadius: 'var(--radius-full)',
          overflow: 'hidden',
          backgroundColor: 'var(--color-bg-subtle)',
        }}
      >
        {comparisons.map((c) => (
          <div
            key={c.platform}
            style={{
              width: `${c.sharePercentage || 0}%`,
              backgroundColor: getBrandColor(c.platform),
              transition: 'width 0.3s ease',
            }}
            title={`${c.platform}: ${formatPct(c.sharePercentage)}`}
          />
        ))}
      </div>

      {/* List breakdown */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
        {comparisons.map((item) => {
          const color = getBrandColor(item.platform);
          return (
            <div
              key={item.platform}
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: '0.625rem 0.875rem',
                backgroundColor: 'var(--color-bg-base)',
                borderRadius: 'var(--radius-md)',
                border: '1px solid var(--color-border-subtle)',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.625rem' }}>
                <span style={{ width: '10px', height: '10px', borderRadius: '50%', backgroundColor: color }} />
                <div>
                  <div style={{ fontWeight: 600, fontSize: '0.875rem' }}>{item.platform}</div>
                  <div className="mono" style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>
                    @{item.username}
                  </div>
                </div>
              </div>

              <div style={{ textAlign: 'right' }}>
                <div className="mono" style={{ fontSize: '0.9375rem', fontWeight: 700, color: 'var(--color-text-primary)' }}>
                  {formatPct(item.sharePercentage)}
                </div>
                <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>
                  {formatSolved(item.totalSolved)} solved
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

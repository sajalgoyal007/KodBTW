import React from 'react';
import { PlatformComparison } from '../../types/dashboard';

interface PlatformShareCardProps {
  platformComparison: PlatformComparison[];
}

export const PlatformShareCard: React.FC<PlatformShareCardProps> = ({ platformComparison }) => {
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

  const formatPct = (val: number | null | undefined) =>
    val !== null && val !== undefined ? `${val.toFixed(1)}%` : '0.0%';

  const formatSolved = (val: number | null | undefined) =>
    val !== null && val !== undefined ? val.toLocaleString() : '—';

  return (
    <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
      <div>
        <h3 style={{ fontSize: '1.125rem', fontWeight: 600 }}>Platform Problem Share</h3>
        <p style={{ fontSize: '0.8125rem', color: 'var(--color-text-secondary)', marginTop: '0.125rem' }}>
          Contribution percentage to your total solved count
        </p>
      </div>

      {/* Proportional Stacked Bar */}
      <div
        style={{
          display: 'flex',
          height: '12px',
          borderRadius: 'var(--radius-full)',
          overflow: 'hidden',
          backgroundColor: 'var(--color-bg-subtle)',
        }}
      >
        {platformComparison.map((c) => (
          <div
            key={c.platform}
            style={{
              width: `${c.sharePercentage || 0}%`,
              backgroundColor: getBrandColor(c.platform),
              transition: 'width 0.4s ease',
            }}
            title={`${c.platform}: ${formatPct(c.sharePercentage)}`}
          />
        ))}
      </div>

      {/* List Breakdown */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
        {platformComparison.map((item) => {
          const color = getBrandColor(item.platform);
          return (
            <div
              key={item.platform}
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: '0.75rem 1rem',
                backgroundColor: 'var(--color-bg-base)',
                borderRadius: 'var(--radius-md)',
                border: '1px solid var(--color-border-subtle)',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                <span style={{ width: '10px', height: '10px', borderRadius: '50%', backgroundColor: color }} />
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <span style={{ fontWeight: 600, fontSize: '0.9375rem' }}>{item.platform}</span>
                    {getSourceBadge(item.source)}
                  </div>
                  <div className="mono" style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', marginTop: '0.125rem' }}>
                    @{item.username}
                  </div>
                </div>
              </div>

              <div style={{ textAlign: 'right' }}>
                <div className="mono" style={{ fontSize: '1rem', fontWeight: 700, color: 'var(--color-text-primary)' }}>
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

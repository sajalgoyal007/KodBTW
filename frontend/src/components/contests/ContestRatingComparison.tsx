import React from 'react';
import { PlatformStats } from '../../types/dashboard';
import { TrendingUp } from 'lucide-react';

interface ContestRatingComparisonProps {
  platforms: PlatformStats[];
}

export const ContestRatingComparison: React.FC<ContestRatingComparisonProps> = ({ platforms }) => {
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

  const ratedPlatforms = platforms.filter((p) => p.rating !== null && p.rating !== undefined);

  return (
    <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.625rem' }}>
          <div
            style={{
              width: '32px',
              height: '32px',
              borderRadius: 'var(--radius-md)',
              backgroundColor: 'rgba(59, 130, 246, 0.12)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#3b82f6',
            }}
          >
            <TrendingUp size={18} />
          </div>
          <div>
            <h3 style={{ fontSize: '1.125rem', fontWeight: 600 }}>Platform Contest Ratings</h3>
            <p style={{ fontSize: '0.8125rem', color: 'var(--color-text-secondary)', marginTop: '0.125rem' }}>
              Ratings use platform-specific scales and should not be compared directly.
            </p>
          </div>
        </div>
      </div>

      {ratedPlatforms.length === 0 ? (
        <div style={{ color: 'var(--color-text-muted)', fontSize: '0.875rem', textAlign: 'center', padding: '1.5rem' }}>
          No contest ratings recorded yet across connected accounts
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {ratedPlatforms.map((item) => {
            const rating = item.rating!;
            const brandColor = getPlatformBrandColor(item.platform);

            return (
              <div
                key={item.platform}
                style={{
                  backgroundColor: 'var(--color-bg-base)',
                  padding: '0.875rem 1rem',
                  borderRadius: 'var(--radius-md)',
                  border: '1px solid var(--color-border-subtle)',
                  display: 'flex',
                  flexDirection: 'column',
                  gap: '0.5rem',
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <span style={{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: brandColor }} />
                    <span style={{ fontWeight: 600, fontSize: '0.875rem' }}>{item.platform}</span>
                    <span className="mono" style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>
                      @{item.username}
                    </span>
                  </div>

                  <div className="mono" style={{ fontSize: '1rem', fontWeight: 700, color: 'var(--color-text-primary)' }}>
                    {rating.toLocaleString()}
                  </div>
                </div>

              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

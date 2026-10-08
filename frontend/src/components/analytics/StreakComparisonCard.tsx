import React from 'react';
import { PlatformComparison } from '../../types/dashboard';
import { Flame } from 'lucide-react';

interface StreakComparisonCardProps {
  platformComparison: PlatformComparison[];
}

export const StreakComparisonCard: React.FC<StreakComparisonCardProps> = ({ platformComparison }) => {
  const formatDays = (val: number | null | undefined) =>
    val !== null && val !== undefined ? `${val} days` : '—';

  // Find max longest streak for relative progress bar scaling
  const maxStreak = Math.max(
    ...platformComparison.map((p) => Math.max(p.currentStreak || 0, p.longestStreak || 0)),
    1
  );

  return (
    <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.625rem' }}>
          <div
            style={{
              width: '32px',
              height: '32px',
              borderRadius: 'var(--radius-md)',
              backgroundColor: 'rgba(235, 115, 18, 0.12)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'var(--color-primary)',
            }}
          >
            <Flame size={18} />
          </div>
          <div>
            <h3 style={{ fontSize: '1.125rem', fontWeight: 600 }}>Streak Comparison</h3>
            <p style={{ fontSize: '0.8125rem', color: 'var(--color-text-secondary)', marginTop: '0.125rem' }}>
              Active daily momentum vs all-time records
            </p>
          </div>
        </div>
      </div>

      {platformComparison.length === 0 ? (
        <div style={{ color: 'var(--color-text-muted)', fontSize: '0.875rem', textAlign: 'center', padding: '1.5rem' }}>
          No streak data available
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {platformComparison.map((item) => {
            const curVal = item.currentStreak ?? 0;
            const longVal = item.longestStreak ?? 0;
            const curPct = Math.min((curVal / maxStreak) * 100, 100);
            const longPct = Math.min((longVal / maxStreak) * 100, 100);

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
                  gap: '0.625rem',
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <span style={{ fontWeight: 600, fontSize: '0.875rem' }}>{item.platform}</span>
                    <span className="mono" style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>
                      @{item.username}
                    </span>
                  </div>

                  <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', fontSize: '0.75rem' }}>
                    <div>
                      <span style={{ color: 'var(--color-text-muted)' }}>Current: </span>
                      <span className="mono" style={{ color: 'var(--color-primary)', fontWeight: 700 }}>
                        {formatDays(item.currentStreak)}
                      </span>
                    </div>
                    <div>
                      <span style={{ color: 'var(--color-text-muted)' }}>Best: </span>
                      <span className="mono" style={{ color: 'var(--color-text-primary)', fontWeight: 700 }}>
                        {formatDays(item.longestStreak)}
                      </span>
                    </div>
                  </div>
                </div>

                {/* Dual comparative bar */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem' }}>
                  {/* Current streak bar */}
                  <div
                    style={{
                      height: '6px',
                      backgroundColor: 'var(--color-bg-subtle)',
                      borderRadius: 'var(--radius-full)',
                      overflow: 'hidden',
                    }}
                  >
                    <div
                      style={{
                        width: `${curPct}%`,
                        height: '100%',
                        backgroundColor: 'var(--color-primary)',
                        borderRadius: 'var(--radius-full)',
                        transition: 'width 0.4s ease',
                      }}
                      title={`Current: ${curVal} days`}
                    />
                  </div>

                  {/* Longest streak bar */}
                  <div
                    style={{
                      height: '6px',
                      backgroundColor: 'var(--color-bg-subtle)',
                      borderRadius: 'var(--radius-full)',
                      overflow: 'hidden',
                    }}
                  >
                    <div
                      style={{
                        width: `${longPct}%`,
                        height: '100%',
                        backgroundColor: '#64748b',
                        borderRadius: 'var(--radius-full)',
                        transition: 'width 0.4s ease',
                      }}
                      title={`Longest: ${longVal} days`}
                    />
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

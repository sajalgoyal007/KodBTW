import React from 'react';
import { DifficultyAnalytics } from '../../types/dashboard';

interface DifficultyDistributionCardProps {
  difficulty: DifficultyAnalytics | null;
}

export const DifficultyDistributionCard: React.FC<DifficultyDistributionCardProps> = ({ difficulty }) => {
  const total = difficulty?.totalProblemsSolved ?? 0;
  const easyCount = difficulty?.easy?.count ?? null;
  const easyPct = difficulty?.easy?.percentage ?? 0;

  const medCount = difficulty?.medium?.count ?? null;
  const medPct = difficulty?.medium?.percentage ?? 0;

  const hardCount = difficulty?.hard?.count ?? null;
  const hardPct = difficulty?.hard?.percentage ?? 0;

  // SVG Donut calculations (radius = 54, circumference ~ 339.292)
  const radius = 54;
  const circumference = 2 * Math.PI * radius;

  const hasData = total > 0;
  const easyStroke = hasData ? (easyPct / 100) * circumference : 0;
  const medStroke = hasData ? (medPct / 100) * circumference : 0;
  const hardStroke = hasData ? (hardPct / 100) * circumference : 0;

  const formatCount = (val: number | null) =>
    val !== null && val !== undefined ? val.toLocaleString() : '—';

  const formatPct = (val: number | null) =>
    val !== null && val !== undefined ? `${val.toFixed(1)}%` : '0.0%';

  return (
    <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
      <div>
        <h3 style={{ fontSize: '1.125rem', fontWeight: 600 }}>Difficulty Distribution</h3>
        <p style={{ fontSize: '0.8125rem', color: 'var(--color-text-secondary)', marginTop: '0.125rem' }}>
          Proportional split across problem complexity
        </p>
      </div>

      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-around',
          flexWrap: 'wrap',
          gap: '1.5rem',
        }}
      >
        {/* SVG Donut Ring */}
        <div style={{ position: 'relative', width: '160px', height: '160px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <svg
            width="160"
            height="160"
            viewBox="0 0 140 140"
            style={{ transform: 'rotate(-90deg)', overflow: 'visible' }}
          >
            {/* Background Track */}
            <circle
              cx="70"
              cy="70"
              r={radius}
              fill="transparent"
              stroke="var(--color-bg-subtle)"
              strokeWidth="14"
            />

            {/* Easy Segment */}
            {hasData && (
              <circle
                cx="70"
                cy="70"
                r={radius}
                fill="transparent"
                stroke="#10b981"
                strokeWidth="14"
                strokeDasharray={`${easyStroke} ${circumference}`}
                strokeDashoffset="0"
                style={{ transition: 'stroke-dasharray 0.5s ease' }}
              />
            )}

            {/* Medium Segment */}
            {hasData && (
              <circle
                cx="70"
                cy="70"
                r={radius}
                fill="transparent"
                stroke="#f59e0b"
                strokeWidth="14"
                strokeDasharray={`${medStroke} ${circumference}`}
                strokeDashoffset={-easyStroke}
                style={{ transition: 'stroke-dasharray 0.5s ease' }}
              />
            )}

            {/* Hard Segment */}
            {hasData && (
              <circle
                cx="70"
                cy="70"
                r={radius}
                fill="transparent"
                stroke="#ef4444"
                strokeWidth="14"
                strokeDasharray={`${hardStroke} ${circumference}`}
                strokeDashoffset={-(easyStroke + medStroke)}
                style={{ transition: 'stroke-dasharray 0.5s ease' }}
              />
            )}
          </svg>

          {/* Center Count */}
          <div
            style={{
              position: 'absolute',
              textAlign: 'center',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
            }}
          >
            <span
              className="mono"
              style={{
                fontSize: '1.75rem',
                fontWeight: 800,
                color: 'var(--color-text-primary)',
                lineHeight: 1,
              }}
            >
              {total.toLocaleString()}
            </span>
            <span
              style={{
                fontSize: '0.6875rem',
                textTransform: 'uppercase',
                letterSpacing: '0.05em',
                color: 'var(--color-text-muted)',
                marginTop: '0.25rem',
              }}
            >
              Total Solved
            </span>
          </div>
        </div>

        {/* Legend / Metrics List */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.875rem', minWidth: '180px', flex: 1 }}>
          {/* Easy */}
          <div
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
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <span style={{ width: '10px', height: '10px', borderRadius: '50%', backgroundColor: '#10b981' }} />
              <span style={{ fontSize: '0.875rem', fontWeight: 600, color: '#10b981' }}>Easy</span>
            </div>
            <div style={{ textAlign: 'right' }}>
              <span className="mono" style={{ fontSize: '0.9375rem', fontWeight: 700 }}>{formatCount(easyCount)}</span>
              <span style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', marginLeft: '0.5rem' }}>
                ({formatPct(easyPct)})
              </span>
            </div>
          </div>

          {/* Medium */}
          <div
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
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <span style={{ width: '10px', height: '10px', borderRadius: '50%', backgroundColor: '#f59e0b' }} />
              <span style={{ fontSize: '0.875rem', fontWeight: 600, color: '#f59e0b' }}>Medium</span>
            </div>
            <div style={{ textAlign: 'right' }}>
              <span className="mono" style={{ fontSize: '0.9375rem', fontWeight: 700 }}>{formatCount(medCount)}</span>
              <span style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', marginLeft: '0.5rem' }}>
                ({formatPct(medPct)})
              </span>
            </div>
          </div>

          {/* Hard */}
          <div
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
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <span style={{ width: '10px', height: '10px', borderRadius: '50%', backgroundColor: '#ef4444' }} />
              <span style={{ fontSize: '0.875rem', fontWeight: 600, color: '#ef4444' }}>Hard</span>
            </div>
            <div style={{ textAlign: 'right' }}>
              <span className="mono" style={{ fontSize: '0.9375rem', fontWeight: 700 }}>{formatCount(hardCount)}</span>
              <span style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', marginLeft: '0.5rem' }}>
                ({formatPct(hardPct)})
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

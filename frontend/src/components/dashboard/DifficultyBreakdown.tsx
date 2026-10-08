import React from 'react';
import { DifficultyAnalytics } from '../../types/dashboard';

interface DifficultyBreakdownProps {
  difficulty: DifficultyAnalytics | null;
}

export const DifficultyBreakdown: React.FC<DifficultyBreakdownProps> = ({ difficulty }) => {
  const easyCount = difficulty?.easy?.count;
  const easyPct = difficulty?.easy?.percentage;

  const medCount = difficulty?.medium?.count;
  const medPct = difficulty?.medium?.percentage;

  const hardCount = difficulty?.hard?.count;
  const hardPct = difficulty?.hard?.percentage;

  const total = difficulty?.totalProblemsSolved;

  const formatCount = (val: number | null | undefined) =>
    val !== null && val !== undefined ? val.toLocaleString() : '—';

  const formatPct = (val: number | null | undefined) =>
    val !== null && val !== undefined ? `${val.toFixed(1)}%` : '—';

  return (
    <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h3 style={{ fontSize: '1.125rem', fontWeight: 600 }}>Difficulty Distribution</h3>
          <p style={{ fontSize: '0.8125rem', color: 'var(--color-text-secondary)', marginTop: '0.125rem' }}>
            Combined breakdown across all connected accounts
          </p>
        </div>
        <div style={{ textAlign: 'right' }}>
          <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', textTransform: 'uppercase' }}>Total Solved</div>
          <div className="mono" style={{ fontSize: '1.25rem', fontWeight: 700, color: 'var(--color-text-primary)' }}>
            {formatCount(total)}
          </div>
        </div>
      </div>

      {/* Progress Bar Stack */}
      <div
        style={{
          display: 'flex',
          height: '10px',
          borderRadius: 'var(--radius-full)',
          overflow: 'hidden',
          backgroundColor: 'var(--color-bg-subtle)',
        }}
      >
        <div
          style={{
            width: `${easyPct || 0}%`,
            backgroundColor: 'var(--color-success)',
            transition: 'width 0.3s ease',
          }}
          title={`Easy: ${formatPct(easyPct)}`}
        />
        <div
          style={{
            width: `${medPct || 0}%`,
            backgroundColor: 'var(--color-warning)',
            transition: 'width 0.3s ease',
          }}
          title={`Medium: ${formatPct(medPct)}`}
        />
        <div
          style={{
            width: `${hardPct || 0}%`,
            backgroundColor: 'var(--color-error)',
            transition: 'width 0.3s ease',
          }}
          title={`Hard: ${formatPct(hardPct)}`}
        />
      </div>

      {/* Detailed 3-column breakdown */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(130px, 1fr))', gap: '1rem' }}>
        {/* Easy */}
        <div
          style={{
            backgroundColor: 'var(--color-bg-base)',
            border: '1px solid var(--color-border-subtle)',
            borderRadius: 'var(--radius-md)',
            padding: '0.875rem 1rem',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.375rem', marginBottom: '0.375rem' }}>
            <span style={{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: 'var(--color-success)' }} />
            <span style={{ fontSize: '0.8125rem', fontWeight: 600, color: 'var(--color-success)' }}>Easy</span>
          </div>
          <div className="mono" style={{ fontSize: '1.25rem', fontWeight: 700 }}>
            {formatCount(easyCount)}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', marginTop: '0.125rem' }}>
            {formatPct(easyPct)}
          </div>
        </div>

        {/* Medium */}
        <div
          style={{
            backgroundColor: 'var(--color-bg-base)',
            border: '1px solid var(--color-border-subtle)',
            borderRadius: 'var(--radius-md)',
            padding: '0.875rem 1rem',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.375rem', marginBottom: '0.375rem' }}>
            <span style={{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: 'var(--color-warning)' }} />
            <span style={{ fontSize: '0.8125rem', fontWeight: 600, color: 'var(--color-warning)' }}>Medium</span>
          </div>
          <div className="mono" style={{ fontSize: '1.25rem', fontWeight: 700 }}>
            {formatCount(medCount)}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', marginTop: '0.125rem' }}>
            {formatPct(medPct)}
          </div>
        </div>

        {/* Hard */}
        <div
          style={{
            backgroundColor: 'var(--color-bg-base)',
            border: '1px solid var(--color-border-subtle)',
            borderRadius: 'var(--radius-md)',
            padding: '0.875rem 1rem',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.375rem', marginBottom: '0.375rem' }}>
            <span style={{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: 'var(--color-error)' }} />
            <span style={{ fontSize: '0.8125rem', fontWeight: 600, color: 'var(--color-error)' }}>Hard</span>
          </div>
          <div className="mono" style={{ fontSize: '1.25rem', fontWeight: 700 }}>
            {formatCount(hardCount)}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', marginTop: '0.125rem' }}>
            {formatPct(hardPct)}
          </div>
        </div>
      </div>
    </div>
  );
};

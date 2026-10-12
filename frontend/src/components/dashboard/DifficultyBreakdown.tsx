import React from 'react';
import { DifficultyAnalytics } from '../../types/dashboard';
import { getDifficultyCoverage, getKnownDifficultyPercentage } from '../../utils/dashboardPresentation';

interface DifficultyBreakdownProps {
  difficulty: DifficultyAnalytics | null;
  connectedPlatformCount?: number;
}

const metrics = [
  { key: 'easy', label: 'Easy', color: 'var(--color-success)' },
  { key: 'medium', label: 'Medium', color: 'var(--color-warning)' },
  { key: 'hard', label: 'Hard', color: 'var(--color-error)' },
] as const;

export const DifficultyBreakdown: React.FC<DifficultyBreakdownProps> = ({ difficulty, connectedPlatformCount = 0 }) => {
  const formatCount = (value: number | null | undefined) => value !== null && value !== undefined ? value.toLocaleString() : '—';
  const formatPct = (value: number | null | undefined) => value !== null && value !== undefined ? `${value.toFixed(1)}%` : '—';
  const coverage = getDifficultyCoverage(difficulty, connectedPlatformCount);
  const segments = metrics.flatMap((metric) => {
    const percentage = getKnownDifficultyPercentage(difficulty?.[metric.key]);
    if (percentage === null) return [];
    return [{ ...metric, percentage: Math.max(0, Math.min(100, percentage)) }];
  });

  return (
    <section className="card dashboard-analytics-card" aria-labelledby="difficulty-title">
      <div className="dashboard-analytics-heading">
        <div>
          <h3 id="difficulty-title">Difficulty distribution</h3>
          <p>Combined breakdown across connected accounts</p>
        </div>
        <div className="dashboard-analytics-total">
          <span>Total solved</span>
          <strong className="mono">{formatCount(difficulty?.totalProblemsSolved)}</strong>
        </div>
      </div>

      <div
        className="dashboard-breakdown-bar"
        role="img"
        aria-label={`Difficulty distribution: easy ${formatPct(getKnownDifficultyPercentage(difficulty?.easy))}, medium ${formatPct(getKnownDifficultyPercentage(difficulty?.medium))}, hard ${formatPct(getKnownDifficultyPercentage(difficulty?.hard))}`}
      >
        {segments.map((segment) => (
          <span
            key={segment.key}
            style={{ width: `${segment.percentage}%`, backgroundColor: segment.color }}
            title={`${segment.label}: ${formatPct(getKnownDifficultyPercentage(difficulty?.[segment.key]))}`}
          />
        ))}
      </div>

      <div className="dashboard-difficulty-grid">
        {metrics.map((metric) => (
          <div className="dashboard-difficulty-item" key={metric.key}>
            <div className="dashboard-difficulty-label"><span style={{ backgroundColor: metric.color }} />{metric.label}</div>
            <strong className="mono">{formatCount(difficulty?.[metric.key]?.count)}</strong>
            <span>{formatPct(getKnownDifficultyPercentage(difficulty?.[metric.key]))}</span>
          </div>
        ))}
      </div>
      <p className="dashboard-coverage-note" role="status">{coverage.message}</p>
    </section>
  );
};

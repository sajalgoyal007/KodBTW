import React from 'react';
import { ContestAnalytics } from '../../types/dashboard';
import { Trophy } from 'lucide-react';

interface ContestSummaryProps {
  contests: ContestAnalytics | null;
}

export const ContestSummary: React.FC<ContestSummaryProps> = ({ contests }) => {
  const formatMetric = (val: number | null | undefined) =>
    val !== null && val !== undefined ? val.toLocaleString() : '—';

  const breakdown = contests?.platformBreakdown || [];

  return (
    <section className="card dashboard-contest-summary" aria-labelledby="dashboard-contest-summary-title">
      <div className="dashboard-contest-heading">
        <div className="dashboard-contest-title-group">
          <div
            style={{
              width: '32px',
              height: '32px',
              borderRadius: 'var(--radius-md)',
              backgroundColor: 'var(--color-primary-soft)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'var(--color-primary)',
            }}
          >
            <Trophy size={18} aria-hidden="true" />
          </div>
          <div>
            <h3 id="dashboard-contest-summary-title" style={{ fontSize: '1.125rem', fontWeight: 600 }}>Contest Standing</h3>
            <p style={{ fontSize: '0.8125rem', color: 'var(--color-text-secondary)', marginTop: '0.125rem' }}>
              Ratings and rank across competitive platforms
            </p>
          </div>
        </div>

        <div className="dashboard-contest-total">
          <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', textTransform: 'uppercase' }}>Contest Participation Count</div>
          <div className="mono" style={{ fontSize: '1.25rem', fontWeight: 700, color: 'var(--color-text-primary)' }}>
            {formatMetric(contests?.totalContests)}
          </div>
        </div>
      </div>

      {breakdown.length === 0 ? (
        <div style={{ color: 'var(--color-text-muted)', fontSize: '0.875rem', textAlign: 'center', padding: '1rem' }}>
          No contest data available
        </div>
      ) : (
        <div className="dashboard-contest-table-scroll" role="region" tabIndex={0} aria-label="Contest standings table; scroll horizontally to view all columns">
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.875rem' }}>
            <caption className="sr-only">Contest participation, rating, and rank by platform</caption>
            <thead>
              <tr style={{ borderBottom: '1px solid var(--color-border-subtle)', textAlign: 'left', color: 'var(--color-text-muted)', fontSize: '0.75rem', textTransform: 'uppercase' }}>
                <th style={{ padding: '0.625rem 0.75rem' }}>Platform</th>
                <th style={{ padding: '0.625rem 0.75rem', textAlign: 'center' }}>Contests</th>
                <th style={{ padding: '0.625rem 0.75rem', textAlign: 'center' }}>Rating</th>
                <th style={{ padding: '0.625rem 0.75rem', textAlign: 'right' }}>Rank</th>
              </tr>
            </thead>
            <tbody>
              {breakdown.map((row) => (
                <tr
                  key={row.platform}
                  style={{
                    borderBottom: '1px solid var(--color-border-subtle)',
                    transition: 'background-color 0.15s ease',
                  }}
                >
                  <td style={{ padding: '0.75rem', fontWeight: 600 }}>
                    {row.platform}
                  </td>
                  <td className="mono" style={{ padding: '0.75rem', textAlign: 'center' }}>
                    {formatMetric(row.contests)}
                  </td>
                  <td className="mono" style={{ padding: '0.75rem', textAlign: 'center', color: row.rating ? 'var(--color-primary)' : undefined, fontWeight: row.rating ? 700 : undefined }}>
                    {formatMetric(row.rating)}
                  </td>
                  <td className="mono" style={{ padding: '0.75rem', textAlign: 'right', color: 'var(--color-text-secondary)' }}>
                    {formatMetric(row.rank)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
};

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
    <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
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
            <Trophy size={18} />
          </div>
          <div>
            <h3 style={{ fontSize: '1.125rem', fontWeight: 600 }}>Contest Standing</h3>
            <p style={{ fontSize: '0.8125rem', color: 'var(--color-text-secondary)', marginTop: '0.125rem' }}>
              Ratings and rank across competitive platforms
            </p>
          </div>
        </div>

        <div style={{ textAlign: 'right' }}>
          <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', textTransform: 'uppercase' }}>Total Contests</div>
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
        <div style={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.875rem' }}>
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
    </div>
  );
};

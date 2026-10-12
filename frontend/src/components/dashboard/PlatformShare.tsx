import React from 'react';
import { PlatformComparison } from '../../types/dashboard';
import { getKnownPlatformSharePercentage } from '../../utils/dashboardPresentation';

interface PlatformShareProps {
  comparisons: PlatformComparison[];
}

const getBrandColor = (platform: string) => {
  switch (platform.toUpperCase()) {
    case 'LEETCODE': return '#f89f1b';
    case 'CODEFORCES': return '#1f8acb';
    case 'CODECHEF': return '#8b4513';
    case 'GEEKSFORGEEKS': return '#2f8d46';
    case 'HACKERRANK': return '#2ec866';
    default: return 'var(--color-primary)';
  }
};

const formatPct = (value: number | null) => value !== null ? `${value.toFixed(1)}%` : '—';
const formatSolved = (value: number | null) => value !== null ? value.toLocaleString() : '—';

export const PlatformShare: React.FC<PlatformShareProps> = ({ comparisons }) => {
  const knownComparisons = comparisons.filter((item) => getKnownPlatformSharePercentage(item) !== null);

  return (
    <section className="card dashboard-analytics-card" aria-labelledby="platform-contribution-title">
      <div>
        <h3 id="platform-contribution-title">Platform contribution</h3>
        <p className="dashboard-analytics-subtitle">Share of solved problems by platform</p>
      </div>

      {knownComparisons.length > 0 ? (
        <div className="dashboard-breakdown-bar" role="img" aria-label={`Solved-problem share across ${knownComparisons.length} platforms`}>
          {knownComparisons.map((item) => {
            const share = getKnownPlatformSharePercentage(item);
            return <span key={item.platform} style={{ width: `${share}%`, backgroundColor: getBrandColor(item.platform) }} title={`${item.platform}: ${formatPct(share)}`} />;
          })}
        </div>
      ) : (
        <p className="dashboard-coverage-note">Platform shares are unavailable until a solved count is reported.</p>
      )}

      <div className="dashboard-platform-share-list">
        {comparisons.map((item) => {
          const share = getKnownPlatformSharePercentage(item);
          const color = getBrandColor(item.platform);
          return (
            <div className="dashboard-platform-share-row" key={item.platform}>
              <div className="dashboard-platform-share-identity">
                <span style={{ backgroundColor: color }} aria-hidden="true" />
                <div><strong>{item.platform}</strong><span className="mono dashboard-platform-share-username">@{item.username}</span></div>
              </div>
              <div className="dashboard-platform-share-value"><strong className="mono">{formatPct(share)}</strong><span>{formatSolved(item.totalSolved)} solved</span></div>
            </div>
          );
        })}
      </div>
    </section>
  );
};

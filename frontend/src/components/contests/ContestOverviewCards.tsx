import React from 'react';
import { ContestOverviewMetric } from '../../types/contest';
import { MetricCard } from '../dashboard/MetricCard';
import { Trophy, Award, Layers } from 'lucide-react';

interface ContestOverviewCardsProps {
  metrics: ContestOverviewMetric;
}

export const ContestOverviewCards: React.FC<ContestOverviewCardsProps> = ({ metrics }) => {
  return (
    <div
      style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
        gap: '1rem',
      }}
    >
      <MetricCard
        title="Total Contests"
        value={metrics.totalContests}
        subtitle="Aggregated events attended"
        icon={<Trophy size={20} />}
        accentColor="var(--color-primary)"
      />

      <MetricCard
        title="Peak Rating"
        value={metrics.highestRating}
        subtitle={
          metrics.highestRatingPlatform
            ? `Highest on ${metrics.highestRatingPlatform}`
            : 'Across active platforms'
        }
        icon={<Award size={20} />}
        accentColor="#3b82f6"
      />

      <MetricCard
        title="Active Platforms"
        value={metrics.activeContestPlatformsCount}
        subtitle="Profiles with contest activity"
        icon={<Layers size={20} />}
        accentColor="#10b981"
      />
    </div>
  );
};

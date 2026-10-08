import React from 'react';
import { ContestOverviewMetric } from '../../types/contest';
import { MetricCard } from '../dashboard/MetricCard';
import { Trophy, Layers } from 'lucide-react';

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
        title="Contest Participation Count"
        value={metrics.totalContests}
        subtitle="Sum of reported counts; platforms may define participation differently"
        icon={<Trophy size={20} />}
        accentColor="var(--color-primary)"
      />

      <MetricCard
        title="Active Platforms"
        value={metrics.activeContestPlatformsCount}
        subtitle="Profiles with contest activity"
        icon={<Layers size={20} />}
        accentColor="var(--color-success)"
      />
    </div>
  );
};

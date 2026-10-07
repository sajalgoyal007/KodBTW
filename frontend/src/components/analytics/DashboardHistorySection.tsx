import React, { useState } from 'react';
import { AlertCircle, Clock3, RefreshCw, TrendingUp } from 'lucide-react';
import { useDashboardHistory } from '../../hooks/useDashboardHistory';
import { HistoryRange } from '../../types/history';
import { HistoryChartSeries, HistoryLineChart } from './HistoryLineChart';

const RANGES: { value: HistoryRange; label: string }[] = [
  { value: '7d', label: '7D' },
  { value: '30d', label: '30D' },
  { value: '90d', label: '90D' },
  { value: '1y', label: '1Y' },
];
const COLORS = ['#eb7312', '#38bdf8', '#a78bfa', '#34d399', '#f472b6'];
const DIFFICULTIES = [
  { key: 'easySolved' as const, name: 'Easy', color: '#34d399' },
  { key: 'mediumSolved' as const, name: 'Medium', color: '#f59e0b' },
  { key: 'hardSolved' as const, name: 'Hard', color: '#f87171' },
];

export const DashboardHistorySection: React.FC = () => {
  const [range, setRange] = useState<HistoryRange>('30d');
  const { data, loading, error, refetch } = useDashboardHistory(range);
  const dateLabel = (value: string | null) => value
    ? new Date(value).toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' })
    : 'No sync timestamp';

  const totalSeries: HistoryChartSeries[] = data ? [{
    name: 'Observed total',
    color: '#eb7312',
    points: data.overallSolved.map((point) => ({ date: point.date, value: point.totalSolved })),
  }] : [];
  const platformSeries: HistoryChartSeries[] = (data?.platforms ?? []).map((platform, index) => ({
    name: platform.platform,
    color: COLORS[index % COLORS.length],
    points: platform.points.map((point) => ({ date: point.date, value: point.totalSolved })),
  }));
  const difficultySeries: HistoryChartSeries[] = data ? DIFFICULTIES.map((difficulty) => ({
    name: difficulty.name,
    color: difficulty.color,
    points: data.overallSolved.map((point) => ({ date: point.date, value: point[difficulty.key] })),
  })) : [];
  const ratingSeries: HistoryChartSeries[] = (data?.platforms ?? []).map((platform, index) => ({
    name: platform.platform,
    color: COLORS[index % COLORS.length],
    points: platform.points.map((point) => ({ date: point.date, value: point.rating })),
  }));
  const ratingAvailable = ratingSeries.some((series) => series.points.some((point) => point.value !== null));

  return (
    <section className="history-section">
      <div className="history-heading">
        <div>
          <div className="history-title-row"><TrendingUp size={20} color="var(--color-primary)" /><h2>Progress History</h2></div>
          <p>Historical tracking begins when real platform snapshots are recorded.</p>
        </div>
        <div className="history-controls" role="group" aria-label="History time range">
          {RANGES.map((item) => (
            <button key={item.value} type="button" className={`history-range ${range === item.value ? 'active' : ''}`}
              aria-pressed={range === item.value} disabled={loading} onClick={() => setRange(item.value)}>
              {item.label}
            </button>
          ))}
        </div>
      </div>

      {data && !loading && !error && (
        <div className="history-freshness"><Clock3 size={15} />
          <span>Last updated {dateLabel(data.freshness.lastUpdatedAt)}</span>
          {data.freshness.latestSnapshotDate && <span>· Latest snapshot {data.freshness.latestSnapshotDate}</span>}
        </div>
      )}

      {loading && <div className="history-loading card" aria-live="polite"><RefreshCw size={18} className="spinner" /> Loading history…</div>}

      {!loading && error && (
        <div className="history-message card error" role="alert">
          <AlertCircle size={20} /><span>{error}</span>
          <button type="button" className="btn btn-secondary" onClick={() => void refetch()}>Retry</button>
        </div>
      )}

      {!loading && !error && data && data.snapshotDates.length === 0 && (
        <div className="history-message card">
          <div className="history-empty-icon"><TrendingUp size={23} /></div>
          <div><h3>No real history yet</h3><p>Progress appears after your first successful real platform sync. Earlier history is not invented or backfilled.</p></div>
        </div>
      )}

      {!loading && !error && data && data.snapshotDates.length > 0 && (
        <>
          {data.snapshotDates.length === 1 && <p className="history-insufficient">One observation is available. Sync again on a later date to compare progress.</p>}
          <p className="history-data-note">Totals use only platforms and metrics recorded on each snapshot date. Missing observations are not counted as zero, and chart points represent recorded dates only.</p>
          <div className="history-chart-grid">
            <HistoryLineChart title="Solved progression" description="Combined values observed on each snapshot date" series={totalSeries} emptyMessage="No total-solved values in this range." />
            <HistoryLineChart title="Platform comparison" description="Real solved counts, shown only at each platform's recorded dates" series={platformSeries} emptyMessage="No platform totals available in this range." />
            <HistoryLineChart title="Difficulty progression" description="Easy, medium, and hard totals where providers report them" series={difficultySeries} emptyMessage="Difficulty history is not available for these snapshots." />
            {ratingAvailable && <HistoryLineChart title="Rating trend" description="Recorded rating observations; no rating is inferred between syncs" series={ratingSeries} emptyMessage="No ratings available in this range." />}
          </div>
        </>
      )}
    </section>
  );
};

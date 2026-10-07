import React from 'react';
import { AlertCircle, ArrowDownRight, ArrowUpRight, CheckCircle2, Clock3, Info, Lightbulb, RefreshCw, ShieldCheck } from 'lucide-react';
import { useDashboardInsights } from '../../hooks/useDashboardInsights';
import { InsightItem } from '../../types/insights';

const InsightCard: React.FC<{ insight: InsightItem }> = ({ insight }) => {
  const positive = insight.severity === 'POSITIVE';
  const Icon = positive ? ArrowUpRight : insight.type === 'PROGRESS_CORRECTION' || insight.type === 'RATING_DECREASE'
    ? ArrowDownRight : Info;
  const date = insight.date ? new Date(`${insight.date}T00:00:00Z`).toLocaleDateString(undefined, {
    year: 'numeric', month: 'short', day: 'numeric', timeZone: 'UTC',
  }) : null;

  return (
    <article className={`insight-card ${positive ? 'positive' : ''}`}>
      <div className="insight-card-icon"><Icon size={17} /></div>
      <div className="insight-card-content">
        <h4>{insight.title}</h4>
        <p>{insight.message}</p>
        <div className="insight-meta">
          {insight.platform && <span>{insight.platform}</span>}
          {insight.metric && <span>{insight.metric.replace(/_/g, ' ').toLowerCase()}</span>}
          {date && <span>{date}</span>}
        </div>
      </div>
    </article>
  );
};

const InsightGroup: React.FC<{ title: string; items: InsightItem[]; empty?: string }> = ({ title, items, empty }) => (
  <section className="insight-group">
    <h3>{title}</h3>
    {items.length ? <div className="insight-list">{items.map((item, index) => <InsightCard key={`${item.type}-${item.platform}-${item.date}-${index}`} insight={item} />)}</div>
      : empty ? <p className="insight-muted">{empty}</p> : null}
  </section>
);

export const DashboardInsightsSection: React.FC = () => {
  const { data, loading, error, refetch } = useDashboardInsights();
  const availability = data?.dataAvailability;
  const sparse = !!availability && availability.snapshotCount > 0 && !availability.hasHistoricalComparison;
  const hasData = !!availability && availability.snapshotCount > 0;

  return (
    <section className="insights-section" aria-labelledby="insights-heading">
      <div className="insights-heading">
        <div>
          <div className="insights-title"><Lightbulb size={20} /><h2 id="insights-heading">Advanced Insights</h2>
          <span className="badge badge-success"><ShieldCheck size={12} />REAL DATA</span></div>
          <p>Deterministic observations from your saved verified platform statistics</p>
        </div>
        {data && <span className="insights-freshness"><Clock3 size={14} /> Latest snapshot {availability?.latestSnapshotDate ?? 'unavailable'}</span>}
      </div>

      {loading && <div className="card insights-state" aria-live="polite"><RefreshCw size={18} className="spinner" /> Building insights from saved data…</div>}

      {!loading && error && (
        <div className="card insights-state error" role="alert"><AlertCircle size={20} /><span>{error}</span>
          <button type="button" className="btn btn-secondary" onClick={refetch}>Retry</button>
        </div>
      )}

      {!loading && !error && data && !hasData && (
        <div className="card insights-state insights-empty">
          <div className="insights-empty-icon"><Info size={21} /></div>
          <div><h3>No verified snapshot data yet</h3><p>Insights will appear after a real LeetCode or Codeforces sync. Mock platform values are not used.</p></div>
        </div>
      )}

      {!loading && !error && data && hasData && (
        <>
          <div className="card insights-summary">
            <div className="insights-summary-icon"><CheckCircle2 size={20} /></div>
            <div><span>Verified snapshot summary</span><p>{data.summary}</p></div>
            <div className="insights-counts"><strong>{availability!.snapshotCount}</strong><small>real snapshots</small><strong>{availability!.platformCount}</strong><small>platforms</small></div>
          </div>

          {sparse && <div className="insights-sparse" role="status">Insights are based on your current verified snapshot. More historical data will unlock progress and trend comparisons.</div>}

          <div className="insights-grid">
            <InsightGroup title="Strengths & progress" items={[...data.strengths, ...data.progressInsights.filter((item) => !data.strengths.some((strength) => strength.type === item.type && strength.platform === item.platform))]} empty="No comparable progress observations are available yet." />
            <InsightGroup title="Platform snapshots" items={data.platformInsights} />
            <InsightGroup title="Difficulty profile" items={data.difficultyInsights} empty="Difficulty insights are unavailable for the current REAL snapshots." />
            <InsightGroup title="Areas to explore" items={data.areasToImprove} empty="No specific data-backed gaps were identified." />
            {(data.ratingInsights.length > 0 || data.streakInsights.length > 0) && (
              <InsightGroup title="Rating & streaks" items={[...data.ratingInsights, ...data.streakInsights]} />
            )}
          </div>
          <p className="insights-disclaimer">Insights describe recorded observations. They do not fill gaps between syncs or estimate missing metrics.</p>
        </>
      )}
    </section>
  );
};

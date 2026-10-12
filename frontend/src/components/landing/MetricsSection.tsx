import React from 'react';

export const MetricsSection: React.FC = () => {
  return (
    <section className="kod-section" id="metrics">
      <div className="kod-section-header">
        <span className="kod-section-eyebrow">APPLICATION INTEGRITY METRICS</span>
        <h2 className="kod-section-title">See the signal behind the work.</h2>
        <p className="kod-section-subtitle">
          Platform connections and metric coverage vary by data source. Missing or unavailable metrics are not shown as zero.
        </p>
      </div>

      <div className="kod-metrics-editorial-grid">
        <div className="kod-metric-editorial-cell">
          <div className="kod-metric-big-num" style={{ color: 'var(--kod-accent-cyan)' }}>
            5
          </div>
          <h3 className="kod-metric-big-title">Major Platforms</h3>
          <p className="kod-metric-big-desc">
            Add handles for five platforms under one identity. Live statistics depend on each platform's supported source and may be unavailable.
          </p>
        </div>

        <div className="kod-metric-editorial-cell">
          <div className="kod-metric-big-num" style={{ color: 'var(--kod-text-primary)' }}>
            1
          </div>
          <h3 className="kod-metric-big-title">Unified Profile URL</h3>
          <p className="kod-metric-big-desc">
            Single canonical developer link <code style={{ color: 'var(--kod-accent-cyan)' }}>/u/:username</code> for resumes, portfolios, and job applications.
          </p>
        </div>

        <div className="kod-metric-editorial-cell">
          <div className="kod-metric-big-num" style={{ color: 'var(--kod-accent-violet-light)' }}>
            3
          </div>
          <h3 className="kod-metric-big-title">Difficulty Tiers</h3>
          <p className="kod-metric-big-desc">
            Easy, Medium, and Hard counts appear when a connected source provides those metrics.
          </p>
        </div>

        <div className="kod-metric-editorial-cell">
          <div className="kod-metric-big-num" style={{ color: 'var(--kod-accent-emerald)' }}>
            Snapshot
          </div>
          <h3 className="kod-metric-big-title">Stored Observations</h3>
          <p className="kod-metric-big-desc">
            Successful sync results can be stored as snapshots. Coverage depends on the source; unavailable metrics remain unavailable.
          </p>
        </div>
      </div>
    </section>
  );
};

import React from 'react';

export const MetricsSection: React.FC = () => {
  return (
    <section className="kod-section" id="metrics">
      <div className="kod-section-header">
        <span className="kod-section-eyebrow">APPLICATION INTEGRITY METRICS</span>
        <h2 className="kod-section-title">See the signal behind the work.</h2>
        <p className="kod-section-subtitle">
          Real system capabilities grounded strictly in our backend architecture and database schema.
        </p>
      </div>

      <div className="kod-metrics-editorial-grid">
        <div className="kod-metric-editorial-cell">
          <div className="kod-metric-big-num" style={{ color: 'var(--kod-accent-cyan)' }}>
            5
          </div>
          <h3 className="kod-metric-big-title">Major Platforms</h3>
          <p className="kod-metric-big-desc">
            Connect LeetCode, Codeforces, CodeChef, GeeksForGeeks, and HackerRank under one identity.
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
            Easy, Medium, and Hard problem classification normalized across connected platforms.
          </p>
        </div>

        <div className="kod-metric-editorial-cell">
          <div className="kod-metric-big-num" style={{ color: 'var(--kod-accent-emerald)' }}>
            100%
          </div>
          <h3 className="kod-metric-big-title">Deterministic Data</h3>
          <p className="kod-metric-big-desc">
            Snapshot-persisted in MySQL with Flyway schema versioning. Zero fake metrics or simulated numbers.
          </p>
        </div>
      </div>
    </section>
  );
};

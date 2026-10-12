import React, { useState } from 'react';
import {
  UserCheck,
  BarChart3,
  TrendingUp,
  Layers,
  Award,
} from 'lucide-react';

export const ProductShowcaseSection: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'profile' | 'analytics' | 'progress'>('profile');

  return (
    <section className="kod-section" id="product">
      <div className="kod-section-header center">
        <span className="kod-section-eyebrow">UNIFIED PRODUCT SHOWCASE</span>
        <h2 className="kod-section-title">One profile. A clearer picture of how you code.</h2>
        <p className="kod-section-subtitle">
          Explore how KodBTW organizes fragmented activity into an understandable developer intelligence profile.
        </p>
      </div>

      {/* Interactive Tabs */}
      <div className="kod-showcase-tabs" role="tablist">
        <button
          type="button"
          role="tab"
          aria-selected={activeTab === 'profile'}
          className={`kod-tab-btn ${activeTab === 'profile' ? 'active' : ''}`}
          onClick={() => setActiveTab('profile')}
        >
          <UserCheck size={16} />
          <span>Unified Profile</span>
        </button>

        <button
          type="button"
          role="tab"
          aria-selected={activeTab === 'analytics'}
          className={`kod-tab-btn ${activeTab === 'analytics' ? 'active' : ''}`}
          onClick={() => setActiveTab('analytics')}
        >
          <BarChart3 size={16} />
          <span>Coding Analytics</span>
        </button>

        <button
          type="button"
          role="tab"
          aria-selected={activeTab === 'progress'}
          className={`kod-tab-btn ${activeTab === 'progress' ? 'active' : ''}`}
          onClick={() => setActiveTab('progress')}
        >
          <TrendingUp size={16} />
          <span>Progress Trajectory</span>
        </button>
      </div>

      {/* Showcase Display Container */}
      <div className="kod-showcase-display">
        <p role="note" style={{ margin: '0 0 1.5rem', padding: '0.75rem 1rem', border: '1px solid var(--kod-border-hairline)', borderRadius: '8px', color: 'var(--kod-text-secondary)', fontSize: '0.85rem' }}>
          Illustrative product preview: all profile details, statistics, and charts below are sample data, not live account records.
        </p>
        {activeTab === 'profile' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem', borderBottom: '1px solid var(--kod-border-hairline)', paddingBottom: '1.5rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                <div style={{ width: '56px', height: '56px', borderRadius: '12px', background: 'var(--color-primary)', color: 'var(--color-primary-text)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 800, fontSize: '1.4rem' }}>
                  SD
                </div>
                <div>
                  <h3 style={{ fontSize: '1.25rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    Sample Developer
                  </h3>
                  <p style={{ color: 'var(--kod-text-secondary)', fontSize: '0.85rem' }}>
                    Example coding profile · sample details
                  </p>
                </div>
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                <span className="kod-dashboard-url-pill">
                  kodbtw.vercel.app/u/<span style={{ color: 'var(--kod-accent-cyan)' }}>sample-developer</span>
                </span>
                <span className="kod-dashboard-status-tag">SAMPLE PROFILE</span>
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '1rem' }}>
              <div className="kod-dash-panel">
                <span className="kod-metric-label">Sample Aggregated Solved</span>
                <span className="kod-metric-number">1,148</span>
                <span style={{ fontSize: '0.78rem', color: 'var(--kod-text-muted)' }}>Illustrative value across example accounts</span>
              </div>
              <div className="kod-dash-panel">
                <span className="kod-metric-label">Sample Platform Rating</span>
                <span className="kod-metric-number">2,084</span>
                <span style={{ fontSize: '0.78rem', color: 'var(--kod-accent-cyan)' }}>Example rating value</span>
              </div>
              <div className="kod-dash-panel">
                <span className="kod-metric-label">Sample Contests</span>
                <span className="kod-metric-number">48</span>
                <span style={{ fontSize: '0.78rem', color: 'var(--kod-text-muted)' }}>Illustrative value, not persisted history</span>
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1rem' }}>
              <div className="kod-dash-panel">
                <div className="kod-panel-title">
                  <Layers size={16} color="var(--kod-accent-cyan)" />
                  <span>Connected Platforms Overview</span>
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', marginTop: '0.5rem' }}>
                  <div className="kod-platform-sample-row" style={{ display: 'flex', justifyContent: 'space-between', padding: '0.6rem 0.85rem', border: '1px solid var(--kod-border-subtle)', borderRadius: '6px', fontSize: '0.85rem' }}>
                    <span style={{ fontWeight: 600 }}>LeetCode</span>
                    <b style={{ color: 'var(--kod-accent-cyan)' }}>Sample solved · sample rating</b>
                  </div>
                  <div className="kod-platform-sample-row" style={{ display: 'flex', justifyContent: 'space-between', padding: '0.6rem 0.85rem', border: '1px solid var(--kod-border-subtle)', borderRadius: '6px', fontSize: '0.85rem' }}>
                    <span style={{ fontWeight: 600 }}>Codeforces</span>
                    <b style={{ color: 'var(--kod-accent-violet)' }}>Sample solved · sample rating</b>
                  </div>
                  <div className="kod-platform-sample-row" style={{ display: 'flex', justifyContent: 'space-between', padding: '0.6rem 0.85rem', border: '1px solid var(--kod-border-subtle)', borderRadius: '6px', fontSize: '0.85rem' }}>
                    <span style={{ fontWeight: 600 }}>CodeChef</span>
                    <span style={{ color: 'var(--kod-text-muted)' }}>Example connection · metrics vary</span>
                  </div>
                </div>
              </div>

              <div className="kod-dash-panel">
                <div className="kod-panel-title">
                  <Award size={16} color="var(--kod-accent-amber)" />
                  <span>Data availability</span>
                </div>
                <p style={{ fontSize: '0.85rem', color: 'var(--kod-text-secondary)', lineHeight: 1.6, marginTop: '0.5rem' }}>
                  A connected handle does not guarantee live statistics. Source support and metric coverage vary by platform; unsupported values remain unavailable.
                </p>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'analytics' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1.5rem' }}>
              <div className="kod-dash-panel">
                <div className="kod-panel-title">
                  <BarChart3 size={16} color="var(--kod-accent-cyan)" />
                  <span>Algorithmic Difficulty Share</span>
                </div>
                <div className="kod-difficulty-bars" style={{ marginTop: '1rem' }}>
                  <div className="kod-diff-row kod-diff-easy">
                    <div className="kod-diff-header">
                      <span>Easy Algorithms</span>
                      <span>390 / 600 (65%)</span>
                    </div>
                    <div className="kod-diff-bar-track">
                      <div className="kod-diff-bar-fill" style={{ width: '65%' }} />
                    </div>
                  </div>
                  <div className="kod-diff-row kod-diff-medium">
                    <div className="kod-diff-header">
                      <span>Medium Algorithms</span>
                      <span>542 / 1,200 (45%)</span>
                    </div>
                    <div className="kod-diff-bar-track">
                      <div className="kod-diff-bar-fill" style={{ width: '45%' }} />
                    </div>
                  </div>
                  <div className="kod-diff-row kod-diff-hard">
                    <div className="kod-diff-header">
                      <span>Hard Algorithms</span>
                      <span>216 / 500 (43%)</span>
                    </div>
                    <div className="kod-diff-bar-track">
                      <div className="kod-diff-bar-fill" style={{ width: '43%' }} />
                    </div>
                  </div>
                </div>
              </div>

              <div className="kod-dash-panel">
                <div className="kod-panel-title">
                  <TrendingUp size={16} color="var(--kod-accent-violet-light)" />
                  <span>Solving Velocity Breakdown</span>
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', marginTop: '1rem' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ fontSize: '0.85rem', color: 'var(--kod-text-secondary)' }}>Weekly Average</span>
                    <span style={{ fontFamily: 'var(--font-mono)', fontWeight: 700, color: 'var(--kod-text-primary)' }}>18.4 Problems / wk</span>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ fontSize: '0.85rem', color: 'var(--kod-text-secondary)' }}>Monthly Peak</span>
                    <span style={{ fontFamily: 'var(--font-mono)', fontWeight: 700, color: 'var(--kod-accent-cyan)' }}>82 Problems</span>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ fontSize: '0.85rem', color: 'var(--kod-text-secondary)' }}>Acceptance Rate</span>
                    <span style={{ fontFamily: 'var(--font-mono)', fontWeight: 700, color: 'var(--kod-accent-emerald)' }}>68.4%</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'progress' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
            <div className="kod-dash-panel">
              <div className="kod-panel-header">
                <div className="kod-panel-title">
                  <TrendingUp size={16} color="var(--kod-accent-cyan)" />
                  <span>Illustrative Rating Trend</span>
                </div>
                <span className="kod-panel-tag">SAMPLE HISTORY PREVIEW</span>
              </div>

              <div className="kod-chart-container" style={{ height: '180px' }}>
                <svg viewBox="0 0 600 150" preserveAspectRatio="none">
                  <line x1="0" y1="30" x2="600" y2="30" stroke="var(--kod-border-hairline)" strokeDasharray="3 3" />
                  <line x1="0" y1="75" x2="600" y2="75" stroke="var(--kod-border-hairline)" strokeDasharray="3 3" />
                  <line x1="0" y1="120" x2="600" y2="120" stroke="var(--kod-border-hairline)" strokeDasharray="3 3" />

                  {/* LeetCode Rating curve */}
                  <path
                    d="M 10 130 C 100 115, 180 80, 260 70 S 400 50, 480 35 L 580 25"
                    fill="none"
                    stroke="var(--color-chart-1)"
                    strokeWidth="2.5"
                  />

                  {/* Codeforces curve */}
                  <path
                    d="M 10 140 C 90 135, 170 120, 250 100 S 390 90, 470 75 L 580 60"
                    fill="none"
                    stroke="var(--color-chart-2)"
                    strokeWidth="2"
                    strokeDasharray="4 2"
                  />

                  <circle cx="580" cy="25" r="4" fill="var(--kod-bg-card)" stroke="var(--color-chart-1)" strokeWidth="2" />
                  <circle cx="580" cy="60" r="4" fill="var(--kod-bg-card)" stroke="var(--color-chart-2)" strokeWidth="2" />
                </svg>
              </div>

              <div style={{ display: 'flex', gap: '1.5rem', justifyContent: 'center', fontSize: '0.8rem', color: 'var(--kod-text-secondary)' }}>
                <span style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                  <span style={{ width: '12px', height: '3px', background: 'var(--color-chart-1)', borderRadius: '2px' }} />
                  Sample LeetCode rating trend
                </span>
                <span style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                  <span style={{ width: '12px', height: '3px', background: 'var(--color-chart-2)', borderRadius: '2px' }} />
                  Sample Codeforces rating trend
                </span>
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1rem' }}>
              <div className="kod-dash-panel">
                <span className="kod-metric-label">Streak Record</span>
                <span className="kod-metric-number">94 Days</span>
                <span style={{ color: 'var(--kod-accent-amber)', fontSize: '0.8rem' }}>Unbroken consistency</span>
              </div>
              <div className="kod-dash-panel">
                <span className="kod-metric-label">First Observation</span>
                <span className="kod-metric-number">Jan 2024</span>
                <span style={{ color: 'var(--kod-text-muted)', fontSize: '0.8rem' }}>Sample reference date</span>
              </div>
              <div className="kod-dash-panel">
                <span className="kod-metric-label">Observed Growth</span>
                <span className="kod-metric-number">+342 Pts</span>
                <span style={{ color: 'var(--kod-accent-emerald)', fontSize: '0.8rem' }}>Illustrative sample trend</span>
              </div>
            </div>
          </div>
        )}
      </div>
    </section>
  );
};

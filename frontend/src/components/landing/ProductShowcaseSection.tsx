import React, { useState } from 'react';
import {
  UserCheck,
  BarChart3,
  TrendingUp,
  Layers,
  Award,
  CheckCircle2,
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
        {activeTab === 'profile' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem', borderBottom: '1px solid var(--kod-border-hairline)', paddingBottom: '1.5rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                <div style={{ width: '56px', height: '56px', borderRadius: '12px', background: 'linear-gradient(135deg, #0284C7, #6366F1)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 800, fontSize: '1.4rem' }}>
                  SG
                </div>
                <div>
                  <h3 style={{ fontSize: '1.25rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    Sajal Goyal <CheckCircle2 size={17} color="var(--kod-accent-cyan)" />
                  </h3>
                  <p style={{ color: 'var(--kod-text-secondary)', fontSize: '0.85rem' }}>
                    Competitive Programmer & Systems Architect · B.Tech CSE '25
                  </p>
                </div>
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                <span className="kod-dashboard-url-pill">
                  kodbtw.vercel.app/u/<span style={{ color: 'var(--kod-accent-cyan)' }}>sajalgoyal</span>
                </span>
                <span className="kod-dashboard-status-tag">VERIFIED</span>
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '1rem' }}>
              <div className="kod-dash-panel">
                <span className="kod-metric-label">Aggregated Solved</span>
                <span className="kod-metric-number">1,148</span>
                <span style={{ fontSize: '0.78rem', color: 'var(--kod-text-muted)' }}>Across 4 connected accounts</span>
              </div>
              <div className="kod-dash-panel">
                <span className="kod-metric-label">Peak Platform Rating</span>
                <span className="kod-metric-number">2,084</span>
                <span style={{ fontSize: '0.78rem', color: 'var(--kod-accent-cyan)' }}>LeetCode Guardian Level</span>
              </div>
              <div className="kod-dash-panel">
                <span className="kod-metric-label">Contests Logged</span>
                <span className="kod-metric-number">48</span>
                <span style={{ fontSize: '0.78rem', color: 'var(--kod-text-muted)' }}>Verified real observations</span>
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1rem' }}>
              <div className="kod-dash-panel">
                <div className="kod-panel-title">
                  <Layers size={16} color="var(--kod-accent-cyan)" />
                  <span>Connected Platforms Overview</span>
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', marginTop: '0.5rem' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', padding: '0.6rem 0.85rem', background: '#F8FAFC', border: '1px solid var(--kod-border-subtle)', borderRadius: '6px', fontSize: '0.85rem' }}>
                    <span style={{ fontWeight: 600 }}>LeetCode</span>
                    <b style={{ color: 'var(--kod-accent-cyan)' }}>680 Solved · 2,084 Rating</b>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', padding: '0.6rem 0.85rem', background: '#F8FAFC', border: '1px solid var(--kod-border-subtle)', borderRadius: '6px', fontSize: '0.85rem' }}>
                    <span style={{ fontWeight: 600 }}>Codeforces</span>
                    <b style={{ color: 'var(--kod-accent-violet)' }}>468 Solved · 1,742 Rating</b>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', padding: '0.6rem 0.85rem', background: '#F8FAFC', border: '1px solid var(--kod-border-subtle)', borderRadius: '6px', fontSize: '0.85rem' }}>
                    <span style={{ fontWeight: 600 }}>CodeChef</span>
                    <span style={{ color: 'var(--kod-text-muted)' }}>Account Connected · 4★</span>
                  </div>
                </div>
              </div>

              <div className="kod-dash-panel">
                <div className="kod-panel-title">
                  <Award size={16} color="var(--kod-accent-amber)" />
                  <span>Platform Integrity Guarantee</span>
                </div>
                <p style={{ fontSize: '0.85rem', color: 'var(--kod-text-secondary)', lineHeight: 1.6, marginTop: '0.5rem' }}>
                  KodBTW never fabricates mock numbers or synthetic leaderboards. Live statistics are pulled through real platform adapters, maintaining honest signal accuracy for recruiters and peers.
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
                  <span>Historical Rating Snapshots (LeetCode & Codeforces Combined)</span>
                </div>
                <span className="kod-panel-tag">PERSISTED HISTORY ONLY</span>
              </div>

              <div className="kod-chart-container" style={{ height: '180px' }}>
                <svg viewBox="0 0 600 150" preserveAspectRatio="none">
                  <line x1="0" y1="30" x2="600" y2="30" stroke="rgba(255,255,255,0.05)" strokeDasharray="3 3" />
                  <line x1="0" y1="75" x2="600" y2="75" stroke="rgba(255,255,255,0.05)" strokeDasharray="3 3" />
                  <line x1="0" y1="120" x2="600" y2="120" stroke="rgba(255,255,255,0.05)" strokeDasharray="3 3" />

                  {/* LeetCode Rating curve */}
                  <path
                    d="M 10 130 C 100 115, 180 80, 260 70 S 400 50, 480 35 L 580 25"
                    fill="none"
                    stroke="#38BDF8"
                    strokeWidth="2.5"
                  />

                  {/* Codeforces curve */}
                  <path
                    d="M 10 140 C 90 135, 170 120, 250 100 S 390 90, 470 75 L 580 60"
                    fill="none"
                    stroke="#818CF8"
                    strokeWidth="2"
                    strokeDasharray="4 2"
                  />

                  <circle cx="580" cy="25" r="4" fill="#38BDF8" stroke="#FFFFFF" strokeWidth="2" />
                  <circle cx="580" cy="60" r="4" fill="#818CF8" stroke="#FFFFFF" strokeWidth="2" />
                </svg>
              </div>

              <div style={{ display: 'flex', gap: '1.5rem', justifyContent: 'center', fontSize: '0.8rem', color: 'var(--kod-text-secondary)' }}>
                <span style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                  <span style={{ width: '12px', height: '3px', background: '#38BDF8', borderRadius: '2px' }} />
                  LeetCode Rating Trajectory (Max: 2,084)
                </span>
                <span style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                  <span style={{ width: '12px', height: '3px', background: '#818CF8', borderRadius: '2px' }} />
                  Codeforces Contest History (Max: 1,742)
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
                <span style={{ color: 'var(--kod-text-muted)', fontSize: '0.8rem' }}>Verified historical anchor</span>
              </div>
              <div className="kod-dash-panel">
                <span className="kod-metric-label">Observed Growth</span>
                <span className="kod-metric-number">+342 Pts</span>
                <span style={{ color: 'var(--kod-accent-emerald)', fontSize: '0.8rem' }}>Consistent upward progression</span>
              </div>
            </div>
          </div>
        )}
      </div>
    </section>
  );
};

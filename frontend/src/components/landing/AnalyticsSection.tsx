import React, { useState } from 'react';
import {
  TrendingUp,
  ShieldCheck,
  Activity,
  Layers,
} from 'lucide-react';

export const AnalyticsSection: React.FC = () => {
  const [activeMetric, setActiveMetric] = useState<'rating' | 'solved' | 'streak'>('rating');

  return (
    <section className="kod-section" id="analytics">
      <div className="kod-analytics-split">
        {/* Left Editorial Content */}
        <div className="kod-analytics-left">
          <span className="kod-section-eyebrow">DETERMINISTIC INTELLIGENCE</span>
          <h2 className="kod-section-title">Your activity, turned into insight.</h2>
          <p className="kod-section-subtitle">
            Most dashboards invent arbitrary aggregate numbers. KodBTW delivers honest,
            persisted telemetry derived directly from your official platform accounts.
          </p>

          <div className="kod-feature-points">
            <div className="kod-feature-point">
              <div className="kod-point-icon">
                <ShieldCheck size={18} />
              </div>
              <div className="kod-point-text">
                <h4>No Fabricated Observations</h4>
                <p>
                  Missing days and unverified platforms are acknowledged honestly. We store snapshots without inventing synthetic data points.
                </p>
              </div>
            </div>

            <div className="kod-feature-point">
              <div className="kod-point-icon">
                <Layers size={18} />
              </div>
              <div className="kod-point-text">
                <h4>Granular Difficulty Context</h4>
                <p>
                  Understand the true composition of your algorithmic problem solving across Easy, Medium, and Hard tiers with exact counts.
                </p>
              </div>
            </div>

            <div className="kod-feature-point">
              <div className="kod-point-icon">
                <TrendingUp size={18} />
              </div>
              <div className="kod-point-text">
                <h4>Real Rating Trajectories</h4>
                <p>
                  Separate tracks for LeetCode contests and Codeforces rounds so recruiters see true competitive programming competence.
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Right Interactive Analytics Terminal */}
        <div className="kod-analytics-terminal">
          <div className="kod-panel-header">
            <div className="kod-panel-title">
              <Activity size={18} color="var(--kod-accent-cyan)" />
              <span>Interactive Analytics Console</span>
            </div>
            <div style={{ display: 'flex', gap: '0.4rem' }}>
              <button
                type="button"
                onClick={() => setActiveMetric('rating')}
                className={`kod-platform-pill ${activeMetric === 'rating' ? 'live' : ''}`}
                style={{ cursor: 'pointer' }}
              >
                Rating
              </button>
              <button
                type="button"
                onClick={() => setActiveMetric('solved')}
                className={`kod-platform-pill ${activeMetric === 'solved' ? 'live' : ''}`}
                style={{ cursor: 'pointer' }}
              >
                Solved
              </button>
              <button
                type="button"
                onClick={() => setActiveMetric('streak')}
                className={`kod-platform-pill ${activeMetric === 'streak' ? 'live' : ''}`}
                style={{ cursor: 'pointer' }}
              >
                Streak
              </button>
            </div>
          </div>

          {/* Dynamic Graphic based on activeMetric */}
          {activeMetric === 'rating' && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
              <div className="kod-chart-container" style={{ height: '180px' }}>
                <svg viewBox="0 0 500 140" preserveAspectRatio="none">
                  <line x1="0" y1="20" x2="500" y2="20" stroke="rgba(255,255,255,0.05)" />
                  <line x1="0" y1="60" x2="500" y2="60" stroke="rgba(255,255,255,0.05)" />
                  <line x1="0" y1="100" x2="500" y2="100" stroke="rgba(255,255,255,0.05)" />

                  <path
                    d="M 10 120 Q 80 110, 140 90 T 260 70 T 380 40 T 490 25"
                    fill="none"
                    stroke="#38BDF8"
                    strokeWidth="3"
                  />
                  <circle cx="490" cy="25" r="5" fill="#38BDF8" stroke="#FFFFFF" strokeWidth="2" />
                </svg>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '0.75rem' }}>
                <div style={{ padding: '0.75rem', background: 'rgba(5,7,11,0.5)', borderRadius: '6px' }}>
                  <span style={{ fontSize: '0.7rem', color: 'var(--kod-text-muted)' }}>CURRENT</span>
                  <div style={{ fontSize: '1.25rem', fontWeight: 800, fontFamily: 'var(--font-mono)', color: 'var(--kod-accent-cyan)' }}>
                    2,185
                  </div>
                </div>
                <div style={{ padding: '0.75rem', background: 'rgba(5,7,11,0.5)', borderRadius: '6px' }}>
                  <span style={{ fontSize: '0.7rem', color: 'var(--kod-text-muted)' }}>TOP TIER</span>
                  <div style={{ fontSize: '1.25rem', fontWeight: 800, fontFamily: 'var(--font-mono)' }}>
                    Top 1.8%
                  </div>
                </div>
                <div style={{ padding: '0.75rem', background: 'rgba(5,7,11,0.5)', borderRadius: '6px' }}>
                  <span style={{ fontSize: '0.7rem', color: 'var(--kod-text-muted)' }}>CONTESTS</span>
                  <div style={{ fontSize: '1.25rem', fontWeight: 800, fontFamily: 'var(--font-mono)' }}>
                    54 Logged
                  </div>
                </div>
              </div>
            </div>
          )}

          {activeMetric === 'solved' && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
              <div className="kod-difficulty-bars">
                <div className="kod-diff-row kod-diff-easy">
                  <div className="kod-diff-header">
                    <span>Easy Category</span>
                    <span style={{ fontFamily: 'var(--font-mono)' }}>512 Solved</span>
                  </div>
                  <div className="kod-diff-bar-track">
                    <div className="kod-diff-bar-fill" style={{ width: '65%' }} />
                  </div>
                </div>
                <div className="kod-diff-row kod-diff-medium">
                  <div className="kod-diff-header">
                    <span>Medium Category</span>
                    <span style={{ fontFamily: 'var(--font-mono)' }}>728 Solved</span>
                  </div>
                  <div className="kod-diff-bar-track">
                    <div className="kod-diff-bar-fill" style={{ width: '55%' }} />
                  </div>
                </div>
                <div className="kod-diff-row kod-diff-hard">
                  <div className="kod-diff-header">
                    <span>Hard Category</span>
                    <span style={{ fontFamily: 'var(--font-mono)' }}>242 Solved</span>
                  </div>
                  <div className="kod-diff-bar-track">
                    <div className="kod-diff-bar-fill" style={{ width: '35%' }} />
                  </div>
                </div>
              </div>

              <div style={{ padding: '0.85rem', background: 'rgba(5,7,11,0.5)', borderRadius: '8px', fontSize: '0.8rem', color: 'var(--kod-text-secondary)' }}>
                Total Algorithmic Solves: <strong style={{ color: 'var(--kod-text-primary)' }}>1,482 Problems</strong> across connected LeetCode and Codeforces profiles.
              </div>
            </div>
          )}

          {activeMetric === 'streak' && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div style={{ padding: '1.25rem', background: 'rgba(5,7,11,0.5)', borderRadius: '8px', textAlign: 'center' }}>
                  <span style={{ fontSize: '0.75rem', color: 'var(--kod-text-muted)' }}>CURRENT STREAK</span>
                  <div style={{ fontSize: '2.5rem', fontWeight: 800, fontFamily: 'var(--font-mono)', color: 'var(--kod-accent-amber)' }}>
                    42d
                  </div>
                  <span style={{ fontSize: '0.75rem', color: 'var(--kod-text-secondary)' }}>Active daily momentum</span>
                </div>
                <div style={{ padding: '1.25rem', background: 'rgba(5,7,11,0.5)', borderRadius: '8px', textAlign: 'center' }}>
                  <span style={{ fontSize: '0.75rem', color: 'var(--kod-text-muted)' }}>LONGEST RECORD</span>
                  <div style={{ fontSize: '2.5rem', fontWeight: 800, fontFamily: 'var(--font-mono)', color: 'var(--kod-accent-cyan)' }}>
                    94d
                  </div>
                  <span style={{ fontSize: '0.75rem', color: 'var(--kod-text-secondary)' }}>Recorded in 2024</span>
                </div>
              </div>

              <p style={{ fontSize: '0.8rem', color: 'var(--kod-text-muted)', lineHeight: 1.5 }}>
                Snapshots evaluate consecutive active days without synthetic padding. If a day had zero recorded submissions, the streak resets cleanly according to actual platform logs.
              </p>
            </div>
          )}
        </div>
      </div>
    </section>
  );
};

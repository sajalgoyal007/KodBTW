import React, { useState } from 'react';
import {
  Layers,
  Activity,
  BarChart3,
  TrendingUp,
  BrainCircuit,
  CheckCircle2,
} from 'lucide-react';

export const CommandCenterSection: React.FC = () => {
  const [activeModule, setActiveModule] = useState<'profile' | 'activity' | 'analytics' | 'progress' | 'insights'>('profile');

  return (
    <section className="kod-section" id="command-center">
      <div className="kod-section-header">
        <span className="kod-section-eyebrow">PLATFORM ARCHITECTURE COMMAND</span>
        <h2 className="kod-section-title">An integrated command center for your code.</h2>
        <p className="kod-section-subtitle">
          Switch between telemetry modules to inspect how KodBTW unifies and computes your developer signals in real time.
        </p>
      </div>

      <div className="kod-command-canvas">
        {/* Navigation Tabs */}
        <div className="kod-command-nav" role="tablist">
          <button
            type="button"
            className={`kod-cmd-item ${activeModule === 'profile' ? 'active' : ''}`}
            onClick={() => setActiveModule('profile')}
          >
            <Layers size={15} />
            <span>01. PROFILE MODULE</span>
          </button>

          <button
            type="button"
            className={`kod-cmd-item ${activeModule === 'activity' ? 'active' : ''}`}
            onClick={() => setActiveModule('activity')}
          >
            <Activity size={15} />
            <span>02. ACTIVITY MODULE</span>
          </button>

          <button
            type="button"
            className={`kod-cmd-item ${activeModule === 'analytics' ? 'active' : ''}`}
            onClick={() => setActiveModule('analytics')}
          >
            <BarChart3 size={15} />
            <span>03. ANALYTICS MODULE</span>
          </button>

          <button
            type="button"
            className={`kod-cmd-item ${activeModule === 'progress' ? 'active' : ''}`}
            onClick={() => setActiveModule('progress')}
          >
            <TrendingUp size={15} />
            <span>04. PROGRESS MODULE</span>
          </button>

          <button
            type="button"
            className={`kod-cmd-item ${activeModule === 'insights' ? 'active' : ''}`}
            onClick={() => setActiveModule('insights')}
          >
            <BrainCircuit size={15} />
            <span>05. INSIGHTS MODULE</span>
          </button>
        </div>

        {/* Viewport for Active Module */}
        <div className="kod-command-view">
          {activeModule === 'profile' && (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1.5rem' }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                <span className="kod-section-eyebrow">IDENTITY PERSISTENCE</span>
                <h3 style={{ fontSize: '1.25rem', fontWeight: 700 }}>Unified Developer Entity</h3>
                <p style={{ color: 'var(--kod-text-secondary)', fontSize: '0.875rem', lineHeight: 1.6 }}>
                  Maps multiple remote handles (LeetCode, Codeforces, HackerRank) to an owner-scoped UUID in MySQL with verified credential tokens.
                </p>
                <div style={{ marginTop: 'auto', display: 'flex', gap: '0.5rem', fontFamily: 'var(--font-mono)', fontSize: '0.75rem', color: 'var(--kod-text-muted)' }}>
                  <span>SCHEMA: user_platforms</span> · <span>INDEX: unique_handle</span>
                </div>
              </div>

              <div style={{ background: 'rgba(5,7,11,0.6)', padding: '1.25rem', borderRadius: '8px', border: '1px solid var(--kod-border-hairline)', fontFamily: 'var(--font-mono)', fontSize: '0.78rem', color: 'var(--kod-text-secondary)' }}>
                <div style={{ color: 'var(--kod-accent-cyan)', marginBottom: '0.5rem' }}>// GET /api/public/profiles/alexrivera</div>
                <div>{'{'}</div>
                <div style={{ paddingLeft: '1rem' }}>"username": "alexrivera",</div>
                <div style={{ paddingLeft: '1rem' }}>"verified": true,</div>
                <div style={{ paddingLeft: '1rem' }}>"sources": ["LEETCODE", "CODEFORCES", "CODECHEF"],</div>
                <div style={{ paddingLeft: '1rem' }}>"totalSolved": 1482,</div>
                <div style={{ paddingLeft: '1rem' }}>"bestRating": 2185</div>
                <div>{'}'}</div>
              </div>
            </div>
          )}

          {activeModule === 'activity' && (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1.5rem' }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                <span className="kod-section-eyebrow">SUBMISSION CADENCE</span>
                <h3 style={{ fontSize: '1.25rem', fontWeight: 700 }}>Deterministic Activity Heatmap</h3>
                <p style={{ color: 'var(--kod-text-secondary)', fontSize: '0.875rem', lineHeight: 1.6 }}>
                  Aggregates timestamped submissions across every connected platform. Highlights daily problem solving streaks and consistency without inflating numbers.
                </p>
                <div style={{ marginTop: 'auto', display: 'flex', gap: '0.5rem', fontFamily: 'var(--font-mono)', fontSize: '0.75rem', color: 'var(--kod-accent-emerald)' }}>
                  <span>365-DAY AGGREGATION ACTIVE</span>
                </div>
              </div>

              <div style={{ background: 'rgba(5,7,11,0.6)', padding: '1.25rem', borderRadius: '8px', border: '1px solid var(--kod-border-hairline)', display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem' }}>
                  <span style={{ color: 'var(--kod-text-muted)' }}>Latest Synced Event</span>
                  <span style={{ color: 'var(--kod-accent-cyan)', fontFamily: 'var(--font-mono)' }}>14 mins ago</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem' }}>
                  <span style={{ color: 'var(--kod-text-muted)' }}>Active Streak Counter</span>
                  <span style={{ color: 'var(--kod-accent-amber)', fontFamily: 'var(--font-mono)', fontWeight: 700 }}>42 Days</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem' }}>
                  <span style={{ color: 'var(--kod-text-muted)' }}>Weekly Volume</span>
                  <span style={{ color: 'var(--kod-text-primary)', fontFamily: 'var(--font-mono)' }}>18 Problems Solved</span>
                </div>
              </div>
            </div>
          )}

          {activeModule === 'analytics' && (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1.5rem' }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                <span className="kod-section-eyebrow">COMPLEXITY ANALYSIS</span>
                <h3 style={{ fontSize: '1.25rem', fontWeight: 700 }}>Algorithmic Tier Distribution</h3>
                <p style={{ color: 'var(--kod-text-secondary)', fontSize: '0.875rem', lineHeight: 1.6 }}>
                  Evaluates the distribution of fundamental algorithmic problems vs advanced competitive programming topics like Segment Trees, Graphs, and Dynamic Programming.
                </p>
              </div>

              <div style={{ background: 'rgba(5,7,11,0.6)', padding: '1.25rem', borderRadius: '8px', border: '1px solid var(--kod-border-hairline)', display: 'flex', flexDirection: 'column', gap: '0.6rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.78rem' }}>
                  <span>Easy (Warmup & Foundational)</span>
                  <span style={{ color: 'var(--kod-accent-emerald)', fontFamily: 'var(--font-mono)' }}>512 (34.5%)</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.78rem' }}>
                  <span>Medium (Core Interview Level)</span>
                  <span style={{ color: 'var(--kod-accent-amber)', fontFamily: 'var(--font-mono)' }}>728 (49.1%)</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.78rem' }}>
                  <span>Hard (Advanced CP / Olympiad)</span>
                  <span style={{ color: 'var(--kod-accent-rose)', fontFamily: 'var(--font-mono)' }}>242 (16.4%)</span>
                </div>
              </div>
            </div>
          )}

          {activeModule === 'progress' && (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1.5rem' }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                <span className="kod-section-eyebrow">RATING CONVERGENCE</span>
                <h3 style={{ fontSize: '1.25rem', fontWeight: 700 }}>Contest Progression Curves</h3>
                <p style={{ color: 'var(--kod-text-secondary)', fontSize: '0.875rem', lineHeight: 1.6 }}>
                  Tracks historical rating snapshots chronologically. Never assumes platform equivalence — Codeforces ratings and LeetCode contest ranks are represented in their natural scales.
                </p>
              </div>

              <div style={{ background: 'rgba(5,7,11,0.6)', padding: '1.25rem', borderRadius: '8px', border: '1px solid var(--kod-border-hairline)', display: 'flex', flexDirection: 'column', gap: '0.6rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem' }}>
                  <span>LeetCode Biweekly/Weekly</span>
                  <span style={{ color: 'var(--kod-accent-cyan)', fontFamily: 'var(--font-mono)', fontWeight: 700 }}>2,185 Peak</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem' }}>
                  <span>Codeforces Div 1 / Div 2</span>
                  <span style={{ color: 'var(--kod-accent-violet-light)', fontFamily: 'var(--font-mono)', fontWeight: 700 }}>1,940 Peak</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem' }}>
                  <span>Historic Snapshots Saved</span>
                  <span style={{ color: 'var(--kod-text-primary)', fontFamily: 'var(--font-mono)' }}>128 Observations</span>
                </div>
              </div>
            </div>
          )}

          {activeModule === 'insights' && (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1.5rem' }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                <span className="kod-section-eyebrow">DETERMINISTIC EVALUATION</span>
                <h3 style={{ fontSize: '1.25rem', fontWeight: 700 }}>Automated Skill Insights</h3>
                <p style={{ color: 'var(--kod-text-secondary)', fontSize: '0.875rem', lineHeight: 1.6 }}>
                  Generates deterministic insight cards based on actual problem solving cadence, topic coverage, and contest consistency.
                </p>
              </div>

              <div style={{ background: 'rgba(5,7,11,0.6)', padding: '1.25rem', borderRadius: '8px', border: '1px solid var(--kod-border-hairline)', display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--kod-accent-emerald)', fontSize: '0.8rem' }}>
                  <CheckCircle2 size={16} />
                  <span>High Hard-Problem Ratio (16.4% vs 7.2% community avg)</span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--kod-accent-cyan)', fontSize: '0.8rem' }}>
                  <CheckCircle2 size={16} />
                  <span>Multi-platform verified (LeetCode & Codeforces live)</span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--kod-accent-violet-light)', fontSize: '0.8rem' }}>
                  <CheckCircle2 size={16} />
                  <span>Contest consistency score: Stable upward trajectory</span>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </section>
  );
};

import React, { useMemo } from 'react';
import {
  CheckCircle2,
  TrendingUp,
  Flame,
  Trophy,
  Activity,
  Layers,
  Terminal,
  ShieldCheck,
} from 'lucide-react';

interface HeroDashboardVisualProps {
  pointerOffset?: { x: number; y: number };
}

export const HeroDashboardVisual: React.FC<HeroDashboardVisualProps> = ({ pointerOffset }) => {

  // 3D perspective tilt calculated from pointer offset
  const tiltStyle = useMemo(() => {
    if (!pointerOffset) return {};
    const tiltX = (pointerOffset.y * -6).toFixed(2);
    const tiltY = (pointerOffset.x * 6).toFixed(2);
    return {
      transform: `rotateX(${tiltX}deg) rotateY(${tiltY}deg)`,
    };
  }, [pointerOffset]);

  // Generate 52 weeks x 7 days realistic deterministic heatmap data
  const heatmapData = useMemo(() => {
    const cells = [];
    for (let i = 0; i < 52 * 7; i++) {
      // Create a natural distribution of activity with occasional high streak days
      const rand = Math.sin(i * 0.3) * 0.5 + Math.cos(i * 0.1) * 0.5;
      let level = 0;
      if (rand > 0.6) level = 4;
      else if (rand > 0.3) level = 3;
      else if (rand > 0.0) level = 2;
      else if (rand > -0.3) level = 1;
      cells.push({ id: i, level, count: level * 3 });
    }
    return cells;
  }, []);

  return (
    <div className="kod-hero-visual-wrap">
      <div className="kod-hero-dashboard-frame" style={tiltStyle}>
        {/* Top Window Bar */}
        <div className="kod-dashboard-topbar">
          <div className="kod-window-controls">
            <span className="kod-window-dot close" />
            <span className="kod-window-dot min" />
            <span className="kod-window-dot max" />
          </div>

          <div className="kod-dashboard-url-pill">
            <span className="kod-url-domain">kodbtw.vercel.app/u/</span>
            <span className="kod-url-path">alexrivera</span>
          </div>

          <div className="kod-dashboard-status-tag">
            <span className="kod-pulse-dot" style={{ width: '6px', height: '6px' }} />
            <span>REAL-TIME SNAPSHOT</span>
          </div>
        </div>

        {/* Dashboard Body Content */}
        <div className="kod-dashboard-body">
          {/* Developer Profile Header Strip */}
          <div className="kod-dash-profile-row">
            <div className="kod-dash-dev-info">
              <div className="kod-dev-avatar">AR</div>
              <div className="kod-dev-names">
                <h3>
                  Alex Rivera
                  <CheckCircle2 size={16} className="kod-verified-check" />
                </h3>
                <p>@alexrivera · Staff Systems & Algorithm Engineer</p>
              </div>
            </div>

            <div className="kod-dash-platform-badges">
              <span className="kod-platform-pill live" title="Live sync enabled">
                <span className="kod-pulse-dot" style={{ width: '5px', height: '5px' }} />
                LeetCode · 2,185
              </span>
              <span className="kod-platform-pill live" title="Live sync enabled">
                <span className="kod-pulse-dot" style={{ width: '5px', height: '5px' }} />
                Codeforces · 1,940
              </span>
              <span className="kod-platform-pill" title="Connected source">
                CodeChef · 4★
              </span>
              <span className="kod-platform-pill" title="Connected source">
                HackerRank
              </span>
            </div>
          </div>

          {/* Metric Cards Grid */}
          <div className="kod-dash-metrics-grid">
            <div className="kod-dash-metric-card">
              <span className="kod-metric-label">Total Solved</span>
              <span className="kod-metric-number">1,482</span>
              <span className="kod-metric-subtext">
                <TrendingUp size={13} />
                +18 this week
              </span>
            </div>

            <div className="kod-dash-metric-card">
              <span className="kod-metric-label">Best Contest Rating</span>
              <span className="kod-metric-number">2,185</span>
              <span className="kod-metric-subtext">
                <Trophy size={13} />
                Knight (Top 1.8%)
              </span>
            </div>

            <div className="kod-dash-metric-card">
              <span className="kod-metric-label">Active Streak</span>
              <span className="kod-metric-number">42 Days</span>
              <span className="kod-metric-subtext" style={{ color: 'var(--kod-accent-amber)' }}>
                <Flame size={13} />
                Personal Best: 94d
              </span>
            </div>

            <div className="kod-dash-metric-card">
              <span className="kod-metric-label">Data Fidelity</span>
              <span className="kod-metric-number">100%</span>
              <span className="kod-metric-subtext" style={{ color: 'var(--kod-accent-emerald)' }}>
                <ShieldCheck size={13} />
                Deterministic snapshot
              </span>
            </div>
          </div>

          {/* Middle Split: Rating Trajectory & Difficulty Breakdown */}
          <div className="kod-dash-middle-split">
            {/* Rating Trajectory Chart */}
            <div className="kod-dash-panel">
              <div className="kod-panel-header">
                <div className="kod-panel-title">
                  <Activity size={16} color="var(--kod-accent-cyan)" />
                  <span>Contest Rating Progression</span>
                </div>
                <span className="kod-panel-tag">6-MONTH TRAJECTORY</span>
              </div>

              <div className="kod-chart-container">
                <svg viewBox="0 0 500 130" preserveAspectRatio="none">
                  <defs>
                    <linearGradient id="chartGradient" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%" stopColor="#0284C7" stopOpacity="0.2" />
                      <stop offset="100%" stopColor="#0284C7" stopOpacity="0.0" />
                    </linearGradient>
                  </defs>

                  {/* Horizontal Guide Lines */}
                  <line x1="0" y1="20" x2="500" y2="20" stroke="rgba(15, 23, 42, 0.08)" strokeDasharray="4 4" />
                  <line x1="0" y1="60" x2="500" y2="60" stroke="rgba(15, 23, 42, 0.08)" strokeDasharray="4 4" />
                  <line x1="0" y1="100" x2="500" y2="100" stroke="rgba(15, 23, 42, 0.08)" strokeDasharray="4 4" />

                  {/* Area fill */}
                  <path
                    d="M 10 110 Q 70 95, 120 85 T 220 70 T 320 40 T 420 30 T 490 20 L 490 120 L 10 120 Z"
                    fill="url(#chartGradient)"
                  />

                  {/* Rating Line */}
                  <path
                    d="M 10 110 Q 70 95, 120 85 T 220 70 T 320 40 T 420 30 T 490 20"
                    fill="none"
                    stroke="#0284C7"
                    strokeWidth="2.5"
                    strokeLinecap="round"
                  />

                  {/* Key milestone dots */}
                  <circle cx="120" cy="85" r="3.5" fill="#FFFFFF" stroke="#0284C7" strokeWidth="2" />
                  <circle cx="220" cy="70" r="3.5" fill="#FFFFFF" stroke="#0284C7" strokeWidth="2" />
                  <circle cx="320" cy="40" r="3.5" fill="#FFFFFF" stroke="#0284C7" strokeWidth="2" />
                  <circle cx="490" cy="20" r="4.5" fill="#0284C7" stroke="#FFFFFF" strokeWidth="2" />
                </svg>
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.72rem', color: 'var(--kod-text-muted)', fontFamily: 'var(--font-mono)' }}>
                <span>MAY: 1,820</span>
                <span>JUL: 1,940</span>
                <span>SEP: 2,050</span>
                <span style={{ color: 'var(--kod-accent-cyan)', fontWeight: 600 }}>CURRENT: 2,185</span>
              </div>
            </div>

            {/* Difficulty Breakdown */}
            <div className="kod-dash-panel">
              <div className="kod-panel-header">
                <div className="kod-panel-title">
                  <Layers size={16} color="var(--kod-accent-violet-light)" />
                  <span>Difficulty Breakdown</span>
                </div>
                <span className="kod-panel-tag">1,482 TOTAL</span>
              </div>

              <div className="kod-difficulty-bars">
                <div className="kod-diff-row kod-diff-easy">
                  <div className="kod-diff-header">
                    <span style={{ color: 'var(--kod-accent-emerald)', fontWeight: 600 }}>Easy</span>
                    <span style={{ fontFamily: 'var(--font-mono)', color: 'var(--kod-text-secondary)' }}>512 / 820 (62%)</span>
                  </div>
                  <div className="kod-diff-bar-track">
                    <div className="kod-diff-bar-fill" style={{ width: '62%' }} />
                  </div>
                </div>

                <div className="kod-diff-row kod-diff-medium">
                  <div className="kod-diff-header">
                    <span style={{ color: 'var(--kod-accent-amber)', fontWeight: 600 }}>Medium</span>
                    <span style={{ fontFamily: 'var(--font-mono)', color: 'var(--kod-text-secondary)' }}>728 / 1,740 (42%)</span>
                  </div>
                  <div className="kod-diff-bar-track">
                    <div className="kod-diff-bar-fill" style={{ width: '42%' }} />
                  </div>
                </div>

                <div className="kod-diff-row kod-diff-hard">
                  <div className="kod-diff-header">
                    <span style={{ color: 'var(--kod-accent-rose)', fontWeight: 600 }}>Hard</span>
                    <span style={{ fontFamily: 'var(--font-mono)', color: 'var(--kod-text-secondary)' }}>242 / 730 (33%)</span>
                  </div>
                  <div className="kod-diff-bar-track">
                    <div className="kod-diff-bar-fill" style={{ width: '33%' }} />
                  </div>
                </div>
              </div>

              <div style={{ marginTop: 'auto', paddingTop: '0.5rem', display: 'flex', alignItems: 'center', justifyContent: 'space-between', fontSize: '0.75rem', color: 'var(--kod-text-muted)' }}>
                <span>Verification Method</span>
                <span style={{ color: 'var(--kod-text-secondary)', fontFamily: 'var(--font-mono)' }}>SHA256 Profile Sync</span>
              </div>
            </div>
          </div>

          {/* Unified Contribution Heatmap */}
          <div className="kod-dash-panel" style={{ padding: '1rem 1.25rem' }}>
            <div className="kod-panel-header" style={{ marginBottom: '0.5rem' }}>
              <div className="kod-panel-title" style={{ fontSize: '0.8rem' }}>
                <Terminal size={14} color="var(--kod-accent-cyan)" />
                <span>Aggregated Activity Cadence (52 Weeks across connected platforms)</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '0.68rem', color: 'var(--kod-text-muted)' }}>
                <span>Less</span>
                <span className="kod-heat-cell" style={{ width: '8px', height: '8px' }} />
                <span className="kod-heat-cell c1" style={{ width: '8px', height: '8px' }} />
                <span className="kod-heat-cell c2" style={{ width: '8px', height: '8px' }} />
                <span className="kod-heat-cell c3" style={{ width: '8px', height: '8px' }} />
                <span className="kod-heat-cell c4" style={{ width: '8px', height: '8px' }} />
                <span>More</span>
              </div>
            </div>

            <div className="kod-heatmap-wrap">
              <div className="kod-heatmap-grid">
                {heatmapData.map((cell) => (
                  <div
                    key={cell.id}
                    className={`kod-heat-cell ${cell.level > 0 ? `c${cell.level}` : ''}`}
                    title={`Day ${cell.id}: ${cell.count} problems solved`}
                  />
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

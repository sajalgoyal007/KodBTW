import React from 'react';
import { ShieldCheck, Activity, BarChart2, Layers } from 'lucide-react';

export const TrustStrip: React.FC = () => {
  return (
    <div className="kod-trust-strip" aria-label="Product Capabilities and Verification">
      <div className="kod-trust-container">
        {/* Core Capabilities */}
        <div className="kod-trust-item">
          <Activity size={17} className="kod-trust-icon" />
          <span>Multi-Platform Coding Activity</span>
        </div>

        <div className="kod-trust-item">
          <Layers size={17} className="kod-trust-icon" />
          <span>Unified Developer Profile</span>
        </div>

        <div className="kod-trust-item">
          <BarChart2 size={17} className="kod-trust-icon" />
          <span>Difficulty & Trajectory Analytics</span>
        </div>

        <div className="kod-trust-item">
          <ShieldCheck size={17} className="kod-trust-icon" />
          <span>Deterministic Snapshot Data</span>
        </div>

        {/* Supported Platform Sources */}
        <div className="kod-trust-platforms">
          <span style={{ fontSize: '0.72rem', color: 'var(--kod-text-muted)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>
            Sources:
          </span>
          <span className="kod-trust-badge verified">LeetCode · Live</span>
          <span className="kod-trust-badge verified">Codeforces · Live</span>
          <span className="kod-trust-badge">CodeChef</span>
          <span className="kod-trust-badge">GeeksForGeeks</span>
          <span className="kod-trust-badge">HackerRank</span>
        </div>
      </div>
    </div>
  );
};

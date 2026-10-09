import React, { useState } from 'react';
import {
  CheckCircle2,
  Copy,
  Check,
} from 'lucide-react';

export const DeveloperIdentitySection: React.FC = () => {
  const [copied, setCopied] = useState(false);

  const handleCopy = () => {
    navigator.clipboard.writeText('https://kodbtw.vercel.app/u/alexrivera');
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <section className="kod-section" id="identity">
      <div className="kod-section-header center">
        <span className="kod-section-eyebrow">PROFESSIONAL REPUTATION LAYER</span>
        <h2 className="kod-section-title">A profile that represents more than a username.</h2>
        <p className="kod-section-subtitle">
          Present your authentic coding competence with an immutable, verified developer profile.
          Designed to be shared in resumes, portfolios, and hiring pipelines.
        </p>
      </div>

      <div className="kod-identity-showcase">
        <div className="kod-id-card-outer">
          {/* Card Top / Identity Block */}
          <div className="kod-id-top">
            <div className="kod-id-user-block">
              <div className="kod-id-avatar-lg">AR</div>
              <div className="kod-id-meta">
                <h3>
                  Alex Rivera
                  <CheckCircle2 size={20} color="var(--kod-accent-cyan)" />
                </h3>
                <p>Senior Infrastructure & Distributed Systems Engineer</p>
                <div className="kod-id-socials">
                  <span className="kod-id-social-pill">San Francisco, CA</span>
                  <span className="kod-id-social-pill">MIT '22</span>
                  <span className="kod-id-social-pill">github.com/arivera</span>
                </div>
              </div>
            </div>

            <div className="kod-id-actions">
              <button
                type="button"
                onClick={handleCopy}
                className="kod-btn-secondary"
                style={{ fontSize: '0.8rem', padding: '0.5rem 0.9rem' }}
              >
                {copied ? <Check size={14} color="var(--kod-accent-emerald)" /> : <Copy size={14} />}
                <span>{copied ? 'Link Copied!' : 'Copy Profile Link'}</span>
              </button>
            </div>
          </div>

          {/* Aggregated Real Metrics */}
          <div className="kod-id-stats-grid">
            <div className="kod-id-stat-box">
              <span className="kod-id-stat-num">1,482</span>
              <span className="kod-id-stat-lbl">Verified Problems Solved</span>
            </div>
            <div className="kod-id-stat-box">
              <span className="kod-id-stat-num" style={{ color: 'var(--kod-accent-cyan)' }}>
                2,185
              </span>
              <span className="kod-id-stat-lbl">Top Contest Rating</span>
            </div>
            <div className="kod-id-stat-box">
              <span className="kod-id-stat-num" style={{ color: 'var(--kod-accent-amber)' }}>
                42 Days
              </span>
              <span className="kod-id-stat-lbl">Active Daily Streak</span>
            </div>
            <div className="kod-id-stat-box">
              <span className="kod-id-stat-num" style={{ color: 'var(--kod-accent-emerald)' }}>
                Top 1.8%
              </span>
              <span className="kod-id-stat-lbl">Global Contest Standing</span>
            </div>
          </div>

          {/* Connected Handles Footer */}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem', paddingTop: '1.5rem', borderTop: '1px solid var(--kod-border-hairline)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', fontSize: '0.75rem', color: 'var(--kod-text-secondary)', fontFamily: 'var(--font-mono)' }}>
              <span>Verified Identity Sources:</span>
              <span className="kod-platform-pill live">leetcode/arivera</span>
              <span className="kod-platform-pill live">codeforces/arivera_cf</span>
            </div>

            <div style={{ fontSize: '0.72rem', color: 'var(--kod-text-muted)' }}>
              Public URL: <strong style={{ color: 'var(--kod-accent-cyan)' }}>kodbtw.vercel.app/u/alexrivera</strong>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

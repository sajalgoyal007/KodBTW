import React from 'react';

export const DeveloperIdentitySection: React.FC = () => {
  return (
    <section className="kod-section" id="identity">
      <div className="kod-section-header center">
        <span className="kod-section-eyebrow">ILLUSTRATIVE PROFILE PREVIEW · SAMPLE DATA</span>
        <h2 className="kod-section-title">A profile that represents more than a username.</h2>
        <p className="kod-section-subtitle">
          Preview a shareable profile layout. Handles are user-provided, and live statistics depend on each platform's supported data source.
        </p>
      </div>

      <div className="kod-identity-showcase">
        <div className="kod-id-card-outer">
          {/* Card Top / Identity Block */}
          <div className="kod-id-top">
            <div className="kod-id-user-block">
              <div className="kod-id-avatar-lg">SD</div>
              <div className="kod-id-meta">
                <h3>
                  Sample Developer
                </h3>
                <p>Example role and profile details</p>
                <div className="kod-id-socials">
                  <span className="kod-id-social-pill">Illustrative profile</span>
                  <span className="kod-id-social-pill">Sample data</span>
                </div>
              </div>
            </div>

            <div className="kod-id-actions"><span className="kod-platform-pill">SAMPLE PREVIEW</span></div>
          </div>

          {/* Illustrative metrics; not a real user profile. */}
          <div className="kod-id-stats-grid">
            <div className="kod-id-stat-box">
              <span className="kod-id-stat-num">1,482</span>
              <span className="kod-id-stat-lbl">Sample Problems Solved</span>
            </div>
            <div className="kod-id-stat-box">
              <span className="kod-id-stat-num" style={{ color: 'var(--kod-accent-cyan)' }}>
                2,185
              </span>
              <span className="kod-id-stat-lbl">Sample Contest Rating</span>
            </div>
            <div className="kod-id-stat-box">
              <span className="kod-id-stat-num" style={{ color: 'var(--kod-accent-amber)' }}>
                42 Days
              </span>
              <span className="kod-id-stat-lbl">Sample Streak</span>
            </div>
            <div className="kod-id-stat-box">
              <span className="kod-id-stat-num" style={{ color: 'var(--kod-accent-emerald)' }}>
                Top 1.8%
              </span>
              <span className="kod-id-stat-lbl">Sample Standing</span>
            </div>
          </div>

          {/* Connected Handles Footer */}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem', paddingTop: '1.5rem', borderTop: '1px solid var(--kod-border-hairline)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', fontSize: '0.75rem', color: 'var(--kod-text-secondary)', fontFamily: 'var(--font-mono)' }}>
              <span>Example platforms:</span>
              <span className="kod-platform-pill">LeetCode</span>
              <span className="kod-platform-pill">Codeforces</span>
            </div>

            <div style={{ fontSize: '0.72rem', color: 'var(--kod-text-muted)' }}>
              Share URL format: <strong style={{ color: 'var(--kod-accent-cyan)' }}>/u/:username</strong>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

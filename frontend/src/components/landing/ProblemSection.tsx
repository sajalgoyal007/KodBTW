import React, { useState } from 'react';
import { ArrowRight, Merge, Split } from 'lucide-react';

export const ProblemSection: React.FC = () => {
  const [isMerged, setIsMerged] = useState(true);

  return (
    <section className="kod-section" id="problem">
      <div className="kod-section-header">
        <span className="kod-section-eyebrow">THE FRAGMENTATION PROBLEM</span>
        <h2 className="kod-section-title">Your coding journey is fragmented.</h2>
        <p className="kod-section-subtitle">
          Different platforms capture different parts of your technical identity.
          KodBTW unifies your signals into one coherent experience with context for every metric.
        </p>
      </div>

      {/* Interactive Toggle to show Fragmented vs Merged State */}
      <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '1.25rem' }}>
        <button
          type="button"
          onClick={() => setIsMerged(!isMerged)}
          className="kod-btn-secondary"
          style={{ fontSize: '0.8rem', padding: '0.45rem 0.9rem' }}
        >
          {isMerged ? <Split size={14} /> : <Merge size={14} />}
          <span>{isMerged ? 'Show Fragmented View' : 'Show Unified Signal'}</span>
        </button>
      </div>

      {/* Problem Visual Grid */}
      <div className="kod-problem-visual-grid">
        {/* Fragment 1: LeetCode */}
        <div
          className="kod-fragment-card"
          style={{
            borderColor: isMerged ? 'rgba(56, 189, 248, 0.3)' : 'var(--kod-border-hairline)',
            transform: isMerged ? 'translateY(0)' : 'translateY(8px)',
          }}
        >
          <div className="kod-fragment-header">
            <span className="kod-fragment-source">
              <span style={{ color: '#FFA116', fontWeight: 700 }}>●</span> LeetCode
            </span>
            <span className={isMerged ? 'kod-logo-badge' : 'kod-fragment-isolated-badge'}>
              {isMerged ? 'UNIFIED' : 'ISOLATED'}
            </span>
          </div>
          <div>
            <div className="kod-fragment-stat">850 Solved</div>
            <p className="kod-fragment-desc">
              Data siloed inside LeetCode. No context of contest performance or algorithmic ratings from other systems.
            </p>
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--kod-text-muted)', fontFamily: 'var(--font-mono)' }}>
            STATUS: {isMerged ? 'Synced into KodBTW' : 'Isolated in silo'}
          </div>
        </div>

        {/* Fragment 2: Codeforces */}
        <div
          className="kod-fragment-card"
          style={{
            borderColor: isMerged ? 'rgba(99, 102, 241, 0.3)' : 'var(--kod-border-hairline)',
            transform: isMerged ? 'translateY(0)' : 'translateY(16px)',
          }}
        >
          <div className="kod-fragment-header">
            <span className="kod-fragment-source">
              <span style={{ color: '#318CE7', fontWeight: 700 }}>●</span> Codeforces
            </span>
            <span className={isMerged ? 'kod-logo-badge' : 'kod-fragment-isolated-badge'}>
              {isMerged ? 'UNIFIED' : 'ISOLATED'}
            </span>
          </div>
          <div>
            <div className="kod-fragment-stat">1,940 Rating</div>
            <p className="kod-fragment-desc">
              Competitive contest history tracked separately. Tough for recruiters or peers to locate or assess easily.
            </p>
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--kod-text-muted)', fontFamily: 'var(--font-mono)' }}>
            STATUS: {isMerged ? 'Live adapter active' : 'Separate handle'}
          </div>
        </div>

        {/* Fragment 3: Multiple Handles & Contests */}
        <div
          className="kod-fragment-card"
          style={{
            borderColor: isMerged ? 'rgba(16, 185, 129, 0.3)' : 'var(--kod-border-hairline)',
            transform: isMerged ? 'translateY(0)' : 'translateY(4px)',
          }}
        >
          <div className="kod-fragment-header">
            <span className="kod-fragment-source">
              <span style={{ color: '#10B981', fontWeight: 700 }}>●</span> Additional Handles
            </span>
            <span className={isMerged ? 'kod-logo-badge' : 'kod-fragment-isolated-badge'}>
              {isMerged ? 'UNIFIED' : 'ISOLATED'}
            </span>
          </div>
          <div>
            <div className="kod-fragment-stat">3 Other Sites</div>
            <p className="kod-fragment-desc">
              CodeChef, GFG, HackerRank links scattered across resumes, LinkedIn descriptions, and GitHub bios.
            </p>
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--kod-text-muted)', fontFamily: 'var(--font-mono)' }}>
            STATUS: {isMerged ? 'Aggregated to /u/username' : 'Scattered links'}
          </div>
        </div>
      </div>

      {/* Assembly Banner */}
      <div className="kod-fragment-merge-banner">
        <div className="kod-merge-content">
          <div className="kod-merge-icon">
            <Merge size={22} />
          </div>
          <div className="kod-merge-text">
            <h4>The Solution: KodBTW Unified Intelligence Layer</h4>
            <p>
              Connect your verified handles once. KodBTW fetches, reconciles, and displays your complete coding footprint in a single shareable profile.
            </p>
          </div>
        </div>

        <a
          href="#product"
          className="kod-btn-primary"
          style={{ padding: '0.65rem 1.35rem', fontSize: '0.85rem' }}
        >
          See The Unified Product <ArrowRight size={15} />
        </a>
      </div>
    </section>
  );
};

import React from 'react';
import { Link2, Cpu, LineChart, ArrowRight, ShieldCheck, Zap } from 'lucide-react';

export const HowItWorksSection: React.FC = () => {
  return (
    <section className="kod-section" id="how-it-works">
      <div className="kod-section-header center">
        <span className="kod-section-eyebrow">THE ARCHITECTURE</span>
        <h2 className="kod-section-title">From coding activity to professional signal.</h2>
        <p className="kod-section-subtitle">
          Three clear stages that bring scattered problem-solving records into one shareable developer profile.
        </p>
      </div>

      <div className="kod-how-steps">
        {/* Step 01 */}
        <div className="kod-how-step-card">
          <div className="kod-step-number">
            <span>01</span>
            <Link2 size={24} className="kod-step-icon" />
          </div>
          <h3 className="kod-step-title">Connect</h3>
          <p className="kod-step-desc">
            Add your coding platform handles once. KodBTW retrieves statistics from supported sources when available; adding a handle does not verify ownership.
          </p>
          <div style={{ marginTop: 'auto', paddingTop: '1rem', display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.75rem', color: 'var(--kod-accent-cyan)', fontFamily: 'var(--font-mono)' }}>
            <Zap size={13} />
            <span>5 Platform Accounts</span>
          </div>
        </div>

        {/* Step 02 */}
        <div className="kod-how-step-card">
          <div className="kod-step-number">
            <span>02</span>
            <Cpu size={24} className="kod-step-icon" />
          </div>
          <h3 className="kod-step-title">Unify</h3>
          <p className="kod-step-desc">
            Our Spring Boot backend synchronizes platform telemetry into deterministic, versioned snapshots in MySQL. We normalize difficulty rankings and deduplicate signals without altering reality.
          </p>
          <div style={{ marginTop: 'auto', paddingTop: '1rem', display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.75rem', color: 'var(--kod-accent-emerald)', fontFamily: 'var(--font-mono)' }}>
            <ShieldCheck size={13} />
            <span>Deterministic Snapshots</span>
          </div>
        </div>

        {/* Step 03 */}
        <div className="kod-how-step-card">
          <div className="kod-step-number">
            <span>03</span>
            <LineChart size={24} className="kod-step-icon" />
          </div>
          <h3 className="kod-step-title">Understand</h3>
          <p className="kod-step-desc">
            Explore granular difficulty splits, contest rating trajectories, and streak persistence. Share your public <code style={{ color: 'var(--kod-accent-cyan)' }}>/u/username</code> profile with engineering teams and recruiters.
          </p>
          <div style={{ marginTop: 'auto', paddingTop: '1rem', display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.75rem', color: 'var(--kod-accent-violet-light)', fontFamily: 'var(--font-mono)' }}>
            <ArrowRight size={13} />
            <span>Public Profile Ready</span>
          </div>
        </div>
      </div>
    </section>
  );
};

import React from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, CheckCircle2 } from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';

export const CtaSection: React.FC = () => {
  const { isAuthenticated } = useAuth();

  const handleScrollToTop = () => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <section className="kod-section" style={{ paddingTop: '2rem', paddingBottom: '7rem' }}>
      <div className="kod-final-cta-wrap">
        <div className="kod-final-cta-inner">
          <div className="kod-eyebrow-pill" style={{ marginBottom: '0.5rem' }}>
            <span className="kod-pulse-dot" />
            <span>INSTANT SETUP · FREE DEVELOPER PROFILE</span>
          </div>

          <h2 className="kod-final-title">
            Build a clearer picture of your coding journey.
          </h2>

          <p className="kod-final-subtitle">
            Connect your platforms in under two minutes. Start publishing your unified developer profile today.
          </p>

          <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', justifyContent: 'center', marginTop: '1rem' }}>
            <Link
              to={isAuthenticated ? '/dashboard' : '/register'}
              className="kod-btn-primary"
              style={{ padding: '0.85rem 2rem', fontSize: '1rem' }}
            >
              {isAuthenticated ? 'Open Dashboard' : 'Create Your Profile'}
              <ArrowRight size={18} />
            </Link>

            <button
              type="button"
              onClick={handleScrollToTop}
              className="kod-btn-secondary"
              style={{ padding: '0.85rem 1.75rem', fontSize: '1rem' }}
            >
              Back to Overview
            </button>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '1.5rem', marginTop: '1.5rem', fontSize: '0.8125rem', color: 'var(--kod-text-muted)', flexWrap: 'wrap', justifyContent: 'center' }}>
            <span style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
              <CheckCircle2 size={15} color="var(--kod-accent-emerald)" /> Free Public URL
            </span>
            <span style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
              <CheckCircle2 size={15} color="var(--kod-accent-emerald)" /> Deterministic Snapshots
            </span>
            <span style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
              <CheckCircle2 size={15} color="var(--kod-accent-emerald)" /> Zero Ads or Tracking
            </span>
          </div>
        </div>
      </div>
    </section>
  );
};

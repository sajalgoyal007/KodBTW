import React from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, ChevronDown, Terminal } from 'lucide-react';
import { HeroDashboardVisual } from './HeroDashboardVisual';
import { useAuth } from '../../hooks/useAuth';

interface HeroSectionProps {
  pointerOffset: { x: number; y: number };
}

export const HeroSection: React.FC<HeroSectionProps> = ({ pointerOffset }) => {
  const { isAuthenticated } = useAuth();

  const handleExploreClick = () => {
    const el = document.getElementById('problem');
    if (el) {
      el.scrollIntoView({ behavior: 'smooth' });
    }
  };

  return (
    <section className="kod-hero" id="hero">
      <div className="kod-hero-inner">
        {/* Eyebrow Pill */}
        <div className="kod-eyebrow-pill">
          <span className="kod-pulse-dot" />
          <span>UNIFIED CODING INTELLIGENCE</span>
        </div>

        {/* Confident Headline */}
        <h1 className="kod-hero-headline">
          <span className="kod-headline-gradient">Every code signal.</span>
          <br />
          <span className="kod-headline-accent">One professional profile.</span>
        </h1>

        {/* Crisp Subheadline */}
        <p className="kod-hero-subheadline">
          KodBTW brings your coding identity and analytics into one polished experience,
          helping developers understand their progress, activity and technical journey.
        </p>

        {/* Call to Actions */}
        <div className="kod-hero-cta-group">
          <Link
            to={isAuthenticated ? '/dashboard' : '/register'}
            className="kod-btn-primary"
            style={{ padding: '0.8rem 1.75rem', fontSize: '0.95rem' }}
          >
            {isAuthenticated ? 'Open Dashboard' : 'Create Your Profile'}
            <ArrowRight size={17} />
          </Link>

          <button
            type="button"
            className="kod-btn-secondary"
            onClick={handleExploreClick}
            style={{ padding: '0.8rem 1.75rem', fontSize: '0.95rem' }}
          >
            Explore the Platform
            <ChevronDown size={17} />
          </button>
        </div>

        {/* Supporting Microcopy */}
        <div className="kod-hero-microcopy">
          <Terminal size={14} color="var(--kod-accent-cyan)" />
          <span>Built for developers. Designed for progress.</span>
        </div>

        {/* Sophisticated Hero Dashboard Visual */}
        <HeroDashboardVisual pointerOffset={pointerOffset} />
      </div>
    </section>
  );
};

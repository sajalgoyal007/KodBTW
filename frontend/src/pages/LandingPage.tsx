import React, { useState, useEffect } from 'react';
import { LandingNavbar } from '../components/landing/LandingNavbar';
import { HeroSection } from '../components/landing/HeroSection';
import { TrustStrip } from '../components/landing/TrustStrip';
import { ProblemSection } from '../components/landing/ProblemSection';
import { ProductShowcaseSection } from '../components/landing/ProductShowcaseSection';
import { AnalyticsSection } from '../components/landing/AnalyticsSection';
import { HowItWorksSection } from '../components/landing/HowItWorksSection';
import { DeveloperIdentitySection } from '../components/landing/DeveloperIdentitySection';
import { CommandCenterSection } from '../components/landing/CommandCenterSection';
import { MetricsSection } from '../components/landing/MetricsSection';
import { UseCasesSection } from '../components/landing/UseCasesSection';
import { CtaSection } from '../components/landing/CtaSection';
import { LandingFooter } from '../components/landing/LandingFooter';
import '../styles/landing.css';

export const LandingPage: React.FC = () => {
  const [pointerOffset, setPointerOffset] = useState<{ x: number; y: number }>({ x: 0, y: 0 });
  const [spotlightPos, setSpotlightPos] = useState<{ x: number; y: number }>({ x: -1000, y: -1000 });
  const [isTouchDevice, setIsTouchDevice] = useState(false);

  useEffect(() => {
    // Check if device is coarse pointer (touch device)
    if (window.matchMedia && window.matchMedia('(pointer: coarse)').matches) {
      setIsTouchDevice(true);
      return;
    }

    const handleMouseMove = (e: MouseEvent) => {
      // Calculate normalized offset from center of viewport (-1 to 1)
      const centerX = window.innerWidth / 2;
      const centerY = window.innerHeight / 2;
      const normX = (e.clientX - centerX) / centerX;
      const normY = (e.clientY - centerY) / centerY;

      setPointerOffset({ x: normX, y: normY });
      setSpotlightPos({ x: e.clientX, y: e.clientY });
    };

    window.addEventListener('mousemove', handleMouseMove, { passive: true });
    return () => window.removeEventListener('mousemove', handleMouseMove);
  }, []);

  return (
    <div className="kod-landing">
      {/* Background Atmosphere & Technical Grid */}
      <div className="kod-atmosphere" aria-hidden="true">
        <div className="kod-grid-lines" />
        <div className="kod-glow-orb kod-glow-orb-primary" />
        <div className="kod-glow-orb kod-glow-orb-secondary" />
        <div className="kod-glow-orb kod-glow-orb-tertiary" />
        
        {/* Cursor spotlight (Desktop only) */}
        {!isTouchDevice && (
          <div
            className="kod-cursor-spotlight"
            style={{
              left: `${spotlightPos.x}px`,
              top: `${spotlightPos.y}px`,
              opacity: spotlightPos.x > 0 ? 1 : 0,
            }}
          />
        )}
      </div>

      {/* Main Content Flow */}
      <div className="kod-content">
        {/* 01. Premium Navigation */}
        <LandingNavbar />

        {/* 02. Hero Section with 3D Dashboard */}
        <HeroSection pointerOffset={pointerOffset} />

        {/* 03. Trust & Capability Strip */}
        <TrustStrip />

        {/* 04. Section 01: The Fragmentation Problem */}
        <ProblemSection />

        {/* 05. Section 02: Unified Product Showcase */}
        <ProductShowcaseSection />

        {/* 06. Section 03: Analytics */}
        <AnalyticsSection />

        {/* 07. Section 04: How It Works */}
        <HowItWorksSection />

        {/* 08. Section 05: Developer Identity */}
        <DeveloperIdentitySection />

        {/* 09. Section 06: Command Center Canvas */}
        <CommandCenterSection />

        {/* 10. Section 07: Genuine Metrics */}
        <MetricsSection />

        {/* 11. Section 08: Use Cases */}
        <UseCasesSection />

        {/* 12. Section 09: Final Call to Action */}
        <CtaSection />

        {/* 13. Minimal Enterprise Footer */}
        <LandingFooter />
      </div>
    </div>
  );
};

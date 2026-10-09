import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Code2, ArrowRight, Menu, X, ShieldCheck } from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';

export const LandingNavbar: React.FC = () => {
  const { isAuthenticated } = useAuth();
  const [isScrolled, setIsScrolled] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 24);
    };
    window.addEventListener('scroll', handleScroll, { passive: true });
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const scrollToSection = (id: string) => {
    setMobileMenuOpen(false);
    const element = document.getElementById(id);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
    }
  };

  return (
    <>
      <header className={`kod-nav ${isScrolled ? 'scrolled' : ''}`}>
        <div className="kod-nav-container">
          {/* Logo */}
          <Link to="/" className="kod-nav-logo" aria-label="KodBTW Home">
            <div className="kod-logo-icon">
              <Code2 size={20} />
            </div>
            <span>
              Kod<span style={{ color: 'var(--kod-accent-cyan)' }}>BTW</span>
            </span>
            <span className="kod-logo-badge">PRO</span>
          </Link>

          {/* Desktop Navigation Links */}
          <nav className="kod-nav-links" aria-label="Main Navigation">
            <button
              type="button"
              className="kod-nav-link"
              onClick={() => scrollToSection('product')}
            >
              Product
            </button>
            <button
              type="button"
              className="kod-nav-link"
              onClick={() => scrollToSection('analytics')}
            >
              Analytics
            </button>
            <button
              type="button"
              className="kod-nav-link"
              onClick={() => scrollToSection('how-it-works')}
            >
              How It Works
            </button>
            <button
              type="button"
              className="kod-nav-link"
              onClick={() => scrollToSection('identity')}
            >
              Developers
            </button>
          </nav>

          {/* Desktop CTA actions */}
          <div className="kod-nav-actions">
            {isAuthenticated ? (
              <Link to="/dashboard" className="kod-btn-primary">
                Open Dashboard <ArrowRight size={15} />
              </Link>
            ) : (
              <>
                <Link to="/login" className="kod-btn-ghost">
                  Sign In
                </Link>
                <Link to="/register" className="kod-btn-primary">
                  Create Profile <ArrowRight size={15} />
                </Link>
              </>
            )}
            <button
              type="button"
              className="kod-nav-mobile-toggle"
              onClick={() => setMobileMenuOpen(true)}
              aria-label="Open Navigation Menu"
            >
              <Menu size={22} />
            </button>
          </div>
        </div>
      </header>

      {/* Premium Fullscreen/Side Drawer Mobile Navigation */}
      <div className={`kod-mobile-drawer ${mobileMenuOpen ? 'open' : ''}`} role="dialog" aria-modal="true">
        <div className="kod-mobile-drawer-header">
          <Link to="/" className="kod-nav-logo" onClick={() => setMobileMenuOpen(false)}>
            <div className="kod-logo-icon">
              <Code2 size={20} />
            </div>
            <span>
              Kod<span style={{ color: 'var(--kod-accent-cyan)' }}>BTW</span>
            </span>
          </Link>
          <button
            type="button"
            className="kod-btn-ghost"
            onClick={() => setMobileMenuOpen(false)}
            aria-label="Close Navigation Menu"
            style={{ padding: '0.4rem' }}
          >
            <X size={24} />
          </button>
        </div>

        <nav className="kod-mobile-drawer-links">
          <button
            type="button"
            className="kod-mobile-drawer-link"
            style={{ textAlign: 'left', background: 'none', border: 'none', borderBottom: '1px solid var(--kod-border-hairline)' }}
            onClick={() => scrollToSection('product')}
          >
            Product
          </button>
          <button
            type="button"
            className="kod-mobile-drawer-link"
            style={{ textAlign: 'left', background: 'none', border: 'none', borderBottom: '1px solid var(--kod-border-hairline)' }}
            onClick={() => scrollToSection('analytics')}
          >
            Analytics
          </button>
          <button
            type="button"
            className="kod-mobile-drawer-link"
            style={{ textAlign: 'left', background: 'none', border: 'none', borderBottom: '1px solid var(--kod-border-hairline)' }}
            onClick={() => scrollToSection('how-it-works')}
          >
            How It Works
          </button>
          <button
            type="button"
            className="kod-mobile-drawer-link"
            style={{ textAlign: 'left', background: 'none', border: 'none', borderBottom: '1px solid var(--kod-border-hairline)' }}
            onClick={() => scrollToSection('identity')}
          >
            Developers & Identity
          </button>
        </nav>

        <div className="kod-mobile-drawer-actions">
          {isAuthenticated ? (
            <Link
              to="/dashboard"
              className="kod-btn-primary"
              onClick={() => setMobileMenuOpen(false)}
              style={{ justifyContent: 'center' }}
            >
              Open Dashboard <ArrowRight size={16} />
            </Link>
          ) : (
            <>
              <Link
                to="/register"
                className="kod-btn-primary"
                onClick={() => setMobileMenuOpen(false)}
                style={{ justifyContent: 'center' }}
              >
                Create Your Profile <ArrowRight size={16} />
              </Link>
              <Link
                to="/login"
                className="kod-btn-secondary"
                onClick={() => setMobileMenuOpen(false)}
                style={{ justifyContent: 'center' }}
              >
                Sign In
              </Link>
            </>
          )}
        </div>

        <div style={{ marginTop: 'auto', paddingTop: '2rem', display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--kod-text-muted)', fontSize: '0.8rem' }}>
          <ShieldCheck size={16} color="var(--kod-accent-cyan)" />
          <span>Unified Coding Intelligence · Production Ready</span>
        </div>
      </div>
    </>
  );
};

import React from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, Code2, Terminal, Zap, Activity, BarChart3, CircleCheck } from 'lucide-react';
import { useAuth } from '../hooks/useAuth';

export const LandingPage: React.FC = () => {
  const { isAuthenticated } = useAuth();

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      {/* Navigation */}
      <header style={{
        borderBottom: '1px solid var(--color-border-subtle)',
        backgroundColor: 'var(--color-bg-card)',
        backdropFilter: 'blur(8px)',
        position: 'sticky',
        top: 0,
        zIndex: 50,
      }}>
        <div style={{
          maxWidth: '1200px',
          margin: '0 auto',
          padding: '1rem 1.5rem',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
        }}>
          <Link to="/" style={{ display: 'flex', alignItems: 'center', gap: '0.625rem' }}>
            <div style={{
              width: '32px',
              height: '32px',
              borderRadius: '6px',
              backgroundColor: 'var(--color-primary)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'var(--color-primary-text)',
              fontWeight: 800,
            }}>
              <Code2 size={20} />
            </div>
            <span style={{ fontWeight: 700, fontSize: '1.25rem', letterSpacing: '-0.02em' }}>
              Kod<span style={{ color: 'var(--color-primary)' }}>BTW</span>
            </span>
          </Link>

          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            {isAuthenticated ? (
              <Link to="/dashboard" className="btn btn-primary">
                Open Dashboard <ArrowRight size={16} />
              </Link>
            ) : (
              <>
                <Link to="/login" className="btn btn-ghost">
                  Sign In
                </Link>
                <Link to="/register" className="btn btn-primary">
                  Create Account
                </Link>
              </>
            )}
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <main style={{
        flex: 1,
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
        textAlign: 'center',
        padding: '5rem 1.5rem',
        maxWidth: '1180px',
        width: '100%',
        margin: '0 auto',
      }}>
        <div className="landing-hero">
          <div className="badge badge-primary landing-kicker"><Zap size={14} /> CODING PROFILE & ANALYTICS</div>

        <h1 style={{
          fontSize: 'clamp(2.4rem, 6vw, 4rem)',
          fontWeight: 700,
          lineHeight: 1.15,
          letterSpacing: '-0.03em',
          marginBottom: '1.5rem',
        }}>
          Your coding work, <br /><span style={{ color: 'var(--color-primary)' }}>in one clear view.</span>
        </h1>

        <p style={{
          fontSize: '1.25rem',
          color: 'var(--color-text-secondary)',
          lineHeight: 1.6,
          marginBottom: '2.5rem',
          maxWidth: '680px',
        }}>
          KodBTW brings your coding profiles and available platform statistics together, with clear context for every metric.
        </p>

        <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', justifyContent: 'center', marginBottom: '3rem' }}>
          <Link to={isAuthenticated ? '/dashboard' : '/register'} className="btn btn-primary" style={{ padding: '0.75rem 1.5rem', fontSize: '0.94rem' }}>
            {isAuthenticated ? 'Open Dashboard' : 'Create your profile'} <ArrowRight size={17} />
          </Link>
          {!isAuthenticated && <Link to="/login" className="btn btn-secondary" style={{ padding: '0.75rem 1.5rem', fontSize: '0.94rem' }}>Sign In</Link>}
        </div>

        <section className="coverage-section" aria-labelledby="coverage-title">
          <div className="section-heading"><div><span className="eyebrow">PLATFORM COVERAGE</span><h2 id="coverage-title">Clear about what is live</h2></div><p>Connections and statistics are shown separately.</p></div>
          <div className="coverage-grid">
            <div className="coverage-group"><h3><CircleCheck size={16} /> Live statistics</h3><div className="platform-labels"><span>LeetCode</span><span>Codeforces</span></div></div>
            <div className="coverage-group pending"><h3><Terminal size={16} /> Connected · stats pending</h3><div className="platform-labels"><span>CodeChef</span><span>GeeksForGeeks</span><span>HackerRank</span></div><p>Profiles can be connected; live statistics are not currently available.</p></div>
          </div>
        </section>
        <section className="landing-preview" aria-label="Product capabilities">
          <div><div className="preview-icon"><Activity size={20} /></div><span className="eyebrow">YOUR WORK, ORGANIZED</span><h2>One profile.<br/>Useful context.</h2><p>Review platform-specific ratings, solved counts and history without implying that different platforms measure performance the same way.</p></div>
          <div className="preview-panel"><div className="preview-panel-heading"><span><BarChart3 size={16}/> Profile overview</span><span className="preview-status">DATA FROM CONNECTED SOURCES</span></div><div className="preview-stat-row"><div><span>Platforms</span><b>5 connected</b></div><div><span>Live statistic sources</span><b>2 available</b></div></div><div className="preview-lines"><i/><i/><i/></div></div>
        </section>
        </div>
      </main>

      {/* Footer */}
      <footer style={{
        borderTop: '1px solid var(--color-border-subtle)',
        padding: '2rem 1.5rem',
        textAlign: 'center',
        color: 'var(--color-text-muted)',
        fontSize: '0.875rem',
      }}>
        <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
          KodBTW &copy; {new Date().getFullYear()} · A clear home for your coding profiles.
        </div>
      </footer>
    </div>
  );
};

import React from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, Code2, Terminal, ShieldCheck, Zap } from 'lucide-react';
import { useAuth } from '../hooks/useAuth';

export const LandingPage: React.FC = () => {
  const { isAuthenticated } = useAuth();

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      {/* Navigation */}
      <header style={{
        borderBottom: '1px solid var(--color-border-subtle)',
        backgroundColor: 'rgba(11, 19, 38, 0.8)',
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
              color: '#0b0f19',
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
        maxWidth: '900px',
        margin: '0 auto',
      }}>
        <div className="badge badge-primary" style={{ marginBottom: '1.5rem' }}>
          <Zap size={14} /> Unified Developer Telemetry
        </div>

        <h1 style={{
          fontSize: '3.5rem',
          fontWeight: 700,
          lineHeight: 1.15,
          letterSpacing: '-0.03em',
          marginBottom: '1.5rem',
        }}>
          Your Coding Identity, <br />
          <span style={{ color: 'var(--color-primary)' }}>Unified.</span>
        </h1>

        <p style={{
          fontSize: '1.25rem',
          color: 'var(--color-text-secondary)',
          lineHeight: 1.6,
          marginBottom: '2.5rem',
          maxWidth: '680px',
        }}>
          Aggregate your competitive programming performance across LeetCode, Codeforces, CodeChef, and more. One cohesive developer dashboard for ratings, problem solving counts, and streaks.
        </p>

        <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', justifyContent: 'center', marginBottom: '3.5rem' }}>
          <Link to="/register" className="btn btn-primary" style={{ padding: '0.75rem 2rem', fontSize: '1rem' }}>
            Get Started Free <ArrowRight size={18} />
          </Link>
          <Link to="/login" className="btn btn-secondary" style={{ padding: '0.75rem 2rem', fontSize: '1rem' }}>
            Sign In
          </Link>
        </div>

        {/* Feature Highlights Grid */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
          gap: '1.25rem',
          width: '100%',
          textAlign: 'left',
        }}>
          <div className="card">
            <div style={{ color: 'var(--color-primary)', marginBottom: '0.75rem' }}>
              <Terminal size={24} />
            </div>
            <h3 style={{ fontSize: '1rem', fontWeight: 600, marginBottom: '0.375rem' }}>Multi-Platform Aggregation</h3>
            <p style={{ fontSize: '0.875rem', color: 'var(--color-text-secondary)' }}>
              Link your existing coding profiles in seconds and visualize unified metrics in one place.
            </p>
          </div>

          <div className="card">
            <div style={{ color: 'var(--color-success)', marginBottom: '0.75rem' }}>
              <ShieldCheck size={24} />
            </div>
            <h3 style={{ fontSize: '1rem', fontWeight: 600, marginBottom: '0.375rem' }}>Verified Live Stats</h3>
            <p style={{ fontSize: '0.875rem', color: 'var(--color-text-secondary)' }}>
              Direct integration with official platform APIs ensures your stats and ratings reflect your real-time standing.
            </p>
          </div>

          <div className="card">
            <div style={{ color: 'var(--color-info)', marginBottom: '0.75rem' }}>
              <Code2 size={24} />
            </div>
            <h3 style={{ fontSize: '1rem', fontWeight: 600, marginBottom: '0.375rem' }}>Developer-First UI</h3>
            <p style={{ fontSize: '0.875rem', color: 'var(--color-text-secondary)' }}>
              Engineered with clean dark theme aesthetics, monospace metrics, and distraction-free telemetry.
            </p>
          </div>
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
          KodBTW &copy; {new Date().getFullYear()} — Built for competitive programmers and engineers.
        </div>
      </footer>
    </div>
  );
};

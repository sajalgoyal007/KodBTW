import React from 'react';
import { Link } from 'react-router-dom';
import { Code2, Github } from 'lucide-react';

export const LandingFooter: React.FC = () => {
  return (
    <footer className="kod-footer">
      <div className="kod-footer-container">
        <div className="kod-footer-top">
          {/* Brand Column */}
          <div className="kod-footer-brand">
            <Link to="/" className="kod-nav-logo">
              <div className="kod-logo-icon">
                <Code2 size={20} />
              </div>
              <span>
                Kod<span style={{ color: 'var(--kod-accent-cyan)' }}>BTW</span>
              </span>
            </Link>
            <p>
              Unified developer intelligence and coding analytics. Bringing multi-platform competitive programming and problem-solving records into one authoritative profile.
            </p>
            <div className="kod-footer-status-pill">
              <span className="kod-pulse-dot" style={{ width: '6px', height: '6px' }} />
              <span>All Systems Operational · v0.1.0</span>
            </div>
          </div>

          {/* Column 1: Product */}
          <div className="kod-footer-col">
            <h4>Product</h4>
            <ul className="kod-footer-links">
              <li>
                <a href="#product" className="kod-footer-link">Unified Profile</a>
              </li>
              <li>
                <a href="#analytics" className="kod-footer-link">Analytics Engine</a>
              </li>
              <li>
                <a href="#identity" className="kod-footer-link">Developer Identity</a>
              </li>
              <li>
                <a href="#how-it-works" className="kod-footer-link">How It Works</a>
              </li>
            </ul>
          </div>

          {/* Column 2: Platforms */}
          <div className="kod-footer-col">
            <h4>Platforms</h4>
            <ul className="kod-footer-links">
              <li>
                <span className="kod-footer-link" style={{ cursor: 'default' }}>LeetCode (Live)</span>
              </li>
              <li>
                <span className="kod-footer-link" style={{ cursor: 'default' }}>Codeforces (Live)</span>
              </li>
              <li>
                <span className="kod-footer-link" style={{ cursor: 'default' }}>CodeChef</span>
              </li>
              <li>
                <span className="kod-footer-link" style={{ cursor: 'default' }}>GeeksForGeeks</span>
              </li>
              <li>
                <span className="kod-footer-link" style={{ cursor: 'default' }}>HackerRank</span>
              </li>
            </ul>
          </div>

          {/* Column 3: Resources */}
          <div className="kod-footer-col">
            <h4>Resources</h4>
            <ul className="kod-footer-links">
              <li>
                <a href="https://github.com/sajalgoyal007/KodBTW" target="_blank" rel="noreferrer" className="kod-footer-link" style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                  <Github size={14} /> GitHub Repository
                </a>
              </li>
              <li>
                <Link to="/register" className="kod-footer-link">Create Account</Link>
              </li>
              <li>
                <Link to="/login" className="kod-footer-link">Sign In</Link>
              </li>
            </ul>
          </div>

          {/* Column 4: Legal & Architecture */}
          <div className="kod-footer-col">
            <h4>Architecture</h4>
            <ul className="kod-footer-links">
              <li>
                <span className="kod-footer-link" style={{ cursor: 'default' }}>Spring Boot 3.3 & React 18</span>
              </li>
              <li>
                <span className="kod-footer-link" style={{ cursor: 'default' }}>Flyway Versioned Schema</span>
              </li>
              <li>
                <span className="kod-footer-link" style={{ cursor: 'default' }}>Deterministic Snapshots</span>
              </li>
              <li>
                <span className="kod-footer-link" style={{ cursor: 'default' }}>JWT Bearer Security</span>
              </li>
            </ul>
          </div>
        </div>

        {/* Footer Bottom */}
        <div className="kod-footer-bottom">
          <div>
            &copy; {new Date().getFullYear()} KodBTW. Unified Coding Profile & Analytics. All rights reserved.
          </div>
          <div style={{ display: 'flex', gap: '1.5rem', alignItems: 'center' }}>
            <span>Built for developers. Designed for progress.</span>
          </div>
        </div>
      </div>
    </footer>
  );
};

import React from 'react';
import { GraduationCap, Briefcase, Code, Users, ArrowRight } from 'lucide-react';
import { Link } from 'react-router-dom';

export const UseCasesSection: React.FC = () => {
  return (
    <section className="kod-section" id="use-cases">
      <div className="kod-section-header">
        <span className="kod-section-eyebrow">TARGET AUDIENCES</span>
        <h2 className="kod-section-title">Built for different stages of a developer's journey.</h2>
        <p className="kod-section-subtitle">
          Whether you're compiling proof for campus placement or proving competitive programming depth to hiring committees.
        </p>
      </div>

      <div className="kod-usecases-grid">
        {/* Card 1: Students (Featured) */}
        <div className="kod-usecase-card featured">
          <div className="kod-usecase-pill">
            <GraduationCap size={15} />
            <span>Students & Early Career</span>
          </div>
          <h3 className="kod-usecase-title">Build a visible, verifiable technical journey.</h3>
          <p className="kod-usecase-desc">
            Campus placements often ask for multiple platform links. KodBTW gives you a unified public profile with college credentials, graduation year, and verified problem counts to share with campus placement drives.
          </p>
          <div style={{ marginTop: 'auto', display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.8rem', color: 'var(--kod-accent-cyan)', fontWeight: 600 }}>
            <span>Verified graduation & college attribution</span>
          </div>
        </div>

        {/* Card 2: Job Seekers */}
        <div className="kod-usecase-card">
          <div className="kod-usecase-pill">
            <Briefcase size={15} />
            <span>Job Seekers</span>
          </div>
          <h3 className="kod-usecase-title">Replace static resume claims with live proof.</h3>
          <p className="kod-usecase-desc">
            Instead of writing "Solved 500+ LeetCode problems" on a PDF, provide a verifiable <code style={{ color: 'var(--kod-accent-cyan)' }}>/u/username</code> link with difficulty breakdowns and rating curves recruiters can verify in seconds.
          </p>
        </div>

        {/* Card 3: Active Developers */}
        <div className="kod-usecase-card">
          <div className="kod-usecase-pill">
            <Code size={15} />
            <span>Active Developers</span>
          </div>
          <h3 className="kod-usecase-title">Monitor coding patterns and progress in one place.</h3>
          <p className="kod-usecase-desc">
            Keep an eye on personal consistency across LeetCode dailies and Codeforces rounds. Track your personal best streaks and algorithmic topic breadth without jumping between portals.
          </p>
        </div>

        {/* Card 4: Developer Communities (Full-width) */}
        <div className="kod-usecase-card fullwidth">
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', maxWidth: '750px' }}>
            <div className="kod-usecase-pill">
              <Users size={15} />
              <span>Developer Communities & Clubs</span>
            </div>
            <h3 className="kod-usecase-title">A structured, honest standard for coding cohorts.</h3>
            <p className="kod-usecase-desc">
              Campus coding clubs, bootcamp cohorts, and developer communities can inspect member progress objectively using synchronized snapshots without manual screenshot verification.
            </p>
          </div>

          <Link
            to="/register"
            className="kod-btn-primary"
            style={{ padding: '0.75rem 1.5rem', whiteSpace: 'nowrap' }}
          >
            Create Developer Profile <ArrowRight size={16} />
          </Link>
        </div>
      </div>
    </section>
  );
};

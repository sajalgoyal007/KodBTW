import React from 'react';
import { useAuth } from '../hooks/useAuth';
import { LogOut, Code2, User as UserIcon } from 'lucide-react';

export const DashboardPlaceholderPage: React.FC = () => {
  const { user, logout } = useAuth();

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      {/* Top Header */}
      <header style={{
        borderBottom: '1px solid var(--color-border-subtle)',
        backgroundColor: 'var(--color-bg-card)',
        padding: '1rem 2rem',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.625rem' }}>
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
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '1.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--color-text-secondary)', fontSize: '0.875rem' }}>
            <UserIcon size={16} />
            <span>{user?.name} ({user?.email})</span>
          </div>

          <button onClick={logout} className="btn btn-secondary" style={{ padding: '0.5rem 1rem', fontSize: '0.875rem' }}>
            <LogOut size={16} /> Sign Out
          </button>
        </div>
      </header>

      {/* Main Content Area */}
      <main style={{
        flex: 1,
        padding: '3rem 2rem',
        maxWidth: '800px',
        margin: '0 auto',
        width: '100%',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
      }}>
        <div className="card" style={{ textAlign: 'center', padding: '3rem 2rem' }}>
          <div className="badge badge-success" style={{ marginBottom: '1.5rem' }}>
            Authentication Successful
          </div>
          <h1 style={{ fontSize: '2rem', fontWeight: 700, marginBottom: '1rem' }}>
            Welcome, {user?.name}!
          </h1>
          <p style={{ color: 'var(--color-text-secondary)', fontSize: '1.0625rem', lineHeight: 1.6, marginBottom: '2rem' }}>
            You are successfully authenticated via Spring Boot JWT. Your user session is hydrated from <code className="mono" style={{ color: 'var(--color-primary)' }}>GET /api/auth/me</code>.
          </p>
          <div style={{
            backgroundColor: 'var(--color-bg-base)',
            border: '1px solid var(--color-border-subtle)',
            borderRadius: 'var(--radius-md)',
            padding: '1.25rem',
            textAlign: 'left',
            fontFamily: 'var(--font-mono)',
            fontSize: '0.875rem',
          }}>
            <div style={{ color: 'var(--color-text-muted)', marginBottom: '0.5rem' }}>// Authenticated Principal</div>
            <div>User ID: <span style={{ color: 'var(--color-primary)' }}>{user?.id}</span></div>
            <div>Name: <span style={{ color: 'var(--color-text-primary)' }}>{user?.name}</span></div>
            <div>Email: <span style={{ color: 'var(--color-text-primary)' }}>{user?.email}</span></div>
          </div>
        </div>
      </main>
    </div>
  );
};

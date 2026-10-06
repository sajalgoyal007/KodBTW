import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { Code2, LogOut, User as UserIcon, LayoutDashboard, BarChart2, Terminal, UserCircle } from 'lucide-react';

export const AppNavbar: React.FC = () => {
  const { user, logout } = useAuth();
  const location = useLocation();

  const navItems = [
    { label: 'Dashboard', path: '/dashboard', icon: <LayoutDashboard size={16} /> },
    { label: 'Analytics', path: '/analytics', icon: <BarChart2 size={16} /> },
    { label: 'Coding Profiles', path: '/coding-profiles', icon: <Terminal size={16} /> },
    { label: 'Developer Profile', path: '/profile', icon: <UserCircle size={16} /> },
  ];

  return (
    <header
      style={{
        borderBottom: '1px solid var(--color-border-subtle)',
        backgroundColor: 'var(--color-bg-card)',
        padding: '0.875rem 1.5rem',
        position: 'sticky',
        top: 0,
        zIndex: 20,
        backdropFilter: 'blur(8px)',
      }}
    >
      <div
        style={{
          maxWidth: '1280px',
          margin: '0 auto',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          flexWrap: 'wrap',
          gap: '1rem',
        }}
      >
        {/* Brand & Main Nav Links */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '2rem' }}>
          <Link to="/dashboard" style={{ display: 'flex', alignItems: 'center', gap: '0.625rem' }}>
            <div
              style={{
                width: '32px',
                height: '32px',
                borderRadius: 'var(--radius-md)',
                backgroundColor: 'var(--color-primary)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#0b0f19',
                fontWeight: 800,
              }}
            >
              <Code2 size={20} />
            </div>
            <span style={{ fontWeight: 800, fontSize: '1.25rem', letterSpacing: '-0.02em' }}>
              Kod<span style={{ color: 'var(--color-primary)' }}>BTW</span>
            </span>
          </Link>

          {/* Navigation Links */}
          <nav style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            {navItems.map((item) => {
              const isActive = location.pathname === item.path;
              return (
                <Link
                  key={item.path}
                  to={item.path}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '0.5rem',
                    padding: '0.5rem 0.875rem',
                    borderRadius: 'var(--radius-md)',
                    fontSize: '0.875rem',
                    fontWeight: 500,
                    color: isActive ? 'var(--color-primary)' : 'var(--color-text-secondary)',
                    backgroundColor: isActive ? 'rgba(235, 115, 18, 0.1)' : 'transparent',
                    border: `1px solid ${isActive ? 'rgba(235, 115, 18, 0.3)' : 'transparent'}`,
                    transition: 'all 0.15s ease',
                  }}
                >
                  {item.icon}
                  <span>{item.label}</span>
                </Link>
              );
            })}
          </nav>
        </div>

        {/* User Info & Actions */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.875rem' }}>
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem',
              color: 'var(--color-text-secondary)',
              fontSize: '0.8125rem',
              padding: '0.375rem 0.75rem',
              backgroundColor: 'var(--color-bg-base)',
              borderRadius: 'var(--radius-md)',
              border: '1px solid var(--color-border-subtle)',
            }}
          >
            <UserIcon size={14} />
            <span style={{ fontWeight: 500, color: 'var(--color-text-primary)' }}>{user?.name}</span>
          </div>

          <button
            onClick={logout}
            className="btn btn-secondary"
            style={{ padding: '0.4375rem 0.875rem', fontSize: '0.8125rem' }}
          >
            <LogOut size={14} />
            <span>Sign Out</span>
          </button>
        </div>
      </div>
    </header>
  );
};

import React, { useState } from 'react';
import { Link, NavLink, useLocation } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { useTheme } from '../../context/ThemeContext';
import { Code2, LogOut, LayoutDashboard, BarChart2, Trophy, Terminal, UserCircle, Medal, Sun, Moon, Menu, X, ChevronDown } from 'lucide-react';

const navItems = [
  { label: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
  { label: 'Analytics', path: '/analytics', icon: BarChart2 },
  { label: 'Contests', path: '/contests', icon: Trophy },
  { label: 'Leaderboard', path: '/leaderboard', icon: Medal },
  { label: 'Coding Profiles', path: '/coding-profiles', icon: Terminal },
];

export const AppNavbar: React.FC = () => {
  const { user, logout } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [profileOpen, setProfileOpen] = useState(false);
  const location = useLocation();

  React.useEffect(() => { setMobileOpen(false); setProfileOpen(false); }, [location.pathname]);
  React.useEffect(() => {
    if (!mobileOpen && !profileOpen) return;
    const close = (event: KeyboardEvent) => { if (event.key === 'Escape') { setMobileOpen(false); setProfileOpen(false); } };
    window.addEventListener('keydown', close);
    return () => window.removeEventListener('keydown', close);
  }, [mobileOpen, profileOpen]);

  return <header className="app-header">
    <div className="app-header-inner">
      <Link to="/dashboard" className="brand" aria-label="KodBTW dashboard">
        <span className="brand-mark"><Code2 size={19} /></span><span>Kod<span className="brand-accent">BTW</span></span>
      </Link>
      <nav id="primary-navigation" className={`primary-nav ${mobileOpen ? 'is-open' : ''}`} aria-label="Primary navigation">
        {navItems.map(({ label, path, icon: Icon }) => <NavLink key={path} to={path} className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}>
          <Icon size={16} aria-hidden="true" /><span>{label}</span>
        </NavLink>)}
      </nav>
      <div className="header-actions">
        <button className="icon-button theme-toggle" onClick={toggleTheme} aria-label={`Switch to ${theme === 'dark' ? 'light' : 'dark'} theme`} title={`Switch to ${theme === 'dark' ? 'light' : 'dark'} theme`}>
          {theme === 'dark' ? <Sun size={17} /> : <Moon size={17} />}
        </button>
        <div className="profile-menu-wrap">
          <button className="profile-menu-trigger" onClick={() => setProfileOpen((open) => !open)} aria-expanded={profileOpen} aria-haspopup="menu">
            <span className="avatar">{(user?.name || 'U').slice(0, 1).toUpperCase()}</span>
            <span className="profile-name">{user?.name}</span><ChevronDown size={14} />
          </button>
          {profileOpen && <div className="profile-menu" role="menu">
            <Link to="/profile" role="menuitem"><UserCircle size={16} /> Developer Profile</Link>
            <button role="menuitem" onClick={logout}><LogOut size={16} /> Sign out</button>
          </div>}
        </div>
        <button className="icon-button mobile-menu-toggle" aria-label={mobileOpen ? 'Close navigation' : 'Open navigation'} aria-expanded={mobileOpen} aria-controls="primary-navigation" onClick={() => setMobileOpen((open) => !open)}>
          {mobileOpen ? <X size={20} /> : <Menu size={20} />}
        </button>
      </div>
    </div>
  </header>;
};

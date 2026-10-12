import React from 'react';
import { Link, Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { SessionValidationNotice } from './SessionValidationNotice';

interface ProtectedRouteProps {
  children?: React.ReactNode;
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
  const { isAuthenticated, isLoading, sessionUnavailable } = useAuth();
  const location = useLocation();

  if (isLoading && !sessionUnavailable) {
    return (
      <div style={{
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: 'var(--color-bg-base)',
        color: 'var(--color-text-secondary)',
        gap: '1rem',
      }}>
        <div className="spinner" style={{ width: '2rem', height: '2rem', borderTopColor: 'var(--color-primary)' }} />
        <span style={{ fontSize: '0.875rem' }}>Authenticating session...</span>
      </div>
    );
  }

  if (sessionUnavailable) {
    return (
      <main style={{
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '1.5rem',
        backgroundColor: 'var(--color-bg-base)',
      }}>
        <div style={{ width: '100%', maxWidth: '34rem' }}>
          <SessionValidationNotice />
          <p style={{ textAlign: 'center', marginTop: '1rem' }}>
            <Link to="/login" state={{ from: location }}>Continue to sign in</Link>
          </p>
        </div>
      </main>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return children ? <>{children}</> : null;
};

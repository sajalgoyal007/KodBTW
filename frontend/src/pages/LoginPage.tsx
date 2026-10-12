import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Code2, AlertCircle, ArrowRight } from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { ApiError } from '../services/api/apiClient';
import { BackendConnectionStatus } from '../components/auth/BackendConnectionStatus';
import { useBackendPreflight } from '../hooks/useBackendPreflight';
import { formatAuthApiError } from '../services/api/authErrors';
import { SessionValidationNotice } from '../components/auth/SessionValidationNotice';

export const LoginPage: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(false);

  const { login, isLoading: isSessionLoading } = useAuth();
  const {
    status: backendStatus,
    submitIfReady,
    retry: retryBackendCheck,
  } = useBackendPreflight();
  const navigate = useNavigate();
  const location = useLocation();

  const from = (location.state as { from?: { pathname: string } })?.from?.pathname || '/dashboard';

  const validate = (): boolean => {
    const errors: Record<string, string> = {};
    if (!email.trim()) {
      errors.email = 'Email address is required';
    } else if (!/\S+@\S+\.\S+/.test(email)) {
      errors.email = 'Please enter a valid email address';
    }
    if (!password) {
      errors.password = 'Password is required';
    }
    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setFieldErrors({});

    if (!validate()) {
      return;
    }
    try {
      await submitIfReady(async () => {
        setIsLoading(true);
        await login(email.trim(), password);
        navigate(from, { replace: true });
      });
    } catch (err) {
      if (err instanceof ApiError) {
        setError(formatAuthApiError(err.status, 'login'));
        if (err.data.details) {
          setFieldErrors(err.data.details);
        }
      } else {
        setError('Failed to connect to the server. Please try again.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-panel">
        {/* Branding */}
        <div className="auth-heading">
          <Link to="/" className="auth-brand">
            <div className="auth-brand-mark">
              <Code2 size={24} />
            </div>
            <span className="auth-wordmark">
              Kod<span style={{ color: 'var(--color-primary)' }}>BTW</span>
            </span>
          </Link>
          <h1>Welcome back</h1>
          <p>
            Sign in to access your unified dashboard
          </p>
        </div>

        {/* Login Card */}
        <div className="card auth-card">
          <SessionValidationNotice />
          <BackendConnectionStatus status={backendStatus} onRetry={() => { void retryBackendCheck(); }} />
          {error && (
            <div className="alert alert-error" role="alert">
              <AlertCircle size={18} style={{ flexShrink: 0 }} />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label" htmlFor="email">Email address</label>
              <input
                id="email"
                type="email"
                className="form-input"
                placeholder="developer@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                autoComplete="email"
                disabled={isLoading || isSessionLoading}
                aria-invalid={Boolean(fieldErrors.email)}
                aria-describedby={fieldErrors.email ? 'login-email-error' : undefined}
              />
              {fieldErrors.email && <span id="login-email-error" className="form-error" role="alert">{fieldErrors.email}</span>}
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="password">Password</label>
              <input
                id="password"
                type="password"
                className="form-input"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                autoComplete="current-password"
                disabled={isLoading || isSessionLoading}
                aria-invalid={Boolean(fieldErrors.password)}
                aria-describedby={fieldErrors.password ? 'login-password-error' : undefined}
              />
              {fieldErrors.password && <span id="login-password-error" className="form-error" role="alert">{fieldErrors.password}</span>}
            </div>

            <button
              type="submit"
              className="btn btn-primary auth-submit"
              disabled={isLoading || isSessionLoading || backendStatus !== 'reachable'}
            >
              {isLoading ? (
                <>
                  <div className="spinner" /> Signing in...
                </>
              ) : (
                <>
                  Sign In <ArrowRight size={16} />
                </>
              )}
            </button>
          </form>
        </div>

        {/* Footer Link */}
        <p className="auth-footer">
          Don't have an account?{' '}
          <Link to="/register" style={{ color: 'var(--color-primary)', fontWeight: 500 }}>
            Create one free
          </Link>
        </p>
      </div>
    </div>
  );
};

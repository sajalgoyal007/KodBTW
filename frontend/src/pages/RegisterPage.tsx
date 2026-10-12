import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Code2, AlertCircle, ArrowRight } from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { ApiError } from '../services/api/apiClient';

export const RegisterPage: React.FC = () => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(false);

  const { register } = useAuth();
  const navigate = useNavigate();

  const validate = (): boolean => {
    const errors: Record<string, string> = {};
    if (!name.trim()) {
      errors.name = 'Full name is required';
    } else if (name.trim().length < 2) {
      errors.name = 'Name must be at least 2 characters';
    }

    if (!email.trim()) {
      errors.email = 'Email address is required';
    } else if (!/\S+@\S+\.\S+/.test(email)) {
      errors.email = 'Please enter a valid email address';
    }

    if (!password) {
      errors.password = 'Password is required';
    } else if (password.length < 8) {
      errors.password = 'Password must be at least 8 characters';
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

    setIsLoading(true);
    try {
      await register(name.trim(), email.trim(), password);
      navigate('/dashboard', { replace: true });
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.data.message || err.message);
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
          <h1>Create an account</h1>
          <p>
            Start unifying your competitive coding statistics
          </p>
        </div>

        {/* Register Card */}
        <div className="card">
          {error && (
            <div className="alert alert-error" role="alert">
              <AlertCircle size={18} style={{ flexShrink: 0 }} />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label" htmlFor="name">Full name</label>
              <input
                id="name"
                type="text"
                className="form-input"
                placeholder="Aarav Sharma"
                value={name}
                onChange={(e) => setName(e.target.value)}
                autoComplete="name"
                disabled={isLoading}
                aria-invalid={Boolean(fieldErrors.name)}
                aria-describedby={fieldErrors.name ? 'register-name-error' : undefined}
              />
              {fieldErrors.name && <span id="register-name-error" className="form-error" role="alert">{fieldErrors.name}</span>}
            </div>

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
                disabled={isLoading}
                aria-invalid={Boolean(fieldErrors.email)}
                aria-describedby={fieldErrors.email ? 'register-email-error' : undefined}
              />
              {fieldErrors.email && <span id="register-email-error" className="form-error" role="alert">{fieldErrors.email}</span>}
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="password">Password (min. 8 characters)</label>
              <input
                id="password"
                type="password"
                className="form-input"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                autoComplete="new-password"
                disabled={isLoading}
                aria-invalid={Boolean(fieldErrors.password)}
                aria-describedby={fieldErrors.password ? 'register-password-error' : undefined}
              />
              {fieldErrors.password && <span id="register-password-error" className="form-error" role="alert">{fieldErrors.password}</span>}
            </div>

            <button
              type="submit"
              className="btn btn-primary"
              style={{ width: '100%', marginTop: '0.5rem', padding: '0.75rem' }}
              disabled={isLoading}
            >
              {isLoading ? (
                <>
                  <div className="spinner" /> Creating account...
                </>
              ) : (
                <>
                  Create Account <ArrowRight size={16} />
                </>
              )}
            </button>
          </form>
        </div>

        {/* Footer Link */}
        <p className="auth-footer">
          Already have an account?{' '}
          <Link to="/login" style={{ color: 'var(--color-primary)', fontWeight: 500 }}>
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
};

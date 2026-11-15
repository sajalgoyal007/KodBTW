import React, { useCallback, useEffect, useRef, useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Code2, AlertCircle, ArrowRight, Mail, RefreshCw, CheckCircle2 } from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { ApiError } from '../services/api/apiClient';
import { authApi } from '../services/api/authApi';
import { BackendConnectionStatus } from '../components/auth/BackendConnectionStatus';
import { useBackendPreflight } from '../hooks/useBackendPreflight';
import { formatAuthApiError, formatResendVerificationError } from '../services/api/authErrors';
import { SessionValidationNotice } from '../components/auth/SessionValidationNotice';
import { GoogleSignInButton } from '../components/auth/GoogleSignInButton';
import { formatGoogleAuthError } from '../services/api/authErrors';
import { runGoogleAuthPageFlow } from '../services/api/googleAuthPageFlow';

export const LoginPage: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(false);
  const googleAuthInProgress = useRef(false);

  // Email not verified state
  const [emailNotVerified, setEmailNotVerified] = useState(false);
  const [resendStatus, setResendStatus] = useState<'idle' | 'loading' | 'success' | 'error'>('idle');
  const [resendMessage, setResendMessage] = useState<string | null>(null);
  const [cooldownSeconds, setCooldownSeconds] = useState(0);

  const { login, loginWithGoogle, isLoading: isSessionLoading } = useAuth();
  const {
    status: backendStatus,
    submitIfReady,
    retry: retryBackendCheck,
  } = useBackendPreflight();
  const navigate = useNavigate();
  const location = useLocation();

  const from = (location.state as { from?: { pathname: string } })?.from?.pathname || '/dashboard';

  useEffect(() => {
    if (cooldownSeconds <= 0) return;
    const timer = setInterval(() => {
      setCooldownSeconds((prev) => (prev > 0 ? prev - 1 : 0));
    }, 1000);
    return () => clearInterval(timer);
  }, [cooldownSeconds]);

  const handleGoogleCredential = useCallback(async (credential: string, challengeId: string) => {
    await runGoogleAuthPageFlow({
      credential,
      challengeId,
      destination: from,
      inProgress: googleAuthInProgress,
      submitIfReady,
      loginWithGoogle,
      navigate,
      setError,
      setIsLoading,
      formatError: (err) => err instanceof ApiError
        ? formatGoogleAuthError(err.status, err.data.code)
        : 'Google sign-in could not be completed. Please try again.',
    });
  }, [from, loginWithGoogle, navigate, submitIfReady]);

  const handleGoogleError = useCallback((message: string) => setError(message), []);

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
    setEmailNotVerified(false);
    setResendMessage(null);

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
        const code = err.data?.code;
        if (code === 'EMAIL_NOT_VERIFIED' || (err.status === 403 && !code)) {
          setEmailNotVerified(true);
        }
        setError(formatAuthApiError(err.status, 'login', code));
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

  const handleResendVerification = async () => {
    if (!email.trim() || cooldownSeconds > 0) return;

    setResendStatus('loading');
    setResendMessage(null);

    try {
      const response = await authApi.resendVerification({ email: email.trim() });
      setResendStatus('success');
      setResendMessage(response.message || 'Verification email sent! Check your inbox.');
      setCooldownSeconds(60);
    } catch (err) {
      setResendStatus('error');
      if (err instanceof ApiError) {
        const retryAfter = err.data?.retryAfterSeconds;
        if (retryAfter) {
          const parsed = parseInt(retryAfter, 10);
          if (!isNaN(parsed) && parsed > 0) {
            setCooldownSeconds(parsed);
          }
        }
        setResendMessage(formatResendVerificationError(err.status, err.data?.code, retryAfter));
      } else {
        setResendMessage('Failed to connect to the server. Please try again.');
      }
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
            <div className={`alert ${emailNotVerified ? 'alert-warning' : 'alert-error'}`} role="alert">
              {emailNotVerified ? <Mail size={18} style={{ flexShrink: 0 }} /> : <AlertCircle size={18} style={{ flexShrink: 0 }} />}
              <span>{error}</span>
            </div>
          )}

          {/* Resend verification section — shown when login fails due to unverified email */}
          {emailNotVerified && (
            <div style={{
              padding: '0.875rem 1rem',
              borderRadius: 'var(--radius-md, 8px)',
              border: '1px solid color-mix(in srgb, var(--color-primary) 25%, transparent)',
              backgroundColor: 'color-mix(in srgb, var(--color-primary) 6%, transparent)',
              marginBottom: '1rem',
            }}>
              <p style={{
                color: 'var(--color-text-secondary)',
                fontSize: '0.875rem',
                lineHeight: 1.5,
                margin: '0 0 0.75rem 0',
              }}>
                Need a new verification link? We'll send one to <strong style={{ color: 'var(--color-text-primary)' }}>{email.trim()}</strong>.
              </p>

              {resendMessage && (
                <div
                  className={`alert ${resendStatus === 'success' ? 'alert-success' : 'alert-error'}`}
                  role="alert"
                  style={{ marginBottom: '0.75rem', fontSize: '0.85rem' }}
                >
                  {resendStatus === 'success' ? <CheckCircle2 size={16} /> : <AlertCircle size={16} />}
                  <span>{resendMessage}</span>
                </div>
              )}

              <button
                type="button"
                className="btn btn-secondary"
                style={{ width: '100%', justifyContent: 'center' }}
                onClick={handleResendVerification}
                disabled={resendStatus === 'loading' || cooldownSeconds > 0 || !email.trim()}
              >
                {resendStatus === 'loading' ? (
                  <>
                    <div className="spinner" /> Sending...
                  </>
                ) : cooldownSeconds > 0 ? (
                  `Resend in ${cooldownSeconds}s`
                ) : (
                  <>
                    <RefreshCw size={15} /> Resend Verification Email
                  </>
                )}
              </button>
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
          <div className="auth-divider" aria-hidden="true"><span>or continue with</span></div>
          <GoogleSignInButton
            disabled={isLoading || isSessionLoading || backendStatus !== 'reachable'}
            onCredential={handleGoogleCredential}
            onError={handleGoogleError}
          />
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

import React, { useCallback, useEffect, useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Code2, AlertCircle, ArrowRight, Mail, CheckCircle2, RefreshCw } from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { ApiError } from '../services/api/apiClient';
import { authApi } from '../services/api/authApi';
import { BackendConnectionStatus } from '../components/auth/BackendConnectionStatus';
import { useBackendPreflight } from '../hooks/useBackendPreflight';
import {
  formatAuthApiError,
  formatRegistrationAutoLoginFailure,
  formatResendVerificationError,
  RegistrationAutoLoginError,
} from '../services/api/authErrors';
import { SessionValidationNotice } from '../components/auth/SessionValidationNotice';
import { GoogleSignInButton } from '../components/auth/GoogleSignInButton';
import { formatGoogleAuthError } from '../services/api/authErrors';
import { runGoogleAuthPageFlow } from '../services/api/googleAuthPageFlow';

export const RegisterPage: React.FC = () => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(false);
  const googleAuthInProgress = useRef(false);

  // Email confirmation state
  const [isRegistered, setIsRegistered] = useState(false);
  const [registeredEmail, setRegisteredEmail] = useState('');
  const [emailDeliveryFailed, setEmailDeliveryFailed] = useState(false);
  const [resendStatus, setResendStatus] = useState<'idle' | 'loading' | 'success' | 'error'>('idle');
  const [resendMessage, setResendMessage] = useState<string | null>(null);
  const [cooldownSeconds, setCooldownSeconds] = useState(0);

  const { register, loginWithGoogle, isLoading: isSessionLoading } = useAuth();
  const {
    status: backendStatus,
    submitIfReady,
    retry: retryBackendCheck,
  } = useBackendPreflight();
  const navigate = useNavigate();

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
      destination: '/dashboard',
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
  }, [loginWithGoogle, navigate, submitIfReady]);

  const handleGoogleError = useCallback((message: string) => setError(message), []);

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
    try {
      await submitIfReady(async () => {
        setIsLoading(true);
        const trimmedEmail = email.trim();
        const response = await register(name.trim(), trimmedEmail, password);
        setRegisteredEmail(trimmedEmail);
        setIsRegistered(true);
        if (response && response.verificationEmailSent === false) {
          setEmailDeliveryFailed(true);
        }
      });
    } catch (err) {
      if (err instanceof RegistrationAutoLoginError) {
        setError(formatRegistrationAutoLoginFailure());
      } else if (err instanceof ApiError) {
        setError(formatAuthApiError(err.status, 'registration'));
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
    if (!registeredEmail || cooldownSeconds > 0) return;

    setResendStatus('loading');
    setResendMessage(null);

    try {
      const response = await authApi.resendVerification({ email: registeredEmail });
      setResendStatus('success');
      setResendMessage(response.message || 'Verification email resent! Please check your inbox.');
      setEmailDeliveryFailed(false);
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
          <h1>{isRegistered ? 'Check your email' : 'Create an account'}</h1>
          <p>
            {isRegistered
              ? 'We sent a verification link to your inbox'
              : 'Start unifying your competitive coding statistics'}
          </p>
        </div>

        {/* Card */}
        {isRegistered ? (
          <div className="card auth-card" style={{ textAlign: 'center', padding: '2rem 1.5rem' }}>
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '1rem' }}>
              <div
                style={{
                  width: 56,
                  height: 56,
                  borderRadius: '50%',
                  backgroundColor: emailDeliveryFailed
                    ? 'color-mix(in srgb, var(--color-warning, #f59e0b) 15%, transparent)'
                    : 'color-mix(in srgb, var(--color-primary) 15%, transparent)',
                  color: emailDeliveryFailed ? 'var(--color-warning, #f59e0b)' : 'var(--color-primary)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                }}
              >
                {emailDeliveryFailed ? <AlertCircle size={30} /> : <Mail size={30} />}
              </div>

              <h2 style={{ fontSize: '1.25rem', fontWeight: 700, margin: 0 }}>
                {emailDeliveryFailed ? 'Email Delivery Pending' : 'Check your inbox'}
              </h2>

              <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.925rem', margin: 0, lineHeight: 1.5 }}>
                {emailDeliveryFailed ? (
                  <>
                    Your account has been created, but we could not deliver the verification email immediately to{' '}
                    <strong style={{ color: 'var(--color-text-primary)' }}>{registeredEmail}</strong>. Please click below to resend.
                  </>
                ) : (
                  <>
                    We have sent a verification link to{' '}
                    <strong style={{ color: 'var(--color-text-primary)' }}>{registeredEmail}</strong>. Please click the link to verify your email address and activate your account.
                  </>
                )}
              </p>

              {resendMessage && (
                <div
                  className={`alert ${resendStatus === 'success' ? 'alert-success' : 'alert-error'}`}
                  role="alert"
                  style={{ width: '100%', fontSize: '0.875rem', marginTop: '0.5rem' }}
                >
                  {resendStatus === 'success' ? <CheckCircle2 size={16} /> : <AlertCircle size={16} />}
                  <span>{resendMessage}</span>
                </div>
              )}

              <div style={{ width: '100%', display: 'flex', flexDirection: 'column', gap: '0.75rem', marginTop: '0.75rem' }}>
                <button
                  type="button"
                  className="btn btn-secondary"
                  style={{ width: '100%', justifyContent: 'center' }}
                  onClick={handleResendVerification}
                  disabled={resendStatus === 'loading' || cooldownSeconds > 0}
                >
                  {resendStatus === 'loading' ? (
                    <>
                      <div className="spinner" /> Resending...
                    </>
                  ) : cooldownSeconds > 0 ? (
                    `Resend in ${cooldownSeconds}s`
                  ) : (
                    <>
                      <RefreshCw size={15} /> Resend verification email
                    </>
                  )}
                </button>

                <Link
                  to="/login"
                  className="btn btn-primary"
                  style={{ width: '100%', justifyContent: 'center' }}
                >
                  Go to Sign In <ArrowRight size={16} />
                </Link>
              </div>
            </div>
          </div>
        ) : (
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
                <label className="form-label" htmlFor="name">Full name</label>
                <input
                  id="name"
                  type="text"
                  className="form-input"
                  placeholder="Aarav Sharma"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  autoComplete="name"
                  disabled={isLoading || isSessionLoading}
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
                  disabled={isLoading || isSessionLoading}
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
                  disabled={isLoading || isSessionLoading}
                  aria-invalid={Boolean(fieldErrors.password)}
                  aria-describedby={fieldErrors.password ? 'register-password-error' : undefined}
                />
                {fieldErrors.password && <span id="register-password-error" className="form-error" role="alert">{fieldErrors.password}</span>}
              </div>

              <button
                type="submit"
                className="btn btn-primary auth-submit"
                disabled={isLoading || isSessionLoading || backendStatus !== 'reachable'}
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
            <div className="auth-divider" aria-hidden="true"><span>or continue with</span></div>
            <GoogleSignInButton
              disabled={isLoading || isSessionLoading || backendStatus !== 'reachable'}
              onCredential={handleGoogleCredential}
              onError={handleGoogleError}
            />
          </div>
        )}

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

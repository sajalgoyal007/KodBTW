import React, { useEffect, useRef, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { Code2, CheckCircle2, AlertCircle, Clock, ArrowRight, RefreshCw, Mail } from 'lucide-react';
import { authApi } from '../services/api/authApi';
import { ApiError } from '../services/api/apiClient';
import { formatResendVerificationError, formatVerifyEmailError } from '../services/api/authErrors';

type VerificationState = 'verifying' | 'success' | 'expired' | 'already_used' | 'invalid' | 'missing';

export const VerifyEmailPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');

  const [state, setState] = useState<VerificationState>('verifying');
  const [message, setMessage] = useState<string>('');
  const [resendEmail, setResendEmail] = useState<string>('');
  const [resendStatus, setResendStatus] = useState<'idle' | 'loading' | 'success' | 'error'>('idle');
  const [resendMessage, setResendMessage] = useState<string | null>(null);
  const [cooldownSeconds, setCooldownSeconds] = useState<number>(0);

  const verificationAttempted = useRef(false);

  useEffect(() => {
    if (cooldownSeconds <= 0) return;
    const timer = setInterval(() => {
      setCooldownSeconds((prev) => (prev > 0 ? prev - 1 : 0));
    }, 1000);
    return () => clearInterval(timer);
  }, [cooldownSeconds]);

  useEffect(() => {
    if (verificationAttempted.current) return;
    verificationAttempted.current = true;

    if (!token || !token.trim()) {
      setState('missing');
      setMessage('No email verification token was provided in the link.');
      return;
    }

    const verify = async () => {
      try {
        const response = await authApi.verifyEmail({ token: token.trim() });
        setState('success');
        setMessage(response.message || 'Your email address has been verified successfully!');
      } catch (err) {
        if (err instanceof ApiError) {
          const code = err.data?.code;
          if (code === 'TOKEN_EXPIRED') {
            setState('expired');
            setMessage(formatVerifyEmailError('TOKEN_EXPIRED'));
          } else if (code === 'TOKEN_ALREADY_USED') {
            setState('already_used');
            setMessage(formatVerifyEmailError('TOKEN_ALREADY_USED'));
          } else {
            setState('invalid');
            setMessage(formatVerifyEmailError(code || 'INVALID_TOKEN'));
          }
        } else {
          setState('invalid');
          setMessage('Unable to verify email due to a connection error. Please try again.');
        }
      }
    };

    void verify();
  }, [token]);

  const handleResend = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!resendEmail.trim() || cooldownSeconds > 0) return;

    setResendStatus('loading');
    setResendMessage(null);

    try {
      const response = await authApi.resendVerification({ email: resendEmail.trim() });
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
          <h1>Email Verification</h1>
          <p>Confirming your account email address</p>
        </div>

        {/* Verification Status Card */}
        <div className="card auth-card" style={{ textAlign: 'center', padding: '2rem 1.5rem' }}>
          {state === 'verifying' && (
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '1rem', padding: '1rem 0' }}>
              <div className="spinner" style={{ width: 36, height: 36, borderWidth: 3 }} />
              <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.95rem' }}>
                Verifying your email address, please wait...
              </p>
            </div>
          )}

          {state === 'success' && (
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '1rem' }}>
              <div
                style={{
                  width: 56,
                  height: 56,
                  borderRadius: '50%',
                  backgroundColor: 'color-mix(in srgb, var(--color-success, #22c55e) 15%, transparent)',
                  color: 'var(--color-success, #22c55e)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                }}
              >
                <CheckCircle2 size={32} />
              </div>
              <h2 style={{ fontSize: '1.25rem', fontWeight: 700, margin: 0 }}>Email Verified!</h2>
              <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.925rem', margin: 0 }}>
                {message}
              </p>
              <Link
                to="/login"
                className="btn btn-primary"
                style={{ marginTop: '0.75rem', width: '100%', justifyContent: 'center' }}
              >
                Proceed to Sign In <ArrowRight size={16} />
              </Link>
            </div>
          )}

          {state === 'already_used' && (
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '1rem' }}>
              <div
                style={{
                  width: 56,
                  height: 56,
                  borderRadius: '50%',
                  backgroundColor: 'color-mix(in srgb, var(--color-primary) 15%, transparent)',
                  color: 'var(--color-primary)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                }}
              >
                <CheckCircle2 size={32} />
              </div>
              <h2 style={{ fontSize: '1.25rem', fontWeight: 700, margin: 0 }}>Already Verified</h2>
              <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.925rem', margin: 0 }}>
                {message}
              </p>
              <Link
                to="/login"
                className="btn btn-primary"
                style={{ marginTop: '0.75rem', width: '100%', justifyContent: 'center' }}
              >
                Sign In Now <ArrowRight size={16} />
              </Link>
            </div>
          )}

          {(state === 'expired' || state === 'invalid' || state === 'missing') && (
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '1rem' }}>
              <div
                style={{
                  width: 56,
                  height: 56,
                  borderRadius: '50%',
                  backgroundColor: state === 'expired'
                    ? 'color-mix(in srgb, var(--color-warning, #f59e0b) 15%, transparent)'
                    : 'color-mix(in srgb, var(--color-error, #ef4444) 15%, transparent)',
                  color: state === 'expired' ? 'var(--color-warning, #f59e0b)' : 'var(--color-error, #ef4444)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                }}
              >
                {state === 'expired' ? <Clock size={32} /> : <AlertCircle size={32} />}
              </div>

              <h2 style={{ fontSize: '1.25rem', fontWeight: 700, margin: 0 }}>
                {state === 'expired'
                  ? 'Verification Link Expired'
                  : state === 'missing'
                  ? 'Invalid Verification Link'
                  : 'Verification Failed'}
              </h2>

              <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.925rem', margin: 0 }}>
                {message}
              </p>

              {/* Resend Verification Form */}
              <div style={{ width: '100%', marginTop: '0.5rem', textAlign: 'left' }}>
                <div style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--color-text-secondary)', marginBottom: '0.5rem' }}>
                  Request a new verification link:
                </div>

                {resendMessage && (
                  <div
                    className={`alert ${resendStatus === 'success' ? 'alert-success' : 'alert-error'}`}
                    role="alert"
                    style={{ marginBottom: '0.75rem', fontSize: '0.875rem' }}
                  >
                    {resendStatus === 'success' ? <Mail size={16} /> : <AlertCircle size={16} />}
                    <span>{resendMessage}</span>
                  </div>
                )}

                <form onSubmit={handleResend} style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                  <input
                    type="email"
                    className="form-input"
                    placeholder="Enter your registered email"
                    value={resendEmail}
                    onChange={(e) => setResendEmail(e.target.value)}
                    required
                    disabled={resendStatus === 'loading'}
                  />
                  <button
                    type="submit"
                    className="btn btn-primary"
                    style={{ width: '100%', justifyContent: 'center' }}
                    disabled={resendStatus === 'loading' || cooldownSeconds > 0 || !resendEmail.trim()}
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
                </form>
              </div>

              <div style={{ marginTop: '0.5rem' }}>
                <Link to="/login" style={{ color: 'var(--color-primary)', fontSize: '0.875rem', fontWeight: 500 }}>
                  Back to Sign In
                </Link>
              </div>
            </div>
          )}
        </div>

        {/* Footer Link */}
        <p className="auth-footer">
          Need help?{' '}
          <Link to="/" style={{ color: 'var(--color-primary)', fontWeight: 500 }}>
            Return home
          </Link>
        </p>
      </div>
    </div>
  );
};

export default VerifyEmailPage;

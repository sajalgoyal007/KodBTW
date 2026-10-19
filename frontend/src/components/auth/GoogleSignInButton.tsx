import React, { useEffect, useRef, useState } from 'react';
import { authApi } from '../../services/api/authApi';

interface GoogleCredentialResponse { credential?: string }
interface GoogleIdentityServices {
  accounts: { id: {
    initialize: (options: { client_id: string; nonce: string; callback: (response: GoogleCredentialResponse) => void; error_callback?: () => void }) => void;
    renderButton: (element: HTMLElement, options: { theme: string; size: string; text: string; shape: string; width: number }) => void;
  } };
}

declare global { interface Window { google?: GoogleIdentityServices } }

interface GoogleSignInButtonProps {
  disabled: boolean;
  onCredential: (credential: string, challengeId: string) => void;
  onError: (message: string) => void;
}

const GOOGLE_SCRIPT_ID = 'google-identity-services';
const CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID?.trim() ?? '';

export const GoogleSignInButton: React.FC<GoogleSignInButtonProps> = ({ disabled, onCredential, onError }) => {
  const buttonRef = useRef<HTMLDivElement>(null);
  const [challenge, setChallenge] = useState<{ challengeId: string; nonce: string; expiresAt: string } | null>(null);
  const [challengeState, setChallengeState] = useState<'loading' | 'ready' | 'error' | 'expired'>('loading');
  const [scriptFailed, setScriptFailed] = useState(false);
  const [retryKey, setRetryKey] = useState(0);
  const [scriptReady, setScriptReady] = useState(false);

  useEffect(() => {
    if (!CLIENT_ID || disabled) return;
    let active = true;
    setChallenge(null);
    setChallengeState('loading');
    setScriptFailed(false);
    setScriptReady(false);

    authApi.createGoogleChallenge()
      .then((issued) => {
        if (active) {
          setChallenge(issued);
          setChallengeState('ready');
        }
      })
      .catch(() => {
        if (active) setChallengeState('error');
      });

    return () => { active = false; };
  }, [disabled, retryKey]);

  useEffect(() => {
    if (!challenge || challengeState !== 'ready' || disabled) return;
    const expiresIn = Date.parse(challenge.expiresAt) - Date.now();
    if (!Number.isFinite(expiresIn) || expiresIn <= 0) {
      setChallengeState('expired');
      return;
    }
    const timer = window.setTimeout(() => setChallengeState('expired'), expiresIn);
    return () => window.clearTimeout(timer);
  }, [challenge, challengeState, disabled]);

  useEffect(() => {
    if (!challenge || challengeState !== 'ready' || disabled) return;
    let active = true;
    let initialized = false;
    const initialize = () => {
      if (!active || initialized || !window.google?.accounts?.id || !buttonRef.current) return;
      initialized = true;
      window.google.accounts.id.initialize({
        client_id: CLIENT_ID,
        nonce: challenge.nonce,
        callback: (response) => response.credential
          ? onCredential(response.credential, challenge.challengeId)
          : onError('Google did not return a sign-in credential. Please try again.'),
        error_callback: () => onError('Google sign-in could not be completed. Please try again.'),
      });
      buttonRef.current.replaceChildren();
      window.google.accounts.id.renderButton(buttonRef.current, {
        theme: 'outline', size: 'large', text: 'continue_with', shape: 'rectangular',
        width: Math.max(220, Math.min(360, buttonRef.current.clientWidth)),
      });
      setScriptReady(true);
    };

    if (window.google?.accounts?.id) {
      initialize();
      return () => { active = false; };
    }

    let script = document.getElementById(GOOGLE_SCRIPT_ID) as HTMLScriptElement | null;
    const createdScript = !script;
    if (!script) {
      script = document.createElement('script');
      script.id = GOOGLE_SCRIPT_ID;
      script.src = 'https://accounts.google.com/gsi/client';
      script.async = true;
      script.defer = true;
    }
    const onLoad = () => initialize();
    const handleScriptError = () => { if (active) setScriptFailed(true); };
    script.addEventListener('load', onLoad);
    script.addEventListener('error', handleScriptError);
    if (createdScript) document.head.appendChild(script);
    if (window.google?.accounts?.id) initialize();

    return () => {
      active = false;
      script?.removeEventListener('load', onLoad);
      script?.removeEventListener('error', handleScriptError);
    };
  }, [challenge, challengeState, disabled, onCredential, onError, retryKey]);

  const retry = () => {
    if (scriptFailed && !window.google?.accounts?.id) {
      document.getElementById(GOOGLE_SCRIPT_ID)?.remove();
    }
    setScriptFailed(false);
    setRetryKey((value) => value + 1);
  };

  if (!CLIENT_ID) {
    return <p className="google-sign-in-unavailable" role="status">Google sign-in is not configured for this app.</p>;
  }
  if (disabled) {
    return <button type="button" className="btn btn-secondary auth-submit" disabled>Continue with Google</button>;
  }
  if (challengeState === 'loading') {
    return <span className="google-sign-in-status" role="status" aria-live="polite">Preparing secure Google sign-in…</span>;
  }
  if (challengeState === 'error' || challengeState === 'expired' || scriptFailed) {
    const message = challengeState === 'expired'
      ? 'This sign-in attempt expired. Refresh to continue.'
      : 'Google sign-in could not be prepared. Check your connection and try again.';
    return <div className="google-sign-in-error" role="status">
      <span>{message}</span>
      <button type="button" className="btn btn-secondary" onClick={retry}>Retry Google sign-in</button>
    </div>;
  }
  return <div className="google-sign-in" aria-busy={!scriptReady}>
    <div ref={buttonRef} />
    {!scriptReady && <span className="google-sign-in-status" role="status" aria-live="polite">Loading Google sign-in…</span>}
  </div>;
};

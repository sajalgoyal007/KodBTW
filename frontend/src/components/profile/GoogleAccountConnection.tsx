import React, { useCallback, useEffect, useRef, useState } from 'react';
import { CheckCircle2, Link2, ShieldCheck } from 'lucide-react';
import { GoogleSignInButton } from '../auth/GoogleSignInButton';
import { authApi } from '../../services/api/authApi';
import { ApiError } from '../../services/api/apiClient';
import { formatGoogleLinkError } from '../../services/api/authErrors';
import { runGoogleAccountLinkFlow } from '../../services/api/googleAccountLinkFlow';
import { getGoogleAccountConnectionView } from './googleAccountConnectionState';

function safeLinkError(error: unknown): string {
  if (error instanceof ApiError) return formatGoogleLinkError(error.status, error.data.code);
  return 'Google connection could not be completed. Please try again.';
}

export const GoogleAccountConnection: React.FC = () => {
  const [connected, setConnected] = useState<boolean | null>(null);
  const [checkingStatus, setCheckingStatus] = useState(true);
  const [statusError, setStatusError] = useState<string | null>(null);
  const [currentPassword, setCurrentPassword] = useState('');
  const [challenge, setChallenge] = useState<Awaited<ReturnType<typeof authApi.createGoogleLinkChallenge>> | null>(null);
  const [showConnectForm, setShowConnectForm] = useState(false);
  const [preparing, setPreparing] = useState(false);
  const [linking, setLinking] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const linkInProgress = useRef(false);
  const prepareInProgress = useRef(false);
  const viewState = getGoogleAccountConnectionView(checkingStatus, statusError, connected);

  const refreshStatus = useCallback(async (signal?: AbortSignal) => {
    setCheckingStatus(true);
    setStatusError(null);
    try {
      const result = await authApi.getGoogleIdentityStatus(signal);
      if (!signal?.aborted) setConnected(result.connected);
    } catch (requestError) {
      if (!signal?.aborted) setStatusError(safeLinkError(requestError));
    } finally {
      if (!signal?.aborted) setCheckingStatus(false);
    }
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    void refreshStatus(controller.signal);
    return () => controller.abort();
  }, [refreshStatus]);

  const prepareChallenge = async () => {
    if (prepareInProgress.current || !currentPassword.trim()) return;
    prepareInProgress.current = true;
    setPreparing(true);
    setError(null);
    try {
      const issued = await authApi.createGoogleLinkChallenge(currentPassword);
      setChallenge(issued);
    } catch (requestError) {
      setError(safeLinkError(requestError));
    } finally {
      prepareInProgress.current = false;
      setPreparing(false);
    }
  };

  const requestFreshChallenge = useCallback(
    () => authApi.createGoogleLinkChallenge(currentPassword),
    [currentPassword],
  );

  const handleCredential = (credential: string, challengeId: string) => runGoogleAccountLinkFlow({
    credential,
    challengeId,
    inProgress: linkInProgress,
    linkIdentity: (token, id) => authApi.linkGoogleIdentity({ credential: token, challengeId: id }),
    setIsLinking: setLinking,
    setError,
    setConnected,
    clearSensitiveFormState: () => {
      setCurrentPassword('');
      setChallenge(null);
      setShowConnectForm(false);
    },
    formatError: safeLinkError,
  });

  return (
    <section className="profile-account-connections" aria-labelledby="account-connections-title">
      <div className="profile-account-connections-heading">
        <div>
          <h2 id="account-connections-title">Account Connections</h2>
          <p>Manage the sign-in methods connected to your KodBTW account.</p>
        </div>
      </div>

      <div className="card google-account-card" aria-labelledby="google-account-title">
        <div className="google-account-heading">
          <div className="google-account-icon" aria-hidden="true"><Link2 size={18} /></div>
          <div className="google-account-title-group">
            <h3 id="google-account-title">Google</h3>
            <p>Use your Google account as another secure way to sign in.</p>
          </div>
          {(viewState === 'connected' || viewState === 'disconnected') && (
            <span className={`google-account-state ${connected ? 'is-connected' : 'is-disconnected'}`}>
              {connected ? 'Connected' : 'Not connected'}
            </span>
          )}
        </div>

        {viewState === 'loading' && <p className="google-account-status" role="status">Checking your Google connection…</p>}
        {viewState === 'error' && (
          <div className="google-account-feedback" role="alert">
            <span>{statusError}</span>
            <button type="button" className="btn btn-secondary" onClick={() => void refreshStatus()}>Retry</button>
          </div>
        )}
        {viewState === 'connected' && (
          <p className="google-account-connected" role="status">
            <CheckCircle2 size={17} aria-hidden="true" /> Google is connected to this account.
          </p>
        )}
        {viewState === 'disconnected' && (
          <div className="google-account-form">
            {showConnectForm ? (
              <>
                <p className="google-account-security-note">
                  <ShieldCheck size={16} aria-hidden="true" /> Confirm your KodBTW password before connecting Google.
                </p>
                <label className="form-label" htmlFor="google-link-current-password">Current KodBTW password</label>
                <input
                  id="google-link-current-password"
                  className="form-input"
                  type="password"
                  autoComplete="current-password"
                  value={currentPassword}
                  onChange={(event) => { setCurrentPassword(event.target.value); setChallenge(null); setError(null); }}
                  disabled={preparing || linking}
                />
                {error && <p className="google-account-error" role="alert">{error}</p>}
                {!challenge ? (
                  <div className="google-account-actions">
                    <button
                      type="button"
                      className="btn btn-primary"
                      onClick={() => void prepareChallenge()}
                      disabled={preparing || linking || !currentPassword.trim()}
                    >
                      {preparing ? 'Verifying password…' : 'Verify password to continue'}
                    </button>
                    <button
                      type="button"
                      className="btn btn-ghost"
                      onClick={() => { setShowConnectForm(false); setCurrentPassword(''); setError(null); }}
                      disabled={preparing || linking}
                    >
                      Cancel
                    </button>
                  </div>
                ) : (
                  <>
                    <p className="google-account-status" role="status">Password verified. Continue with Google to finish connecting your account.</p>
                    <GoogleSignInButton
                      disabled={preparing || linking}
                      challenge={challenge}
                      challengeRequest={requestFreshChallenge}
                      onCredential={handleCredential}
                      onError={setError}
                    />
                  </>
                )}
              </>
            ) : (
              <button
                type="button"
                className="btn btn-primary google-account-connect-button"
                onClick={() => { setShowConnectForm(true); setError(null); }}
              >
                <Link2 size={16} aria-hidden="true" /> Connect Google Account
              </button>
            )}
            {linking && <p className="google-account-status" role="status" aria-live="polite">Connecting your Google account…</p>}
          </div>
        )}
      </div>
    </section>
  );
};

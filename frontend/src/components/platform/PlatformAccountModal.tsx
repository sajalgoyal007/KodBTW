import React, { useState, useEffect, useRef, useCallback } from 'react';
import { PlatformAccountResponse, PlatformAccountRequest, PlatformType } from '../../types/platform';
import { ApiError } from '../../services/api/apiClient';
import { X, Layers, AlertCircle } from 'lucide-react';
import { useDialogAccessibility } from '../../hooks/useDialogAccessibility';
import { runSingleSubmission, validatePlatformAccountForm } from '../../utils/platformAccountForm';

interface PlatformAccountModalProps {
  isOpen: boolean;
  initialAccount?: PlatformAccountResponse | null;
  existingPlatforms?: PlatformType[];
  preferredPlatform?: PlatformType | null;
  isLoading?: boolean;
  onClose: () => void;
  onSubmit: (data: PlatformAccountRequest) => Promise<void>;
}

const PLATFORM_OPTIONS: { value: PlatformType; label: string }[] = [
  { value: 'LEETCODE', label: 'LeetCode' },
  { value: 'CODEFORCES', label: 'Codeforces' },
  { value: 'CODECHEF', label: 'CodeChef' },
  { value: 'GEEKSFORGEEKS', label: 'GeeksforGeeks' },
  { value: 'HACKERRANK', label: 'HackerRank' },
];

export const PlatformAccountModal: React.FC<PlatformAccountModalProps> = ({
  isOpen,
  initialAccount,
  existingPlatforms = [],
  preferredPlatform = null,
  isLoading = false,
  onClose,
  onSubmit,
}) => {
  const dialogRef = useRef<HTMLDivElement>(null);
  const isEdit = !!initialAccount;

  const [platform, setPlatform] = useState<PlatformType>('LEETCODE');
  const [username, setUsername] = useState<string>('');
  const [profileUrl, setProfileUrl] = useState<string>('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [generalError, setGeneralError] = useState<string | null>(null);
  const submittingRef = useRef(false);
  const closeIfIdle = useCallback(() => {
    if (!isLoading && !submittingRef.current) onClose();
  }, [isLoading, onClose]);

  useEffect(() => {
    if (initialAccount) {
      setPlatform(initialAccount.platform);
      setUsername(initialAccount.username);
      setProfileUrl(initialAccount.profileUrl || '');
    } else {
      // Pick first unlinked platform if available
      const available = preferredPlatform && !existingPlatforms.includes(preferredPlatform)
        ? preferredPlatform
        : PLATFORM_OPTIONS.find((p) => !existingPlatforms.includes(p.value))?.value;
      setPlatform(available || 'LEETCODE');
      setUsername('');
      setProfileUrl('');
    }
    setFieldErrors({});
    setGeneralError(null);
  }, [initialAccount, isOpen, existingPlatforms, preferredPlatform]);

  useEffect(() => {
    const firstInvalidField = ['platform', 'username', 'profileUrl']
      .find((field) => fieldErrors[field]);
    if (firstInvalidField) {
      const fieldId = firstInvalidField === 'profileUrl'
        ? 'platform-account-profile-url'
        : `platform-account-${firstInvalidField}`;
      document.getElementById(fieldId)?.focus();
    }
  }, [fieldErrors]);

  useDialogAccessibility(isOpen, closeIfIdle, dialogRef);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (isLoading || submittingRef.current) return;
    setFieldErrors({});
    setGeneralError(null);

    const errors = validatePlatformAccountForm({ username, profileUrl });

    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }

    try {
      const submitted = await runSingleSubmission(submittingRef, async () => {
        await onSubmit({
          platform,
          username: username.trim(),
          profileUrl: profileUrl.trim() || null,
        });
      });
      if (!submitted) return;
      onClose();
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        if (err.status === 409) {
          setGeneralError(`A ${platform} account is already connected.`);
        } else if (err.data?.details) {
          setFieldErrors(err.data.details);
        } else {
          setGeneralError('We could not save this platform account. Check the details and try again.');
        }
      } else {
        setGeneralError('We could not save this platform account. Check your connection and try again.');
      }
    }
  };

  return (
    <div
      style={{
        position: 'fixed',
        inset: 0,
        backgroundColor: 'rgba(0, 0, 0, 0.7)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        zIndex: 50,
        padding: '1rem',
        backdropFilter: 'blur(4px)',
      }}
      onClick={(event) => { if (event.target === event.currentTarget) closeIfIdle(); }}
    >
      <div
        className="card"
        ref={dialogRef}
        role="dialog"
        aria-modal="true"
        aria-labelledby="platform-account-dialog-title"
        tabIndex={-1}
        style={{
          width: '100%',
          maxWidth: '480px',
          maxHeight: 'calc(100dvh - 2rem)',
          overflowY: 'auto',
          padding: 'clamp(1rem, 5vw, 2rem)',
          backgroundColor: 'var(--color-bg-card)',
          borderRadius: 'var(--radius-lg)',
          border: '1px solid var(--color-border-subtle)',
          boxShadow: 'var(--shadow-card)',
        }}
        onClick={(e) => e.stopPropagation()}
      >
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.625rem' }}>
            <div
              style={{
                width: '32px',
                height: '32px',
                borderRadius: 'var(--radius-md)',
                backgroundColor: 'var(--color-primary-soft)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: 'var(--color-primary)',
              }}
            >
              <Layers size={18} />
            </div>
            <h3 id="platform-account-dialog-title" style={{ fontSize: '1.25rem', fontWeight: 700 }}>
              {isEdit ? 'Edit Platform Account' : 'Connect Platform Account'}
            </h3>
          </div>
          <button
            onClick={closeIfIdle}
            aria-label="Close dialog"
            disabled={isLoading}
            className="btn btn-ghost"
            style={{ padding: '0.25rem', color: 'var(--color-text-muted)' }}
          >
            <X size={18} />
          </button>
        </div>

        {generalError && (
          <div className="alert alert-error" role="alert" style={{ marginBottom: '1.25rem' }}>
            <AlertCircle size={16} />
            <span>{generalError}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} aria-busy={isLoading}>
          {/* Platform Dropdown */}
          <div className="form-group">
            <label className="form-label" htmlFor="platform-account-platform">Platform</label>
            <select
              id="platform-account-platform"
              className="form-input"
              value={platform}
              onChange={(e) => setPlatform(e.target.value as PlatformType)}
              disabled={isEdit || isLoading}
              aria-invalid={Boolean(fieldErrors.platform)}
              aria-describedby={fieldErrors.platform ? 'platform-account-platform-error' : undefined}
              style={{ cursor: isEdit ? 'not-allowed' : 'pointer' }}
            >
              {PLATFORM_OPTIONS.map((opt) => (
                <option
                  key={opt.value}
                  value={opt.value}
                  disabled={!isEdit && existingPlatforms.includes(opt.value)}
                >
                  {opt.label} {!isEdit && existingPlatforms.includes(opt.value) ? '(Already linked)' : ''}
                </option>
              ))}
            </select>
            {fieldErrors.platform && <div id="platform-account-platform-error" className="form-error" role="alert">{fieldErrors.platform}</div>}
          </div>

          {/* Username */}
          <div className="form-group">
            <label className="form-label" htmlFor="platform-account-username">Handle / Username *</label>
            <input
              id="platform-account-username"
              type="text"
              className="form-input"
              placeholder="e.g. tourist, neal_wu"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              disabled={isLoading}
              maxLength={100}
              required
              aria-invalid={Boolean(fieldErrors.username)}
              aria-describedby={fieldErrors.username ? 'platform-account-username-error' : undefined}
            />
            {fieldErrors.username && <div id="platform-account-username-error" className="form-error" role="alert">{fieldErrors.username}</div>}
          </div>

          {/* Profile URL */}
          <div className="form-group">
            <label className="form-label" htmlFor="platform-account-profile-url">Profile URL (optional)</label>
            <input
              id="platform-account-profile-url"
              type="url"
              className="form-input"
              placeholder="https://leetcode.com/u/username"
              value={profileUrl}
              onChange={(e) => setProfileUrl(e.target.value)}
              disabled={isLoading}
              maxLength={500}
              aria-invalid={Boolean(fieldErrors.profileUrl)}
              aria-describedby={fieldErrors.profileUrl ? 'platform-account-profile-url-error' : undefined}
            />
            {fieldErrors.profileUrl && <div id="platform-account-profile-url-error" className="form-error" role="alert">{fieldErrors.profileUrl}</div>}
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.5rem' }}>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={closeIfIdle}
              disabled={isLoading}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn btn-primary"
              disabled={isLoading}
            >
              {isLoading || submittingRef.current
                ? <><span className="spinner" aria-hidden="true" /><span>{isEdit ? 'Saving…' : 'Connecting…'}</span></>
                : isEdit ? 'Save Changes' : 'Connect Account'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

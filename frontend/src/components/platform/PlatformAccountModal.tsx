import React, { useState, useEffect } from 'react';
import { PlatformAccountResponse, PlatformAccountRequest, PlatformType } from '../../types/platform';
import { ApiError } from '../../services/api/apiClient';
import { X, Layers, AlertCircle } from 'lucide-react';

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
  const isEdit = !!initialAccount;

  const [platform, setPlatform] = useState<PlatformType>('LEETCODE');
  const [username, setUsername] = useState<string>('');
  const [profileUrl, setProfileUrl] = useState<string>('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [generalError, setGeneralError] = useState<string | null>(null);

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

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFieldErrors({});
    setGeneralError(null);

    // Client-side quick check
    const errors: Record<string, string> = {};
    if (!username.trim()) {
      errors.username = 'Username is required';
    } else if (username.length > 100) {
      errors.username = 'Username must not exceed 100 characters';
    }

    if (profileUrl && !profileUrl.startsWith('http://') && !profileUrl.startsWith('https://')) {
      errors.profileUrl = 'Profile URL must be a valid HTTP or HTTPS URL';
    }

    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }

    try {
      await onSubmit({
        platform,
        username: username.trim(),
        profileUrl: profileUrl.trim() || null,
      });
      onClose();
    } catch (err: any) {
      if (err instanceof ApiError) {
        if (err.status === 409) {
          setGeneralError(err.message || `You have already linked a ${platform} account`);
        } else if (err.data?.details) {
          setFieldErrors(err.data.details);
        } else {
          setGeneralError(err.message || 'Failed to save platform account');
        }
      } else {
        setGeneralError(err?.message || 'An unexpected error occurred');
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
      onClick={onClose}
    >
      <div
        className="card"
        style={{
          width: '100%',
          maxWidth: '480px',
          padding: '2rem',
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
                backgroundColor: 'rgba(235, 115, 18, 0.12)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: 'var(--color-primary)',
              }}
            >
              <Layers size={18} />
            </div>
            <h3 style={{ fontSize: '1.25rem', fontWeight: 700 }}>
              {isEdit ? 'Edit Platform Account' : 'Connect Platform Account'}
            </h3>
          </div>
          <button
            onClick={onClose}
            className="btn btn-ghost"
            style={{ padding: '0.25rem', color: 'var(--color-text-muted)' }}
          >
            <X size={18} />
          </button>
        </div>

        {generalError && (
          <div className="alert alert-error" style={{ marginBottom: '1.25rem' }}>
            <AlertCircle size={16} />
            <span>{generalError}</span>
          </div>
        )}

        <form onSubmit={handleSubmit}>
          {/* Platform Dropdown */}
          <div className="form-group">
            <label className="form-label">Platform</label>
            <select
              className="form-input"
              value={platform}
              onChange={(e) => setPlatform(e.target.value as PlatformType)}
              disabled={isEdit || isLoading}
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
            {fieldErrors.platform && <div className="form-error">{fieldErrors.platform}</div>}
          </div>

          {/* Username */}
          <div className="form-group">
            <label className="form-label">Handle / Username *</label>
            <input
              type="text"
              className="form-input"
              placeholder="e.g. tourist, neal_wu"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              disabled={isLoading}
              maxLength={100}
              required
            />
            {fieldErrors.username && <div className="form-error">{fieldErrors.username}</div>}
          </div>

          {/* Profile URL */}
          <div className="form-group">
            <label className="form-label">Profile URL (optional)</label>
            <input
              type="url"
              className="form-input"
              placeholder="https://leetcode.com/u/username"
              value={profileUrl}
              onChange={(e) => setProfileUrl(e.target.value)}
              disabled={isLoading}
              maxLength={500}
            />
            {fieldErrors.profileUrl && <div className="form-error">{fieldErrors.profileUrl}</div>}
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.5rem' }}>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={onClose}
              disabled={isLoading}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn btn-primary"
              disabled={isLoading}
            >
              {isLoading ? <span className="spinner" /> : isEdit ? 'Save Changes' : 'Connect Account'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

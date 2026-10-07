import React, { useState, useEffect } from 'react';
import { ProfileResponse, ProfileRequest } from '../../types/profile';
import { ApiError } from '../../services/api/apiClient';
import { X, UserCheck, AlertCircle } from 'lucide-react';

interface ProfileEditModalProps {
  isOpen: boolean;
  initialProfile: ProfileResponse | null;
  isLoading?: boolean;
  onClose: () => void;
  onSubmit: (data: ProfileRequest) => Promise<void>;
}

export const ProfileEditModal: React.FC<ProfileEditModalProps> = ({
  isOpen,
  initialProfile,
  isLoading = false,
  onClose,
  onSubmit,
}) => {
  const [username, setUsername] = useState<string>('');
  const [displayName, setDisplayName] = useState<string>('');
  const [bio, setBio] = useState<string>('');
  const [avatarUrl, setAvatarUrl] = useState<string>('');
  const [college, setCollege] = useState<string>('');
  const [graduationYear, setGraduationYear] = useState<string>('');
  const [location, setLocation] = useState<string>('');
  const [githubUrl, setGithubUrl] = useState<string>('');
  const [linkedinUrl, setLinkedinUrl] = useState<string>('');
  const [portfolioUrl, setPortfolioUrl] = useState<string>('');

  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [generalError, setGeneralError] = useState<string | null>(null);

  useEffect(() => {
    if (initialProfile) {
      setUsername(initialProfile.username || '');
      setDisplayName(initialProfile.displayName || '');
      setBio(initialProfile.bio || '');
      setAvatarUrl(initialProfile.avatarUrl || '');
      setCollege(initialProfile.college || '');
      setGraduationYear(initialProfile.graduationYear ? String(initialProfile.graduationYear) : '');
      setLocation(initialProfile.location || '');
      setGithubUrl(initialProfile.githubUrl || '');
      setLinkedinUrl(initialProfile.linkedinUrl || '');
      setPortfolioUrl(initialProfile.portfolioUrl || '');
    } else {
      setUsername('');
      setDisplayName('');
      setBio('');
      setAvatarUrl('');
      setCollege('');
      setGraduationYear('');
      setLocation('');
      setGithubUrl('');
      setLinkedinUrl('');
      setPortfolioUrl('');
    }
    setFieldErrors({});
    setGeneralError(null);
  }, [initialProfile, isOpen]);

  if (!isOpen) return null;

  const validateUrl = (url: string) => {
    if (!url) return true;
    return url.startsWith('http://') || url.startsWith('https://');
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFieldErrors({});
    setGeneralError(null);

    const errors: Record<string, string> = {};

    if (username.trim()) {
      const u = username.trim().toLowerCase();
      if (!/^[a-z0-9_-]{3,50}$/.test(u)) {
        errors.username = 'Username must be 3-50 characters with lowercase letters, numbers, hyphens, and underscores only';
      }
    }

    if (displayName.length > 100) {
      errors.displayName = 'Display name must not exceed 100 characters';
    }
    if (bio.length > 1000) {
      errors.bio = 'Bio must not exceed 1000 characters';
    }
    if (college.length > 255) {
      errors.college = 'College must not exceed 255 characters';
    }
    if (location.length > 255) {
      errors.location = 'Location must not exceed 255 characters';
    }

    if (graduationYear) {
      const year = parseInt(graduationYear, 10);
      if (isNaN(year) || year < 2000 || year > 2040) {
        errors.graduationYear = 'Graduation year must be between 2000 and 2040';
      }
    }

    if (avatarUrl && !validateUrl(avatarUrl)) {
      errors.avatarUrl = 'Avatar URL must be a valid HTTP or HTTPS URL';
    }
    if (githubUrl && !validateUrl(githubUrl)) {
      errors.githubUrl = 'GitHub URL must be a valid HTTP or HTTPS URL';
    }
    if (linkedinUrl && !validateUrl(linkedinUrl)) {
      errors.linkedinUrl = 'LinkedIn URL must be a valid HTTP or HTTPS URL';
    }
    if (portfolioUrl && !validateUrl(portfolioUrl)) {
      errors.portfolioUrl = 'Portfolio URL must be a valid HTTP or HTTPS URL';
    }

    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }

    const payload: ProfileRequest = {
      username: username.trim().toLowerCase() || null,
      displayName: displayName.trim() || null,
      bio: bio.trim() || null,
      avatarUrl: avatarUrl.trim() || null,
      college: college.trim() || null,
      graduationYear: graduationYear ? parseInt(graduationYear, 10) : null,
      location: location.trim() || null,
      githubUrl: githubUrl.trim() || null,
      linkedinUrl: linkedinUrl.trim() || null,
      portfolioUrl: portfolioUrl.trim() || null,
    };

    try {
      await onSubmit(payload);
      onClose();
    } catch (err: any) {
      if (err instanceof ApiError) {
        if (err.data?.details) {
          setFieldErrors(err.data.details);
        } else {
          setGeneralError(err.message || 'Failed to update profile');
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
          maxWidth: '560px',
          maxHeight: '90vh',
          overflowY: 'auto',
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
              <UserCheck size={18} />
            </div>
            <h3 style={{ fontSize: '1.25rem', fontWeight: 700 }}>Edit Developer Profile</h3>
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
          {/* Username / Handle */}
          <div className="form-group">
            <label className="form-label">Username / Public Handle</label>
            <input
              type="text"
              className="form-input"
              placeholder="e.g. alexchen"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              disabled={isLoading}
              maxLength={50}
            />
            <div style={{ fontSize: '0.75rem', color: 'var(--color-text-secondary)', marginTop: '0.25rem' }}>
              Your public profile will be shareable at /u/{username || 'username'}
            </div>
            {fieldErrors.username && <div className="form-error">{fieldErrors.username}</div>}
          </div>

          {/* Display Name */}
          <div className="form-group">
            <label className="form-label">Display Name</label>
            <input
              type="text"
              className="form-input"
              placeholder="e.g. Alex Chen"
              value={displayName}
              onChange={(e) => setDisplayName(e.target.value)}
              disabled={isLoading}
              maxLength={100}
            />
            {fieldErrors.displayName && <div className="form-error">{fieldErrors.displayName}</div>}
          </div>

          {/* Bio */}
          <div className="form-group">
            <label className="form-label">Bio (max 1000 characters)</label>
            <textarea
              className="form-input"
              placeholder="Competitive programmer, algorithm enthusiast, full-stack engineer..."
              value={bio}
              onChange={(e) => setBio(e.target.value)}
              disabled={isLoading}
              maxLength={1000}
              rows={3}
              style={{ resize: 'vertical' }}
            />
            {fieldErrors.bio && <div className="form-error">{fieldErrors.bio}</div>}
          </div>

          {/* Two-column layout: College & Graduation Year */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
            <div className="form-group">
              <label className="form-label">College / University</label>
              <input
                type="text"
                className="form-input"
                placeholder="e.g. Stanford University"
                value={college}
                onChange={(e) => setCollege(e.target.value)}
                disabled={isLoading}
                maxLength={255}
              />
              {fieldErrors.college && <div className="form-error">{fieldErrors.college}</div>}
            </div>

            <div className="form-group">
              <label className="form-label">Graduation Year (2000 - 2040)</label>
              <input
                type="number"
                className="form-input mono"
                placeholder="2026"
                value={graduationYear}
                onChange={(e) => setGraduationYear(e.target.value)}
                disabled={isLoading}
                min={2000}
                max={2040}
              />
              {fieldErrors.graduationYear && <div className="form-error">{fieldErrors.graduationYear}</div>}
            </div>
          </div>

          {/* Location & Avatar URL */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
            <div className="form-group">
              <label className="form-label">Location</label>
              <input
                type="text"
                className="form-input"
                placeholder="e.g. San Francisco, CA"
                value={location}
                onChange={(e) => setLocation(e.target.value)}
                disabled={isLoading}
                maxLength={255}
              />
              {fieldErrors.location && <div className="form-error">{fieldErrors.location}</div>}
            </div>

            <div className="form-group">
              <label className="form-label">Avatar URL</label>
              <input
                type="url"
                className="form-input"
                placeholder="https://example.com/avatar.jpg"
                value={avatarUrl}
                onChange={(e) => setAvatarUrl(e.target.value)}
                disabled={isLoading}
                maxLength={500}
              />
              {fieldErrors.avatarUrl && <div className="form-error">{fieldErrors.avatarUrl}</div>}
            </div>
          </div>

          {/* Social Links */}
          <div className="form-group">
            <label className="form-label">GitHub URL</label>
            <input
              type="url"
              className="form-input"
              placeholder="https://github.com/username"
              value={githubUrl}
              onChange={(e) => setGithubUrl(e.target.value)}
              disabled={isLoading}
              maxLength={500}
            />
            {fieldErrors.githubUrl && <div className="form-error">{fieldErrors.githubUrl}</div>}
          </div>

          <div className="form-group">
            <label className="form-label">LinkedIn URL</label>
            <input
              type="url"
              className="form-input"
              placeholder="https://linkedin.com/in/username"
              value={linkedinUrl}
              onChange={(e) => setLinkedinUrl(e.target.value)}
              disabled={isLoading}
              maxLength={500}
            />
            {fieldErrors.linkedinUrl && <div className="form-error">{fieldErrors.linkedinUrl}</div>}
          </div>

          <div className="form-group">
            <label className="form-label">Portfolio URL</label>
            <input
              type="url"
              className="form-input"
              placeholder="https://mywebsite.dev"
              value={portfolioUrl}
              onChange={(e) => setPortfolioUrl(e.target.value)}
              disabled={isLoading}
              maxLength={500}
            />
            {fieldErrors.portfolioUrl && <div className="form-error">{fieldErrors.portfolioUrl}</div>}
          </div>

          {/* Form Actions */}
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
              {isLoading ? <span className="spinner" /> : 'Save Profile'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

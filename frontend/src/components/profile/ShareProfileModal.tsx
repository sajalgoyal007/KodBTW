import React, { useState, useRef } from 'react';
import { X, Copy, Check, Share2 } from 'lucide-react';
import { useDialogAccessibility } from '../../hooks/useDialogAccessibility';

interface ShareProfileModalProps {
  isOpen: boolean;
  onClose: () => void;
  username: string;
  displayName?: string | null;
}

export const ShareProfileModal: React.FC<ShareProfileModalProps> = ({
  isOpen,
  onClose,
  username,
  displayName,
}) => {
  const [copied, setCopied] = useState<boolean>(false);
  const dialogRef = useRef<HTMLDivElement>(null);
  useDialogAccessibility(isOpen, onClose, dialogRef);

  if (!isOpen) return null;

  const origin = typeof window !== 'undefined' ? window.location.origin : 'https://kodbtw.com';
  const shareUrl = `${origin}/u/${encodeURIComponent(username)}`;
  const title = displayName ? `${displayName}'s Developer Profile` : `@${username} on KodBTW`;
  const shareText = `Check out my unified developer stats and competitive programming portfolio on KodBTW!`;

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(shareUrl);
      setCopied(true);
      setTimeout(() => setCopied(false), 2500);
    } catch {
      // Fallback if clipboard API is restricted
      const textarea = document.createElement('textarea');
      textarea.value = shareUrl;
      document.body.appendChild(textarea);
      textarea.select();
      document.execCommand('copy');
      document.body.removeChild(textarea);
      setCopied(true);
      setTimeout(() => setCopied(false), 2500);
    }
  };

  const shareToTwitter = () => {
    const url = `https://twitter.com/intent/tweet?text=${encodeURIComponent(shareText)}&url=${encodeURIComponent(shareUrl)}`;
    window.open(url, '_blank', 'noopener,noreferrer');
  };

  const shareToLinkedIn = () => {
    const url = `https://www.linkedin.com/sharing/share-offsite/?url=${encodeURIComponent(shareUrl)}`;
    window.open(url, '_blank', 'noopener,noreferrer');
  };

  const shareToWhatsApp = () => {
    const url = `https://api.whatsapp.com/send?text=${encodeURIComponent(`${shareText} ${shareUrl}`)}`;
    window.open(url, '_blank', 'noopener,noreferrer');
  };

  return (
    <div
      style={{
        position: 'fixed',
        inset: 0,
        backgroundColor: 'rgba(0, 0, 0, 0.75)',
        backdropFilter: 'blur(4px)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        zIndex: 9999,
        padding: '1rem',
      }}
      onClick={onClose}
    >
      <div
        ref={dialogRef}
        role="dialog"
        aria-modal="true"
        aria-labelledby="share-profile-dialog-title"
        tabIndex={-1}
        style={{
          background: 'var(--color-bg-card, var(--color-bg-card))',
          border: '1px solid var(--color-border, var(--color-border-subtle))',
          borderRadius: '16px',
          width: '100%',
          maxWidth: '480px',
          boxShadow: '0 20px 40px rgba(0,0,0,0.5)',
          overflow: 'hidden',
        }}
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '1.25rem 1.5rem',
            borderBottom: '1px solid var(--color-border, var(--color-border-subtle))',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.625rem' }}>
            <div
              style={{
                width: '32px',
                height: '32px',
                borderRadius: '8px',
                background: 'rgba(56, 189, 248, 0.1)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: 'var(--color-primary, var(--color-primary))',
              }}
            >
              <Share2 size={18} />
            </div>
            <div>
              <h3 id="share-profile-dialog-title" style={{ fontSize: '1.125rem', fontWeight: 600, color: 'var(--color-text-primary)', margin: 0 }}>
                Share Developer Profile
              </h3>
              <p style={{ fontSize: '0.8125rem', color: 'var(--color-text-muted, var(--color-text-muted))', margin: '0.125rem 0 0 0' }}>
                {title}
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            aria-label="Close dialog"
            style={{
              background: 'transparent',
              border: 'none',
              color: 'var(--color-text-muted, var(--color-text-muted))',
              cursor: 'pointer',
              padding: '0.25rem',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              borderRadius: '6px',
            }}
          >
            <X size={20} />
          </button>
        </div>

        {/* Content */}
        <div style={{ padding: '1.5rem' }}>
          {/* Link Box */}
          <label
            style={{
              display: 'block',
              fontSize: '0.8125rem',
              fontWeight: 500,
              color: 'var(--color-text-muted, var(--color-text-muted))',
              marginBottom: '0.5rem',
            }}
          >
            Public Profile Link
          </label>
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem',
              background: 'var(--color-bg-base, var(--color-bg-base))',
              border: '1px solid var(--color-border, var(--color-border-subtle))',
              borderRadius: '8px',
              padding: '0.5rem 0.75rem',
              marginBottom: '1.5rem',
            }}
          >
            <input
              type="text"
              readOnly
              value={shareUrl}
              style={{
                flex: 1,
                background: 'transparent',
                border: 'none',
                color: 'var(--color-text, var(--color-text-primary))',
                fontSize: '0.875rem',
                outline: 'none',
                fontFamily: 'monospace',
              }}
            />
            <button
              onClick={handleCopy}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '0.375rem',
                padding: '0.4rem 0.75rem',
                background: copied ? '#238636' : 'var(--color-primary, var(--color-primary))',
                color: '#ffffff',
                border: 'none',
                borderRadius: '6px',
                fontSize: '0.8125rem',
                fontWeight: 600,
                cursor: 'pointer',
                transition: 'all 0.2s',
                whiteSpace: 'nowrap',
              }}
            >
              {copied ? (
                <>
                  <Check size={14} />
                  Copied!
                </>
              ) : (
                <>
                  <Copy size={14} />
                  Copy Link
                </>
              )}
            </button>
          </div>

          {/* Social Share Buttons */}
          <div style={{ marginBottom: '0.5rem' }}>
            <span
              style={{
                display: 'block',
                fontSize: '0.8125rem',
                fontWeight: 500,
                color: 'var(--color-text-muted, var(--color-text-muted))',
                marginBottom: '0.75rem',
              }}
            >
              Share via
            </span>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '0.75rem' }}>
              <button
                onClick={shareToTwitter}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '0.375rem',
                  padding: '0.625rem',
                  background: 'rgba(255, 255, 255, 0.05)',
                  border: '1px solid var(--color-border, var(--color-border-subtle))',
                  borderRadius: '8px',
                  color: 'var(--color-text, var(--color-text-primary))',
                  fontSize: '0.8125rem',
                  fontWeight: 500,
                  cursor: 'pointer',
                }}
              >
                𝕏 Twitter
              </button>
              <button
                onClick={shareToLinkedIn}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '0.375rem',
                  padding: '0.625rem',
                  background: 'rgba(10, 102, 194, 0.1)',
                  border: '1px solid rgba(10, 102, 194, 0.3)',
                  borderRadius: '8px',
                  color: '#70b5f9',
                  fontSize: '0.8125rem',
                  fontWeight: 500,
                  cursor: 'pointer',
                }}
              >
                LinkedIn
              </button>
              <button
                onClick={shareToWhatsApp}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '0.375rem',
                  padding: '0.625rem',
                  background: 'rgba(37, 211, 102, 0.1)',
                  border: '1px solid rgba(37, 211, 102, 0.3)',
                  borderRadius: '8px',
                  color: '#25d366',
                  fontSize: '0.8125rem',
                  fontWeight: 500,
                  cursor: 'pointer',
                }}
              >
                WhatsApp
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ShareProfileModal;

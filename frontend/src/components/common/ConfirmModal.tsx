import React, { useRef } from 'react';
import { AlertTriangle, X } from 'lucide-react';
import { useDialogAccessibility } from '../../hooks/useDialogAccessibility';

interface ConfirmModalProps {
  isOpen: boolean;
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
  isDestructive?: boolean;
  isLoading?: boolean;
  onConfirm: () => void;
  onClose: () => void;
}

export const ConfirmModal: React.FC<ConfirmModalProps> = ({
  isOpen,
  title,
  message,
  confirmLabel = 'Confirm',
  cancelLabel = 'Cancel',
  isDestructive = true,
  isLoading = false,
  onConfirm,
  onClose,
}) => {
  const dialogRef = useRef<HTMLDivElement>(null);
  useDialogAccessibility(isOpen, onClose, dialogRef);
  if (!isOpen) return null;

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
      onKeyDown={(event) => { if (event.key === 'Escape' && isLoading) event.stopPropagation(); }}
    >
      <div
        className="card"
        ref={dialogRef}
        role="dialog"
        aria-modal="true"
        aria-labelledby="confirm-dialog-title"
        aria-describedby="confirm-dialog-description"
        tabIndex={-1}
        style={{
          width: '100%',
          maxWidth: '440px',
          padding: '1.75rem',
          backgroundColor: 'var(--color-bg-card)',
          borderRadius: 'var(--radius-lg)',
          border: '1px solid var(--color-border-subtle)',
          boxShadow: 'var(--shadow-card)',
          display: 'flex',
          flexDirection: 'column',
          gap: '1.25rem',
        }}
        onClick={(e) => e.stopPropagation()}
      >
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <div
              style={{
                width: '36px',
                height: '36px',
                borderRadius: '50%',
                backgroundColor: isDestructive ? 'rgba(239, 68, 68, 0.15)' : 'rgba(235, 115, 18, 0.15)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: isDestructive ? 'var(--color-error)' : 'var(--color-primary)',
              }}
            >
              <AlertTriangle size={20} />
            </div>
            <h3 id="confirm-dialog-title" style={{ fontSize: '1.125rem', fontWeight: 700 }}>{title}</h3>
          </div>
          <button
            onClick={onClose}
            className="btn btn-ghost"
            aria-label="Close dialog"
            disabled={isLoading}
            style={{ padding: '0.25rem', color: 'var(--color-text-muted)' }}
          >
            <X size={18} />
          </button>
        </div>

        <p id="confirm-dialog-description" style={{ color: 'var(--color-text-secondary)', fontSize: '0.9375rem', lineHeight: 1.5 }}>
          {message}
        </p>

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '0.5rem' }}>
          <button
            type="button"
            className="btn btn-secondary"
            onClick={onClose}
            disabled={isLoading}
          >
            {cancelLabel}
          </button>
          <button
            type="button"
            className="btn"
            style={{
              backgroundColor: isDestructive ? 'var(--color-error)' : 'var(--color-primary)',
              color: '#ffffff',
            }}
            onClick={onConfirm}
            disabled={isLoading}
          >
            {isLoading ? <span className="spinner" /> : confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
};

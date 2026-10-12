import React from 'react';
import { BackendPreflightStatus } from '../../services/api/backendPreflight';

interface BackendConnectionStatusProps {
  status: BackendPreflightStatus;
  onRetry: () => void;
}

export const BackendConnectionStatus: React.FC<BackendConnectionStatusProps> = ({ status, onRetry }) => {
  const needsRetry = status === 'failed' || status === 'stale';
  const message = status === 'checking'
    ? 'Checking connection to KodBTW…'
    : status === 'reachable'
      ? 'Backend reachable. Authentication is checked when you submit.'
      : status === 'stale'
        ? 'Connection check expired. Retry before submitting.'
        : 'Connection check failed. Retry the check before submitting.';

  return (
    <div
      role={needsRetry ? 'alert' : 'status'}
      aria-live={needsRetry ? 'assertive' : 'polite'}
      style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexWrap: 'wrap',
        gap: '0.5rem',
        marginBottom: '1rem',
        color: needsRetry ? 'var(--color-error, #f87171)' : 'var(--color-text-secondary)',
        fontSize: '0.8125rem',
        lineHeight: 1.4,
      }}
    >
      <span>{message}</span>
      {needsRetry && (
        <button
          type="button"
          onClick={onRetry}
          style={{
            border: 0,
            padding: 0,
            background: 'transparent',
            color: 'var(--color-primary)',
            font: 'inherit',
            fontWeight: 600,
            textDecoration: 'underline',
            cursor: 'pointer',
          }}
        >
          Retry connection check
        </button>
      )}
    </div>
  );
};

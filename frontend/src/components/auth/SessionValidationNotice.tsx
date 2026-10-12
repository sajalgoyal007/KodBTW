import React from 'react';
import { useAuth } from '../../hooks/useAuth';

export const SessionValidationNotice: React.FC = () => {
  const { sessionUnavailable, isLoading, retrySession } = useAuth();

  if (!sessionUnavailable) return null;

  return (
    <div className="alert alert-error" role="alert">
      <div>
        <strong>We couldn’t verify your saved session.</strong>{' '}
        It has been kept on this device. Check your connection and retry, or sign in again.
      </div>
      <button
        type="button"
        className="btn btn-secondary"
        onClick={() => { void retrySession(); }}
        disabled={isLoading}
      >
        {isLoading ? 'Checking session…' : 'Retry session check'}
      </button>
    </div>
  );
};

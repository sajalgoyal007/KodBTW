import React from 'react';
import { Layers } from 'lucide-react';

export const PublicProfileEmptyState: React.FC = () => {
  return (
    <div
      style={{
        padding: '3rem 2rem',
        textAlign: 'center',
        background: 'var(--color-bg-card, var(--color-bg-card))',
        border: '1px dashed var(--color-border, var(--color-border-subtle))',
        borderRadius: '12px',
        marginTop: '1.5rem',
      }}
    >
      <div
        style={{
          width: '56px',
          height: '56px',
          borderRadius: '50%',
          background: 'rgba(56, 189, 248, 0.1)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          margin: '0 auto 1rem',
          color: 'var(--color-primary, var(--color-primary))',
        }}
      >
        <Layers size={28} />
      </div>
      <h3
        style={{
          fontSize: '1.125rem',
          fontWeight: 600,
          color: 'var(--color-text, var(--color-text-primary))',
          marginBottom: '0.5rem',
        }}
      >
        No Coding Platforms Connected Yet
      </h3>
      <p
        style={{
          fontSize: '0.875rem',
          color: 'var(--color-text-muted, var(--color-text-muted))',
          maxWidth: '440px',
          margin: '0 auto',
          lineHeight: 1.5,
        }}
      >
        This developer hasn’t connected any competitive programming accounts (LeetCode, Codeforces, etc.) to KodBTW yet.
      </p>
    </div>
  );
};

export default PublicProfileEmptyState;

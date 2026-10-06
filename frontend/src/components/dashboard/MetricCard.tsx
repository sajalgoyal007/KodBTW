import React from 'react';

interface MetricCardProps {
  title: string;
  value: number | string | null | undefined;
  subtitle?: string;
  icon?: React.ReactNode;
  accentColor?: string;
}

export const MetricCard: React.FC<MetricCardProps> = ({
  title,
  value,
  subtitle,
  icon,
  accentColor,
}) => {
  const displayValue = value !== null && value !== undefined ? value.toLocaleString() : '—';

  return (
    <div
      className="card"
      style={{
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'space-between',
        padding: '1.25rem',
        borderLeft: accentColor ? `3px solid ${accentColor}` : undefined,
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.75rem' }}>
        <span style={{ fontSize: '0.8125rem', fontWeight: 500, color: 'var(--color-text-secondary)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
          {title}
        </span>
        {icon && (
          <div style={{ color: accentColor || 'var(--color-text-muted)' }}>
            {icon}
          </div>
        )}
      </div>

      <div>
        <div
          className="mono"
          style={{
            fontSize: '1.75rem',
            fontWeight: 700,
            color: 'var(--color-text-primary)',
            lineHeight: 1.2,
          }}
        >
          {displayValue}
        </div>
        {subtitle && (
          <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', marginTop: '0.375rem' }}>
            {subtitle}
          </div>
        )}
      </div>
    </div>
  );
};

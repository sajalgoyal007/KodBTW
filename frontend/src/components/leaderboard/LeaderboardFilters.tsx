import React, { useState, useEffect } from 'react';
import { LeaderboardSort, LeaderboardDataFilter } from '../../types/leaderboard';
import { Search, X, ShieldCheck, Layers, ArrowUpDown } from 'lucide-react';

interface LeaderboardFiltersProps {
  sort: LeaderboardSort;
  dataFilter: LeaderboardDataFilter;
  college: string;
  onSortChange: (sort: LeaderboardSort) => void;
  onDataFilterChange: (dataFilter: LeaderboardDataFilter) => void;
  onCollegeChange: (college: string) => void;
}

export const LeaderboardFilters: React.FC<LeaderboardFiltersProps> = ({
  sort,
  dataFilter,
  college,
  onSortChange,
  onDataFilterChange,
  onCollegeChange,
}) => {
  const [localCollege, setLocalCollege] = useState(college);

  // Sync internal state with external prop changes
  useEffect(() => {
    setLocalCollege(college);
  }, [college]);

  // Debounce college change by 350ms
  useEffect(() => {
    const timer = setTimeout(() => {
      if (localCollege !== college) {
        onCollegeChange(localCollege);
      }
    }, 350);
    return () => clearTimeout(timer);
  }, [localCollege, college, onCollegeChange]);

  const clearCollege = () => {
    setLocalCollege('');
    onCollegeChange('');
  };

  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        gap: '1rem',
      }}
    >
      {/* Controls Bar */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '1rem',
        }}
      >
        {/* Left: Search by College */}
        <div style={{ position: 'relative', minWidth: '260px', flex: '1 1 280px' }}>
          <Search
            size={16}
            style={{
              position: 'absolute',
              left: '0.875rem',
              top: '50%',
              transform: 'translateY(-50%)',
              color: 'var(--color-text-muted)',
              pointerEvents: 'none',
            }}
          />
          <input
            type="text"
            className="input"
            placeholder="Filter by college / university..."
            value={localCollege}
            onChange={(e) => setLocalCollege(e.target.value)}
            style={{
              paddingLeft: '2.5rem',
              paddingRight: localCollege ? '2.5rem' : '1rem',
              width: '100%',
              fontSize: '0.875rem',
            }}
          />
          {localCollege && (
            <button
              type="button"
              onClick={clearCollege}
              style={{
                position: 'absolute',
                right: '0.75rem',
                top: '50%',
                transform: 'translateY(-50%)',
                background: 'none',
                border: 'none',
                color: 'var(--color-text-muted)',
                cursor: 'pointer',
                padding: '0.25rem',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
              title="Clear college filter"
            >
              <X size={14} />
            </button>
          )}
        </div>

        {/* Right: Data Filter Toggle + Sort Dropdown */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', flexWrap: 'wrap' }}>
          {/* Data Filter Toggle (Real vs All) */}
          <div
            style={{
              display: 'flex',
              backgroundColor: 'var(--color-bg-subtle)',
              padding: '0.25rem',
              borderRadius: 'var(--radius-md)',
              border: '1px solid var(--color-border-subtle)',
            }}
          >
            <button
              type="button"
              onClick={() => onDataFilterChange('real')}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '0.375rem',
                padding: '0.375rem 0.75rem',
                fontSize: '0.8125rem',
                fontWeight: 600,
                borderRadius: 'var(--radius-sm)',
                border: 'none',
                cursor: 'pointer',
                backgroundColor: dataFilter === 'real' ? 'var(--color-primary)' : 'transparent',
                color: dataFilter === 'real' ? '#0b0f19' : 'var(--color-text-secondary)',
                transition: 'all 0.15s ease',
              }}
            >
              <ShieldCheck size={14} />
              <span>Verified Only</span>
            </button>

            <button
              type="button"
              onClick={() => onDataFilterChange('all')}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '0.375rem',
                padding: '0.375rem 0.75rem',
                fontSize: '0.8125rem',
                fontWeight: 600,
                borderRadius: 'var(--radius-sm)',
                border: 'none',
                cursor: 'pointer',
                backgroundColor: dataFilter === 'all' ? 'var(--color-primary)' : 'transparent',
                color: dataFilter === 'all' ? '#0b0f19' : 'var(--color-text-secondary)',
                transition: 'all 0.15s ease',
              }}
            >
              <Layers size={14} />
              <span>All Data</span>
            </button>
          </div>

          {/* Sort Dropdown */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <ArrowUpDown size={15} style={{ color: 'var(--color-text-muted)' }} />
            <select
              className="input"
              value={sort}
              onChange={(e) => onSortChange(e.target.value as LeaderboardSort)}
              style={{
                fontSize: '0.8125rem',
                padding: '0.45rem 2rem 0.45rem 0.75rem',
                cursor: 'pointer',
              }}
            >
              <option value="score">Sort by: Score (WDS)</option>
              <option value="solved">Sort by: Total Solved</option>
              <option value="rating">Sort by: Best Rating</option>
            </select>
          </div>
        </div>
      </div>

      {/* Contextual Notice */}
      <div
        style={{
          fontSize: '0.8125rem',
          color: 'var(--color-text-muted)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          flexWrap: 'wrap',
          gap: '0.5rem',
          padding: '0.5rem 0.75rem',
          backgroundColor: 'rgba(255, 255, 255, 0.02)',
          borderRadius: 'var(--radius-sm)',
          borderLeft: dataFilter === 'real' ? '3px solid var(--color-primary)' : '3px solid #eab308',
        }}
      >
        <span>
          {dataFilter === 'real'
            ? '✓ Verified Only: Rankings computed exclusively from live-synced platforms (LeetCode & Codeforces). Mock platforms excluded.'
            : '⚠ All Data: Includes self-reported or unverified mock platform submissions marked with badges below.'}
        </span>
        {college && (
          <span style={{ color: 'var(--color-primary)', fontWeight: 600 }}>
            Filtered to "{college}"
          </span>
        )}
      </div>
    </div>
  );
};

import React from 'react';
import { CONTEST_PLATFORMS } from '../../utils/contestCalendarMonth';

interface PlatformFilterPillsProps {
  selected: readonly string[];
  onToggle: (code: string) => void;
  onSelectAll: () => void;
}

export const PlatformFilterPills: React.FC<PlatformFilterPillsProps> = ({
  selected,
  onToggle,
  onSelectAll,
}) => {
  const allActive = selected.length === 0;

  return (
    <div className="platform-pills" role="group" aria-label="Filter contests by platform">
      <button
        type="button"
        className={`platform-pill${allActive ? ' active' : ''}`}
        aria-pressed={allActive}
        onClick={onSelectAll}
      >
        All
      </button>
      {CONTEST_PLATFORMS.map((platform) => {
        const active = selected.includes(platform.code);
        return (
          <button
            key={platform.code}
            type="button"
            className={`platform-pill${active ? ' active' : ''}`}
            style={{ '--platform-accent': platform.accent } as React.CSSProperties}
            aria-pressed={active}
            onClick={() => onToggle(platform.code)}
          >
            <span className="platform-pill-dot" aria-hidden="true" />
            {platform.label}
          </button>
        );
      })}
    </div>
  );
};

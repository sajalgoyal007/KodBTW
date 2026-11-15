import React, { useEffect, useRef } from 'react';
import { ExternalLink, X } from 'lucide-react';
import { ContestMonthEvent } from '../../types/contestCalendarMonth';
import {
  formatContestDateTime,
  formatContestDuration,
  formatDayLabel,
  formatStatusLabel,
  getPlatformAccent,
  isSafeHttpUrl,
} from '../../utils/contestCalendarMonth';

interface ContestDayDetailsPanelProps {
  dateKey: string;
  contests: readonly ContestMonthEvent[];
  onClose: () => void;
}

export const ContestDayDetailsPanel: React.FC<ContestDayDetailsPanelProps> = ({
  dateKey,
  contests,
  onClose,
}) => {
  const closeButtonRef = useRef<HTMLButtonElement>(null);
  const panelRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const previouslyFocused = document.activeElement as HTMLElement | null;
    closeButtonRef.current?.focus();

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        event.stopPropagation();
        onClose();
      }
      if (event.key !== 'Tab' || !panelRef.current) return;

      const focusable = panelRef.current.querySelectorAll<HTMLElement>(
        'a[href], button:not([disabled]), [tabindex]:not([tabindex="-1"])',
      );
      if (focusable.length === 0) return;
      const first = focusable[0];
      const last = focusable[focusable.length - 1];
      if (event.shiftKey && document.activeElement === first) {
        event.preventDefault();
        last.focus();
      } else if (!event.shiftKey && document.activeElement === last) {
        event.preventDefault();
        first.focus();
      }
    };

    document.addEventListener('keydown', handleKeyDown);
    return () => {
      document.removeEventListener('keydown', handleKeyDown);
      previouslyFocused?.focus?.();
    };
  }, [onClose]);

  const handleBackdropClick = (event: React.MouseEvent<HTMLDivElement>) => {
    if (event.target === event.currentTarget) onClose();
  };

  return (
    <div
      className="contest-day-backdrop"
      onClick={handleBackdropClick}
      data-testid="contest-day-backdrop"
    >
      <div
        ref={panelRef}
        className="contest-day-panel"
        role="dialog"
        aria-modal="true"
        aria-labelledby="contest-day-panel-title"
      >
        <div className="contest-day-panel-header">
          <div>
            <h2 id="contest-day-panel-title">{formatDayLabel(dateKey)}</h2>
            <p className="contest-day-panel-subtitle">
              {contests.length} contest{contests.length === 1 ? '' : 's'} scheduled
            </p>
          </div>
          <button
            ref={closeButtonRef}
            type="button"
            className="contest-day-panel-close"
            onClick={onClose}
            aria-label="Close day details"
          >
            <X size={18} />
          </button>
        </div>

        {contests.length === 0 ? (
          <p className="contest-day-panel-empty">No contests are scheduled on this day.</p>
        ) : (
          <ul className="contest-day-list">
            {contests.map((contest) => (
              <li key={contest.id} className="contest-day-item">
                <div className="contest-day-item-heading">
                  <h3>{contest.title}</h3>
                  <span
                    className="contest-platform-badge"
                    style={{ '--platform-accent': getPlatformAccent(contest.platform) } as React.CSSProperties}
                  >
                    {contest.platformDisplayName}
                  </span>
                </div>

                <div className="contest-day-item-meta">
                  <span className="contest-day-item-datetime">
                    {formatContestDateTime(contest.startTimeUtc)}
                  </span>
                  <span className="contest-day-item-duration">
                    {formatContestDuration(contest.durationSeconds)}
                  </span>
                  <span className={`contest-status-chip is-${contest.status.toLowerCase()}`}>
                    {formatStatusLabel(contest.status)}
                  </span>
                </div>

                {isSafeHttpUrl(contest.contestUrl) ? (
                  <a
                    className="btn btn-primary contest-day-item-link"
                    href={contest.contestUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    aria-label={`Go to ${contest.title} (opens in a new tab)`}
                  >
                    Go to Contest <span aria-hidden="true">↗</span>
                    <ExternalLink size={14} aria-hidden="true" />
                  </a>
                ) : (
                  <span className="contest-day-item-unavailable">Official link unavailable</span>
                )}
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
};

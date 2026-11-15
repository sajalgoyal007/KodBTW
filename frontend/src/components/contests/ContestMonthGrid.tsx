import React from 'react';
import { ContestMonthEvent } from '../../types/contestCalendarMonth';
import {
  buildMonthWeeks,
  getDayOfMonth,
  getPlatformAccent,
  getWeekdayLabels,
} from '../../utils/contestCalendarMonth';

interface ContestMonthGridProps {
  year: number;
  month: number;
  contestsByDate: Map<string, ContestMonthEvent[]>;
  todayKey: string;
  onSelectDay: (dateKey: string) => void;
}

const MAX_VISIBLE_PILLS = 2;

export const ContestMonthGrid: React.FC<ContestMonthGridProps> = ({
  year,
  month,
  contestsByDate,
  todayKey,
  onSelectDay,
}) => {
  const weeks = buildMonthWeeks(year, month);
  const weekdays = getWeekdayLabels();

  return (
    <div className="contest-month-grid" role="grid" aria-label="Contest calendar by day">
      <div className="contest-month-weekdays" role="rowgroup">
        {weekdays.map((label) => (
          <div key={label} className="contest-month-weekday" role="columnheader" aria-label={`${label} (column header)`}>
            {label}
          </div>
        ))}
      </div>

      <div className="contest-month-weeks" role="rowgroup">
        {weeks.map((week, weekIndex) => (
          <div key={`week-${weekIndex}`} className="contest-month-week" role="row">
            {week.map((dateKey, dayIndex) => {
              if (!dateKey) {
                return (
                  <div
                    key={`pad-${weekIndex}-${dayIndex}`}
                    className="contest-day-cell is-empty"
                    role="gridcell"
                    aria-hidden="true"
                  />
                );
              }

              const dayContests = contestsByDate.get(dateKey) ?? [];
              const visible = dayContests.slice(0, MAX_VISIBLE_PILLS);
              const hiddenCount = dayContests.length - visible.length;
              const dayLabel = `${getDayOfMonth(dateKey)}`;
              const summary = dayContests.length === 0
                ? 'no contests'
                : `${dayContests.length} contest${dayContests.length === 1 ? '' : 's'}`;

              return (
                <div
                  key={dateKey}
                  className={`contest-day-cell${dateKey === todayKey ? ' is-today' : ''}${dayContests.length > 0 ? ' has-contests' : ''}`}
                  role="gridcell"
                >
                  <button
                    type="button"
                    className="contest-day-button"
                    onClick={() => onSelectDay(dateKey)}
                    aria-label={`${dateKey}: ${summary}. Open day details.`}
                  >
                    <span className="contest-day-number">{dayLabel}</span>
                    {dateKey === todayKey && <span className="contest-day-today">Today</span>}
                  </button>

                  {dayContests.length > 0 && (
                    <div className="contest-day-pills">
                      {visible.map((contest) => (
                        <button
                          key={contest.id}
                          type="button"
                          className="contest-day-pill"
                          style={{ '--platform-accent': getPlatformAccent(contest.platform) } as React.CSSProperties}
                          onClick={() => onSelectDay(dateKey)}
                          title={`${contest.platformDisplayName}: ${contest.title}`}
                        >
                          <span className="contest-day-pill-dot" aria-hidden="true" />
                          <span className="contest-day-pill-label">{contest.title}</span>
                        </button>
                      ))}
                      {hiddenCount > 0 && (
                        <button
                          type="button"
                          className="contest-day-more"
                          onClick={() => onSelectDay(dateKey)}
                        >
                          +{hiddenCount} more
                        </button>
                      )}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        ))}
      </div>
    </div>
  );
};

import React, { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { AlertCircle, CalendarDays, ChevronLeft, ChevronRight, RefreshCw, Trophy } from 'lucide-react';
import { AppNavbar } from '../components/layout/AppNavbar';
import { PlatformFilterPills } from '../components/contests/PlatformFilterPills';
import { ContestMonthGrid } from '../components/contests/ContestMonthGrid';
import { ContestDayDetailsPanel } from '../components/contests/ContestDayDetailsPanel';
import { useContestCalendarMonth } from '../hooks/useContestCalendarMonth';
import {
  filterContestsByPlatform,
  formatMonthLabel,
  getCurrentMonth,
  getTodayDateKey,
  groupContestsByDate,
  shiftMonth,
} from '../utils/contestCalendarMonth';

export const ContestCalendarPage: React.FC = () => {
  const initialMonth = useMemo(() => getCurrentMonth(), []);
  const [year, setYear] = useState(initialMonth.year);
  const [month, setMonth] = useState(initialMonth.month);
  const [selectedPlatforms, setSelectedPlatforms] = useState<string[]>([]);
  const [selectedDay, setSelectedDay] = useState<string | null>(null);

  const { response, loading, error, refetch } = useContestCalendarMonth(year, month);
  const todayKey = getTodayDateKey();

  useEffect(() => {
    setSelectedDay(null);
  }, [year, month]);

  const allContests = response?.contests ?? [];
  const visibleContests = useMemo(
    () => filterContestsByPlatform(allContests, selectedPlatforms),
    [allContests, selectedPlatforms],
  );
  const contestsByDate = useMemo(() => groupContestsByDate(visibleContests), [visibleContests]);

  const goToPreviousMonth = () => {
    const next = shiftMonth(year, month, -1);
    setYear(next.year);
    setMonth(next.month);
  };

  const goToNextMonth = () => {
    const next = shiftMonth(year, month, 1);
    setYear(next.year);
    setMonth(next.month);
  };

  const goToCurrentMonth = () => {
    const current = getCurrentMonth();
    setYear(current.year);
    setMonth(current.month);
  };

  const togglePlatform = (code: string) => {
    setSelectedPlatforms((current) => (
      current.includes(code) ? current.filter((item) => item !== code) : [...current, code]
    ));
  };

  const selectedDayContests = selectedDay ? (contestsByDate.get(selectedDay) ?? []) : [];
  const activeFilterCount = selectedPlatforms.length;
  const isEmpty = !loading && !error && visibleContests.length === 0;

  return (
    <div className="contest-calendar-page">
      <AppNavbar />

      <main className="contest-calendar-main">
        <div className="contest-calendar-topbar">
          <div>
            <div className="contest-calendar-page-title">
              <h1>Contest Calendar</h1>
              {!loading && !error && (
                <div className="badge badge-primary">
                  <Trophy size={12} />
                  <span>
                    {visibleContests.length} contest{visibleContests.length === 1 ? '' : 's'}
                  </span>
                </div>
              )}
            </div>
            <p className="contest-calendar-page-subtitle">
              Codeforces, LeetCode, CodeChef, AtCoder, GFG, Code360, HackerRank and HackerEarth
              contests, shown in India Standard Time (IST).
            </p>
          </div>

          <div className="contest-calendar-topbar-actions">
            <Link to="/contests" className="btn btn-secondary contest-calendar-back-link">
              <Trophy size={14} aria-hidden="true" />
              <span>My Contest Stats</span>
            </Link>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => refetch()}
              disabled={loading}
            >
              <RefreshCw size={14} className={loading ? 'spinner' : ''} aria-hidden="true" />
              <span>Refresh</span>
            </button>
          </div>
        </div>

        <div className="contest-month-toolbar">
          <div className="contest-month-nav">
            <button type="button" className="btn btn-secondary" onClick={goToPreviousMonth}>
              <ChevronLeft size={16} aria-hidden="true" />
              <span>Prev</span>
            </button>
            <button type="button" className="btn btn-secondary" onClick={goToCurrentMonth}>
              Today
            </button>
            <button type="button" className="btn btn-secondary" onClick={goToNextMonth}>
              <span>Next</span>
              <ChevronRight size={16} aria-hidden="true" />
            </button>
            <h2 className="contest-month-label" aria-live="polite">
              {formatMonthLabel(year, month)}
            </h2>
          </div>

          <PlatformFilterPills
            selected={selectedPlatforms}
            onToggle={togglePlatform}
            onSelectAll={() => setSelectedPlatforms([])}
          />
        </div>

        {loading && (
          <div className="contest-month-grid contest-month-grid-skeleton" aria-hidden="true">
            {Array.from({ length: 42 }, (_, index) => (
              <div key={index} className="contest-day-cell skeleton" />
            ))}
          </div>
        )}

        {!loading && error && (
          <div className="card contest-calendar-state contest-calendar-state-error" role="alert">
            <AlertCircle size={28} />
            <div>
              <h2>Unable to Load Contests</h2>
              <p>{error}</p>
            </div>
            <button type="button" className="btn btn-primary" onClick={() => refetch()}>
              <RefreshCw size={16} aria-hidden="true" /> Retry
            </button>
          </div>
        )}

        {!loading && !error && (
          <>
            {isEmpty ? (
              <div className="card contest-calendar-state">
                <CalendarDays size={28} />
                <div>
                  <h2>No Contests Found</h2>
                  <p>
                    {activeFilterCount > 0
                      ? 'No contests match the selected platforms this month. Try another filter or month.'
                      : 'There are no contests scheduled for this month yet. Try the previous or next month.'}
                  </p>
                </div>
                {activeFilterCount > 0 ? (
                  <button
                    type="button"
                    className="btn btn-secondary"
                    onClick={() => setSelectedPlatforms([])}
                  >
                    Show All Platforms
                  </button>
                ) : (
                  <button type="button" className="btn btn-secondary" onClick={goToCurrentMonth}>
                    Back to This Month
                  </button>
                )}
              </div>
            ) : (
              <>
                {activeFilterCount > 0 && (
                  <p className="contest-calendar-filter-note">
                    Showing {visibleContests.length} of {allContests.length} contests for the selected
                    platform{activeFilterCount === 1 ? '' : 's'}.
                  </p>
                )}
                <ContestMonthGrid
                  year={year}
                  month={month}
                  contestsByDate={contestsByDate}
                  todayKey={todayKey}
                  onSelectDay={setSelectedDay}
                />
              </>
            )}
          </>
        )}
      </main>

      {selectedDay && (
        <ContestDayDetailsPanel
          dateKey={selectedDay}
          contests={selectedDayContests}
          onClose={() => setSelectedDay(null)}
        />
      )}
    </div>
  );
};

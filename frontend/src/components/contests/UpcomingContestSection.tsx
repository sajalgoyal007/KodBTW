import React, { useEffect, useState } from 'react';
import { AlertCircle, CalendarDays, Clock3, ExternalLink, RefreshCw } from 'lucide-react';
import { useContestCalendar } from '../../hooks/useContestCalendar';
import { ContestPlatformFilter, ContestTimeFilter, filterAndSortContests, formatContestDuration, formatContestLocalTime, getContestCalendarViewState, getContestCountdown } from '../../utils/contestCalendar';

export const UpcomingContestSection: React.FC = () => {
  const { response, loading, error, refetch } = useContestCalendar();
  const [platformFilter, setPlatformFilter] = useState<ContestPlatformFilter>('ALL');
  const [timeFilter, setTimeFilter] = useState<ContestTimeFilter>('ALL_UPCOMING');
  const [now, setNow] = useState(Date.now());

  useEffect(() => {
    const timer = window.setInterval(() => setNow(Date.now()), 1_000);
    return () => window.clearInterval(timer);
  }, []);

  const contests = filterAndSortContests(response?.contests ?? [], platformFilter, timeFilter, now);
  const source = response?.sources.find((item) => item.platform === 'CODEFORCES');
  const sourceLabel = source?.source === 'LOCAL_FIXTURE' ? 'Local fixture'
    : source?.source === 'CODEFORCES_API' ? 'Codeforces API' : null;
  const sourceUnavailable = source?.status === 'UNAVAILABLE';
  const stale = response?.stale || source?.status === 'STALE';
  const viewState = getContestCalendarViewState({ loading, error, response, visibleContestCount: contests.length });

  return (
    <section className="contest-calendar-section" aria-labelledby="upcoming-contests-title">
      <div className="contest-calendar-heading">
        <div>
          <div className="contest-calendar-title">
            <CalendarDays size={19} aria-hidden="true" />
            <h2 id="upcoming-contests-title">Upcoming Contests</h2>
          </div>
          <p>Contest times are shown in your local timezone.</p>
        </div>
        <div className="contest-calendar-controls">
          <div className="contest-calendar-filter">
            <label htmlFor="contest-platform-filter">Platform</label>
            <select id="contest-platform-filter" value={platformFilter}
              onChange={(event) => setPlatformFilter(event.target.value as ContestPlatformFilter)}>
              <option value="ALL">All available</option>
              <option value="CODEFORCES">Codeforces</option>
            </select>
          </div>
          <div className="contest-calendar-filter">
            <label htmlFor="contest-time-filter">When</label>
            <select id="contest-time-filter" value={timeFilter}
              onChange={(event) => setTimeFilter(event.target.value as ContestTimeFilter)}>
              <option value="ALL_UPCOMING">All upcoming</option>
              <option value="NEXT_7_DAYS">Next 7 days</option>
              <option value="NEXT_30_DAYS">Next 30 days</option>
            </select>
          </div>
          <button type="button" className="btn btn-secondary" onClick={() => void refetch()} disabled={loading} aria-label="Refresh upcoming contests">
            <RefreshCw size={14} className={loading ? 'spinner' : ''} /> Refresh
          </button>
        </div>
      </div>

      {stale && <div className="contest-calendar-notice" role="status">
        Showing cached contest times. The latest schedule could not be refreshed.
      </div>}
      {viewState === 'unavailable' && sourceUnavailable && (
        <div className="contest-calendar-state error" role="alert">
          <AlertCircle size={18} aria-hidden="true" />
          <span>{source.message || 'Contest schedule is temporarily unavailable.'}</span>
          <button type="button" className="btn btn-secondary" onClick={() => void refetch()} disabled={loading}>Retry</button>
        </div>
      )}
      {viewState === 'error' && error && (
        <div className="contest-calendar-state error" role="alert">
          <AlertCircle size={18} aria-hidden="true" /><span>{error}</span>
          <button type="button" className="btn btn-secondary" onClick={() => void refetch()} disabled={loading}>Retry</button>
        </div>
      )}
      {viewState === 'loading' && <div className="contest-calendar-state" role="status">Loading contest schedule…</div>}
      {viewState === 'empty' && (
        <div className="contest-calendar-state" role="status">No upcoming contests are currently listed.</div>
      )}
      {contests.length > 0 && (
        <div className="contest-calendar-list">
          {contests.map((contest) => {
            const localTime = formatContestLocalTime(contest.startTime);
            const countdown = getContestCountdown(contest, now);
            const platformName = contest.platform === 'CODEFORCES' ? 'Codeforces' : contest.platform;
            return (
              <article className="contest-calendar-card" key={`${contest.platform}:${contest.id}`}>
                <div className="contest-calendar-card-main">
                  <div className="contest-calendar-card-heading">
                    <span className="contest-calendar-platform">{platformName}</span><h3>{contest.name}</h3>
                  </div>
                  <p className="contest-calendar-datetime">
                    <CalendarDays size={15} aria-hidden="true" />
                    <time dateTime={contest.startTime}>{localTime.dateTime}</time>
                    <span className="contest-calendar-zone">{localTime.timeZone}</span>
                  </p>
                  <div className="contest-calendar-meta">
                    <span><Clock3 size={14} aria-hidden="true" /> {formatContestDuration(contest.durationSeconds)}</span>
                    <span className={`contest-calendar-countdown ${countdown.state}`} aria-live="off">{countdown.label}</span>
                  </div>
                </div>
                <a className="contest-calendar-link" href={contest.officialUrl} target="_blank" rel="noopener noreferrer">
                  Open official contest <ExternalLink size={14} aria-hidden="true" />
                </a>
              </article>
            );
          })}
        </div>
      )}
      {response && <p className="contest-calendar-freshness">
        Last successful update {source?.fetchedAt ? formatContestLocalTime(source.fetchedAt).dateTime : 'unavailable'}
        {source?.fetchedAt && sourceLabel && ` · ${sourceLabel}`}
      </p>}
    </section>
  );
};

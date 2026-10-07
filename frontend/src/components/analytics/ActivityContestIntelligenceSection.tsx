import React from 'react';
import { Activity, AlertCircle, Clock3, RefreshCw, Trophy } from 'lucide-react';
import { useActivityContestIntelligence } from '../../hooks/useActivityContestIntelligence';

export const ActivityContestIntelligenceSection: React.FC = () => {
  const { activity, contests, loading, error, refetch } = useActivityContestIntelligence();

  return (
    <section className="activity-contest-section" aria-labelledby="activity-contest-heading">
      <div className="activity-contest-heading">
        <div>
          <h2 id="activity-contest-heading">Activity & Contest Intelligence</h2>
          <p>Only dated records and persisted rating observations are shown.</p>
        </div>
        {contests?.lastUpdatedAt && <span className="activity-contest-freshness"><Clock3 size={14} />Updated {new Date(contests.lastUpdatedAt).toLocaleString()}</span>}
      </div>

      {loading && <div className="card activity-contest-state" aria-live="polite"><RefreshCw size={18} className="spinner" /> Loading recorded activity and ratings…</div>}

      {!loading && error && <div className="card activity-contest-state error" role="alert">
        <AlertCircle size={19} /><span>{error}</span><button type="button" className="btn btn-secondary" onClick={refetch}>Retry</button>
      </div>}

      {!loading && !error && activity && contests && (
        <div className="activity-contest-grid">
          <article className="card activity-contest-card">
            <div className="activity-contest-card-title"><Activity size={18} /><h3>Activity history</h3></div>
            <p className="activity-contest-unavailable">{activity.message}</p>
            {activity.platformsWithRealSnapshots.length > 0 && <p className="activity-contest-note">
              REAL snapshots exist for {activity.platformsWithRealSnapshots.join(', ')}, but these integrations do not currently persist dated submission or activity events. Snapshot dates are not treated as activity.
            </p>}
          </article>

          <article className="card activity-contest-card">
            <div className="activity-contest-card-title"><Trophy size={18} /><h3>Contest history</h3></div>
            <p className="activity-contest-unavailable">{contests.contestHistoryMessage}</p>
            {contests.platforms.length > 0 ? (
              <>
                <p className="activity-contest-note">The rating values below are dated platform-stat snapshots, not contest events.</p>
                <div className="contest-rating-list">
                  {contests.platforms.map((platform) => (
                    <div className="contest-rating-row" key={platform.platform}>
                      <div><strong>{platform.platform}</strong><span>{platform.observationCount} rating observation{platform.observationCount === 1 ? '' : 's'} · {platform.firstObservationDate} to {platform.latestObservationDate}</span></div>
                      <div className="contest-rating-values">
                        <span>Latest snapshot {platform.latestSnapshotDate} <b>{platform.currentRating ?? 'Rating unavailable'}</b></span>
                        <span>High <b>{platform.highestObservedRating}</b></span>
                        <span>Low <b>{platform.lowestObservedRating}</b></span>
                        {platform.change !== null && <span>Observed change <b className={platform.change < 0 ? 'negative' : platform.change > 0 ? 'positive' : ''}>{platform.change > 0 ? '+' : ''}{platform.change}</b></span>}
                      </div>
                    </div>
                  ))}
                </div>
              </>
            ) : <p className="activity-contest-note">No REAL persisted rating observations are available yet.</p>}
          </article>
        </div>
      )}
    </section>
  );
};

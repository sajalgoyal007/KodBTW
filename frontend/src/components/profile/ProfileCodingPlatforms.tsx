import React from 'react';
import { Link } from 'react-router-dom';
import { ExternalLink, RefreshCw } from 'lucide-react';
import { getAccountConnectablePlatforms, getPlatformCapability, getPlatformConnectionStatus, getUnavailablePlatforms } from '../../config/platformCapabilities';
import { usePlatformAccounts } from '../../hooks/usePlatformAccounts';
import type { PlatformAccountResponse } from '../../types/platform';
import { getPlatformAvailabilityLabel, getPlatformStatsSourceLabel, isVerifiedStatsSource } from '../../utils/platformStatsSource';
import type { PlatformStats } from '../../types/dashboard';
import { getProfileAchievements, getProfileSyncMessage, getSupportedProfileMetrics } from '../../utils/profileStatisticsPresentation';

interface ProfileCodingPlatformsProps {
  platformStats?: PlatformStats[];
  statisticsLoading?: boolean;
  statisticsError?: string | null;
  onRetryStatistics?: () => void;
}

function formatDate(value: string | null): string | null {
  if (!value) return null;
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? null : date.toLocaleDateString(undefined, {
    month: 'short', day: 'numeric', year: 'numeric',
  });
}

function findAccount(accounts: PlatformAccountResponse[], platform: string): PlatformAccountResponse | undefined {
  return accounts.find((account) => account.platform === platform);
}

export const ProfileCodingPlatforms: React.FC<ProfileCodingPlatformsProps> = ({
  platformStats = [],
  statisticsLoading = false,
  statisticsError = null,
  onRetryStatistics,
}) => {
  const { accounts, loading, error, fetchAccounts } = usePlatformAccounts();
  const platforms = getAccountConnectablePlatforms();
  const unavailablePlatforms = getUnavailablePlatforms();
  const statisticsByPlatform = new Map(platformStats.map((stats) => [stats.platform, stats]));
  const achievements = getProfileAchievements(platformStats
    .filter((stats) => isVerifiedStatsSource(stats.source))
    .map((stats) => ({
      displayName: getPlatformCapability(stats.platform)?.displayName ?? 'Coding platform',
      totalProblemsSolved: stats.totalProblemsSolved,
    })), accounts.length);

  return (
    <section className="profile-platform-section" aria-labelledby="profile-platform-title">
      <div className="profile-section-heading">
        <div>
          <h2 id="profile-platform-title">Coding platform profiles</h2>
          <p>Saved handles and data availability. Handle ownership is not verified by KodBTW.</p>
        </div>
        <Link className="btn btn-secondary" to="/coding-profiles">Manage profiles</Link>
      </div>

      {loading && accounts.length === 0 && (
        <p className="profile-platform-message" role="status">Loading your saved coding profiles…</p>
      )}

      {error && (
        <div className="profile-platform-message profile-platform-message--error" role="alert">
          <span>{error}{accounts.length > 0 ? ' Previously loaded profiles are still shown.' : ''}</span>
          <button type="button" className="btn btn-ghost" onClick={() => void fetchAccounts()}>
            <RefreshCw size={14} aria-hidden="true" /> Retry
          </button>
        </div>
      )}

      {statisticsError && (
        <div className="profile-platform-message profile-platform-message--error" role="alert">
          <span>{statisticsError} Saved platform connections remain available.</span>
          {onRetryStatistics && <button type="button" className="btn btn-ghost" onClick={onRetryStatistics}>
            <RefreshCw size={14} aria-hidden="true" /> Retry statistics
          </button>}
        </div>
      )}

      <div className="profile-platform-grid">
        {platforms.map((capability) => {
          const account = findAccount(accounts, capability.accountPlatform);
          const profileUrl = account && capability.profileUrlFor
            ? capability.profileUrlFor(account.username)
            : null;
          const connectionStatus = getPlatformConnectionStatus(capability.id, account?.sourceStatus);
          const lastSync = account ? formatDate(account.lastSuccessAt) : null;
          const stats = account ? statisticsByPlatform.get(account.platform) : undefined;
          const supportedMetrics = getSupportedProfileMetrics(
            stats,
            capability.supportedMetrics,
            Boolean(stats && isVerifiedStatsSource(stats.source)),
          );

          return (
            <article className="card profile-platform-card" key={capability.id}>
              <div className="profile-platform-card-heading">
                <h3>{capability.displayName}</h3>
                <span className={`badge ${account ? 'badge-primary' : 'badge-muted'}`}>
                  {account ? 'Handle saved' : 'Not connected'}
                </span>
              </div>
              {account ? (
                <>
                  <p className="profile-platform-username">@{account.username} <span>· ownership unverified</span></p>
                  {profileUrl && (
                    <a className="profile-platform-link" href={profileUrl} target="_blank" rel="noopener noreferrer">
                      Open {capability.displayName} profile <ExternalLink size={13} aria-hidden="true" />
                    </a>
                  )}
                  <p className="profile-platform-status">{connectionStatus}</p>
                  <p className="profile-platform-last-sync">{getProfileSyncMessage(account, Boolean(stats?.lastSyncedAt))}</p>
                  {lastSync && <p className="profile-platform-last-sync">Last successful sync · {lastSync}</p>}
                  <div className="profile-platform-source" style={{ marginTop: '.65rem' }}>
                    Statistics source: {stats ? getPlatformStatsSourceLabel(stats.source) : 'No saved snapshot'}
                  </div>
                  {statisticsLoading && <p className="profile-platform-status" role="status">
                    {stats ? 'Updating statistics; previously loaded values remain visible.' : 'Loading saved statistics…'}
                  </p>}
                  {supportedMetrics.length > 0 ? (
                    <dl style={{ display: 'grid', gridTemplateColumns: 'repeat(2, minmax(0, 1fr))', gap: '.5rem', margin: '.75rem 0 0' }}>
                      {supportedMetrics.map((metric) => (
                        <div key={metric.key} style={{ minWidth: 0, padding: '.55rem .65rem', borderRadius: 'var(--radius-md)', background: 'var(--color-bg-base)', border: '1px solid var(--color-border-subtle)' }}>
                          <dt style={{ color: 'var(--color-text-muted)', fontSize: '.7rem' }}>{metric.label}</dt>
                          <dd className="mono" style={{ margin: '.2rem 0 0', color: 'var(--color-text-primary)', fontWeight: 700 }}>
                            {metric.value === null ? '—' : metric.value.toLocaleString()}
                          </dd>
                        </div>
                      ))}
                    </dl>
                  ) : !statisticsError && !statisticsLoading ? (
                    <p className="profile-platform-status" style={{ marginTop: '.65rem' }}>
                      {stats && isVerifiedStatsSource(stats.source)
                        ? 'This source has not reported supported metrics yet.'
                        : 'No eligible statistics snapshot is available yet.'}
                    </p>
                  ) : null}
                </>
              ) : (
                <p className="profile-platform-status">{getPlatformAvailabilityLabel(capability.id)}</p>
              )}
              <p className="profile-platform-capability">{capability.availabilityMessage}</p>
              <p className="profile-platform-source">Source: {capability.sourceDescription}</p>
            </article>
          );
        })}
      </div>

      <section aria-labelledby="profile-achievements-title" style={{ marginTop: '1.25rem' }}>
        <div className="profile-section-heading" style={{ marginBottom: '.75rem' }}>
          <div>
            <h2 id="profile-achievements-title">Data-backed milestones</h2>
            <p>Milestones use saved profile counts and eligible source-reported solves; they are not contest awards.</p>
          </div>
        </div>
        {loading || error || statisticsLoading || statisticsError ? (
          <p className="profile-platform-message" role="status">Milestones are shown when saved profiles and statistics are available.</p>
        ) : (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(min(100%, 210px), 1fr))', gap: '.75rem' }}>
            {achievements.map((achievement) => (
              <article key={achievement.id} className="card" style={{ padding: '1rem', borderColor: achievement.unlocked ? 'var(--color-success-border)' : 'var(--color-border-subtle)' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '.5rem' }}>
                  <span aria-hidden="true" style={{ color: achievement.unlocked ? 'var(--color-success)' : 'var(--color-text-muted)' }}>{achievement.unlocked ? '✓' : '○'}</span>
                  <h3 style={{ fontSize: '.875rem', fontWeight: 700 }}>{achievement.title}</h3>
                </div>
                <p style={{ marginTop: '.45rem', color: 'var(--color-text-secondary)', fontSize: '.78rem', lineHeight: 1.5 }}>{achievement.description}</p>
                {achievement.evidence && <p style={{ marginTop: '.45rem', color: 'var(--color-text-muted)', fontSize: '.72rem' }}>{achievement.evidence}</p>}
                <span className="sr-only">{achievement.unlocked ? 'Achieved' : 'Not achieved'}</span>
              </article>
            ))}
          </div>
        )}
      </section>

      {unavailablePlatforms.length > 0 && (
        <p className="profile-platform-unavailable">
          Not available for connection: {unavailablePlatforms.map((platform) => platform.displayName).join(' · ')}.
        </p>
      )}
    </section>
  );
};

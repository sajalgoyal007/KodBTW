import React, { useState, useEffect, useCallback, useRef } from 'react';
import { Link } from 'react-router-dom';
import { AppNavbar } from '../components/layout/AppNavbar';
import { useAuth } from '../hooks/useAuth';
import { useProfile } from '../hooks/useProfile';
import { ProfileEditModal } from '../components/profile/ProfileEditModal';
import { ShareProfileModal } from '../components/profile/ShareProfileModal';
import { GoogleAccountConnection } from '../components/profile/GoogleAccountConnection';
import { ProfileCodingPlatforms } from '../components/profile/ProfileCodingPlatforms';
import { MetricCard } from '../components/dashboard/MetricCard';
import { getPlatformCapability } from '../config/platformCapabilities';
import { getPlatformStatsSourceLabel, getPlatformStatsSourceKind } from '../utils/platformStatsSource';
import { getProfileMetricValue } from '../utils/profileStatistics';
import { getStats } from '../services/api/dashboardApi';
import { DashboardStatsResponse } from '../types/dashboard';
import { ProfileRequest } from '../types/profile';
import {
  MapPin,
  GraduationCap,
  Calendar,
  ExternalLink,
  Edit2,
  Share2,
  UserCheck,
  CheckCircle2,
  Trophy,
  Flame,
  Layers,
  Github,
  Linkedin,
  Globe,
  PlusCircle,
  AlertCircle,
  RefreshCw,
  TrendingUp,
  ArrowUpRight,
} from 'lucide-react';

export const ProfilePage: React.FC = () => {
  const { user } = useAuth();
  const { profile, loading, saving, error, isInitialSetup, fetchProfile, saveProfile } = useProfile();

  const [dashboardStats, setDashboardStats] = useState<DashboardStatsResponse | null>(null);
  const [dashboardStatsLoading, setDashboardStatsLoading] = useState(true);
  const [dashboardStatsError, setDashboardStatsError] = useState<string | null>(null);
  const [isEditModalOpen, setIsEditModalOpen] = useState<boolean>(false);
  const [isShareModalOpen, setIsShareModalOpen] = useState<boolean>(false);
  const statsRequestId = useRef(0);

  const loadDashboardStats = useCallback(async () => {
    const requestId = ++statsRequestId.current;
    setDashboardStatsLoading(true);
    setDashboardStatsError(null);
    try {
      const data = await getStats();
      if (requestId === statsRequestId.current) setDashboardStats(data);
    } catch {
      if (requestId === statsRequestId.current) {
        setDashboardStatsError('Coding statistics could not be updated. Previously loaded values, if any, remain visible.');
      }
    } finally {
      if (requestId === statsRequestId.current) setDashboardStatsLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadDashboardStats();
    return () => { statsRequestId.current += 1; };
  }, [loadDashboardStats]);

  const handleSave = async (data: ProfileRequest) => {
    await saveProfile(data);
  };

  const overview = dashboardStats?.overview;
  const profilesWithStats = dashboardStats?.platforms.filter((platform) => {
    const hasValue = [
      platform.totalProblemsSolved, platform.easySolved, platform.mediumSolved, platform.hardSolved,
      platform.rating, platform.maxRating, platform.rank, platform.contestsParticipated,
      platform.currentStreak, platform.longestStreak,
    ].some((value) => value !== null && value !== undefined);
    const sourceKind = getPlatformStatsSourceKind(platform.source);
    return hasValue && (sourceKind === 'live' || sourceKind === 'third-party');
  }) ?? [];
  const statsCoverage = profilesWithStats.map((platform) => {
    const name = getPlatformCapability(platform.platform)?.displayName ?? 'Coding platform';
    return `${name} (${getPlatformStatsSourceLabel(platform.source)})`;
  });
  const currentStreak = getProfileMetricValue(overview?.currentStreak, overview?.connectedPlatformsCount);
  const longestStreak = getProfileMetricValue(overview?.longestStreak, overview?.connectedPlatformsCount);

  const getInitials = (name: string | null | undefined) => {
    if (!name) return 'U';
    const parts = name.trim().split(/\s+/);
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return parts[0].slice(0, 2).toUpperCase();
  };

  return (
    <div className="profile-page" style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <AppNavbar />

      <main
        className="profile-page-main"
        style={{
          flex: 1,
          maxWidth: '1280px',
          width: '100%',
          margin: '0 auto',
          display: 'flex',
          flexDirection: 'column',
          gap: '2rem',
        }}
      >
        {/* Loading state */}
        {loading && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
            <div className="card skeleton" style={{ height: '220px' }} />
            <div
              style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(210px, 1fr))',
                gap: '1rem',
              }}
            >
              {[1, 2, 3, 4].map((i) => (
                <div key={i} className="card skeleton" style={{ height: '110px' }} />
              ))}
            </div>
          </div>
        )}

        {/* Global Error */}
        {!loading && error && (
          <div className="alert alert-error">
            <AlertCircle size={18} />
            <span style={{ flex: 1 }}>{error}</span>
            <button onClick={() => fetchProfile()} className="btn btn-ghost" style={{ padding: '0.25rem 0.5rem' }}>
              <RefreshCw size={14} /> Retry
            </button>
          </div>
        )}

        {/* Initial Setup Empty State */}
        {!loading && !error && isInitialSetup && (
          <div
            className="card"
            style={{
              padding: '4rem 2rem',
              textAlign: 'center',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: '1.25rem',
              maxWidth: '560px',
              margin: '2rem auto',
            }}
          >
            <div
              style={{
                width: '64px',
                height: '64px',
                borderRadius: '50%',
                backgroundColor: 'var(--color-primary-soft)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: 'var(--color-primary)',
              }}
            >
              <UserCheck size={32} />
            </div>
            <div>
              <h2 style={{ fontSize: '1.5rem', fontWeight: 700, marginBottom: '0.5rem' }}>
                Complete your developer profile
              </h2>
              <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.9375rem', lineHeight: 1.5 }}>
                Add your bio, education, location, and social links to build your public developer identity on KodBTW.
              </p>
            </div>
            <button
              onClick={() => setIsEditModalOpen(true)}
              className="btn btn-primary"
              style={{ padding: '0.75rem 1.5rem', fontSize: '0.9375rem' }}
            >
              <PlusCircle size={18} /> Add Profile Details
            </button>
          </div>
        )}

        {/* Completed Profile Display */}
        {!loading && !error && !isInitialSetup && (
            <div className="card profile-summary-card" style={{ padding: '2rem', position: 'relative' }}>
              <div
                style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'flex-start',
                  flexWrap: 'wrap',
                  gap: '1.5rem',
                }}
              >
                {/* Avatar & Details */}
                <div style={{ display: 'flex', gap: '1.5rem', alignItems: 'flex-start', flexWrap: 'wrap' }}>
                  {/* Avatar */}
                  {profile?.avatarUrl ? (
                    <img
                      src={profile.avatarUrl}
                      alt={profile.displayName || user?.name}
                      style={{
                        width: '88px',
                        height: '88px',
                        borderRadius: '50%',
                        objectFit: 'cover',
                        border: '2px solid var(--color-primary)',
                      }}
                    />
                  ) : (
                    <div
                      style={{
                        width: '88px',
                        height: '88px',
                        borderRadius: '50%',
                        backgroundColor: 'var(--color-primary-soft)',
                        border: '2px solid var(--color-primary)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        fontSize: '1.75rem',
                        fontWeight: 700,
                        color: 'var(--color-primary)',
                      }}
                    >
                      {getInitials(profile?.displayName || user?.name)}
                    </div>
                  )}

                  {/* Name, email, bio, details */}
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', maxWidth: '640px' }}>
                    <div>
                      <h1 style={{ fontSize: '1.625rem', fontWeight: 800, letterSpacing: '-0.02em' }}>
                        {profile?.displayName || user?.name}
                      </h1>
                      {profile?.username && <div className="profile-username">@{profile.username}</div>}
                      <div className="mono" style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)' }}>
                        {user?.email}
                      </div>
                    </div>

                    {profile?.bio && (
                      <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.9375rem', lineHeight: 1.6, marginTop: '0.25rem' }}>
                        {profile.bio}
                      </p>
                    )}

                    {/* Metadata tags */}
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '1rem', marginTop: '0.5rem', fontSize: '0.8125rem', color: 'var(--color-text-secondary)' }}>
                      {profile?.location && (
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.375rem' }}>
                          <MapPin size={14} style={{ color: 'var(--color-primary)' }} />
                          <span>{profile.location}</span>
                        </div>
                      )}
                      {profile?.college && (
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.375rem' }}>
                          <GraduationCap size={14} style={{ color: 'var(--color-info)' }} />
                          <span>{profile.college}</span>
                        </div>
                      )}
                      {profile?.graduationYear && (
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.375rem' }}>
                          <Calendar size={14} style={{ color: 'var(--color-success)' }} />
                          <span>Class of {profile.graduationYear}</span>
                        </div>
                      )}
                    </div>
                  </div>
                </div>

                {/* Action Buttons */}
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', flexWrap: 'wrap' }}>
                  {profile?.username && (
                    <>
                      <a
                        href={`/u/${profile.username}`}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="btn btn-secondary"
                        style={{ padding: '0.5rem 1rem', fontSize: '0.875rem' }}
                      >
                        <ExternalLink size={15} /> Public View
                      </a>
                      <button
                        onClick={() => setIsShareModalOpen(true)}
                        className="btn btn-secondary"
                        style={{ padding: '0.5rem 1rem', fontSize: '0.875rem' }}
                      >
                        <Share2 size={15} /> Share Profile
                      </button>
                    </>
                  )}
                  <button
                    onClick={() => setIsEditModalOpen(true)}
                    className="btn btn-primary"
                    style={{ padding: '0.5rem 1rem', fontSize: '0.875rem' }}
                  >
                    <Edit2 size={15} /> Edit Profile
                  </button>
                </div>
              </div>

              {/* Social links row */}
              {(profile?.githubUrl || profile?.linkedinUrl || profile?.portfolioUrl) && (
                <div
                  style={{
                    display: 'flex',
                    flexWrap: 'wrap',
                    gap: '0.75rem',
                    marginTop: '1.5rem',
                    paddingTop: '1.25rem',
                    borderTop: '1px solid var(--color-border-subtle)',
                  }}
                >
                  {profile.githubUrl && (
                    <a
                      href={profile.githubUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="btn btn-secondary"
                      style={{ padding: '0.375rem 0.75rem', fontSize: '0.8125rem' }}
                    >
                      <Github size={14} /> GitHub <ExternalLink size={11} />
                    </a>
                  )}

                  {profile.linkedinUrl && (
                    <a
                      href={profile.linkedinUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="btn btn-secondary"
                      style={{ padding: '0.375rem 0.75rem', fontSize: '0.8125rem' }}
                    >
                      <Linkedin size={14} /> LinkedIn <ExternalLink size={11} />
                    </a>
                  )}

                  {profile.portfolioUrl && (
                    <a
                      href={profile.portfolioUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="btn btn-secondary"
                      style={{ padding: '0.375rem 0.75rem', fontSize: '0.8125rem' }}
                    >
                      <Globe size={14} /> Portfolio <ExternalLink size={11} />
                    </a>
                  )}
                </div>
              )}
            </div>

        )}

        <GoogleAccountConnection />

        <ProfileCodingPlatforms
          platformStats={dashboardStats?.platforms ?? []}
          statisticsLoading={dashboardStatsLoading}
          statisticsError={dashboardStatsError}
          onRetryStatistics={() => { void loadDashboardStats(); }}
        />

        {/* Coding Stats Highlight section */}
        {!loading && !error && !isInitialSetup && (
            <section className="profile-stats-section" aria-labelledby="profile-stats-title">
              <div className="profile-section-heading">
                <div>
                  <h2 id="profile-stats-title">Coding statistics</h2>
                  <p>Only metrics returned by an eligible data source are included; unavailable values stay blank.</p>
                </div>
                <span className="badge badge-muted">
                  {overview?.connectedPlatformsCount ?? 0} saved profile{overview?.connectedPlatformsCount === 1 ? '' : 's'}
                </span>
              </div>

              <div
                style={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(auto-fit, minmax(210px, 1fr))',
                  gap: '1rem',
                }}
              >
                <MetricCard
                  title="Total Platform Solves"
                  value={getProfileMetricValue(overview?.totalProblemsSolved, overview?.connectedPlatformsCount)}
                  subtitle="Same problem may be counted on multiple platforms."
                  icon={<CheckCircle2 size={20} />}
                  accentColor="var(--color-primary)"
                />
                <MetricCard
                  title="Reported contest participations"
                  value={getProfileMetricValue(overview?.contestsParticipated, overview?.connectedPlatformsCount)}
                  subtitle="Counts may include the same event on multiple platforms."
                  icon={<Trophy size={20} />}
                  accentColor="var(--color-chart-2)"
                />
                <MetricCard
                  title="Current Streak"
                  value={currentStreak === null ? null : `${currentStreak} days`}
                  subtitle="Active daily streak"
                  icon={<Flame size={20} />}
                  accentColor="var(--color-primary)"
                />
                <MetricCard
                  title="Longest Streak"
                  value={longestStreak === null ? null : `${longestStreak} days`}
                  subtitle="Personal record"
                  icon={<Flame size={20} />}
                  accentColor="var(--color-warning)"
                />
                <MetricCard
                  title="Platforms"
                  value={overview?.connectedPlatformsCount}
                  subtitle="Linked services"
                  icon={<Layers size={20} />}
                  accentColor="var(--color-success)"
                />
              </div>

              <p className="profile-statistics-coverage">
                {statsCoverage.length > 0
                  ? `Statistics currently available from: ${statsCoverage.join(' · ')}.`
                  : 'No eligible source has returned statistics yet. Saved handles are shown above; missing data is not counted as zero.'}
              </p>
            </section>
        )}

        {!loading && !error && !isInitialSetup && (
          <section className="profile-more-section" aria-label="Analytics and data availability">
            <Link to="/analytics" className="card profile-analytics-link">
              <span className="profile-analytics-icon" aria-hidden="true"><TrendingUp size={20} /></span>
              <span className="profile-analytics-copy">
                <strong>Rating history &amp; analytics</strong>
                <span>Explore saved daily snapshots, platform comparisons, and difficulty coverage.</span>
              </span>
              <ArrowUpRight size={18} aria-hidden="true" />
            </Link>
            <div className="card profile-activity-unavailable" role="note">
              <h2>Recent activity</h2>
              <p>Submission-level activity is not available from the current platform sources. Rating snapshots are available in Analytics, and deterministic data-backed milestones appear with your coding profiles.</p>
            </div>
          </section>
        )}
      </main>

      {/* Edit Profile Modal */}
      <ProfileEditModal
        isOpen={isEditModalOpen}
        initialProfile={profile}
        isLoading={saving}
        onClose={() => setIsEditModalOpen(false)}
        onSubmit={handleSave}
      />

      {/* Share Profile Modal */}
      {profile?.username && (
        <ShareProfileModal
          isOpen={isShareModalOpen}
          onClose={() => setIsShareModalOpen(false)}
          username={profile.username}
          displayName={profile.displayName}
        />
      )}
    </div>
  );
};

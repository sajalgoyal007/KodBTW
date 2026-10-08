import React, { useState, useEffect } from 'react';
import { AppNavbar } from '../components/layout/AppNavbar';
import { useAuth } from '../hooks/useAuth';
import { useProfile } from '../hooks/useProfile';
import { ProfileEditModal } from '../components/profile/ProfileEditModal';
import { ShareProfileModal } from '../components/profile/ShareProfileModal';
import { MetricCard } from '../components/dashboard/MetricCard';
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
} from 'lucide-react';

export const ProfilePage: React.FC = () => {
  const { user } = useAuth();
  const { profile, loading, saving, error, isInitialSetup, fetchProfile, saveProfile } = useProfile();

  const [dashboardStats, setDashboardStats] = useState<DashboardStatsResponse | null>(null);
  const [isEditModalOpen, setIsEditModalOpen] = useState<boolean>(false);
  const [isShareModalOpen, setIsShareModalOpen] = useState<boolean>(false);

  useEffect(() => {
    getStats()
      .then((data) => setDashboardStats(data))
      .catch(() => {
        // Soft fail for stats highlight
      });
  }, []);

  const handleSave = async (data: ProfileRequest) => {
    await saveProfile(data);
  };

  const overview = dashboardStats?.overview;

  const getInitials = (name: string | null | undefined) => {
    if (!name) return 'U';
    const parts = name.trim().split(/\s+/);
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return parts[0].slice(0, 2).toUpperCase();
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <AppNavbar />

      <main
        style={{
          flex: 1,
          maxWidth: '1280px',
          width: '100%',
          margin: '0 auto',
          padding: '2rem 1.5rem',
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
                backgroundColor: 'rgba(235, 115, 18, 0.1)',
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
          <>
            {/* Top Profile Card */}
            <div className="card" style={{ padding: '2rem', position: 'relative' }}>
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
                        backgroundColor: 'rgba(235, 115, 18, 0.15)',
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
                          <GraduationCap size={14} style={{ color: '#3b82f6' }} />
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

            {/* Coding Stats Highlight section */}
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                <h2 style={{ fontSize: '1.25rem', fontWeight: 700 }}>
                  Competitive Programming Highlight
                </h2>
                {overview && (
                  <span className="badge badge-muted">
                    {overview.connectedPlatformsCount} platform{overview.connectedPlatformsCount > 1 ? 's' : ''} connected
                  </span>
                )}
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
                  value={overview?.totalProblemsSolved}
                  subtitle="Same problem may be counted on multiple platforms."
                  icon={<CheckCircle2 size={20} />}
                  accentColor="var(--color-primary)"
                />
                <MetricCard
                  title="Contests"
                  value={overview?.contestsParticipated}
                  subtitle="Attended competitions"
                  icon={<Trophy size={20} />}
                  accentColor="#3b82f6"
                />
                <MetricCard
                  title="Current Streak"
                  value={overview?.currentStreak !== null && overview?.currentStreak !== undefined ? `${overview.currentStreak} days` : null}
                  subtitle="Active daily streak"
                  icon={<Flame size={20} />}
                  accentColor="var(--color-primary)"
                />
                <MetricCard
                  title="Longest Streak"
                  value={overview?.longestStreak !== null && overview?.longestStreak !== undefined ? `${overview.longestStreak} days` : null}
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
            </div>
          </>
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

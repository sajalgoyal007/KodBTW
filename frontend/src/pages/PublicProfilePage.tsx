import React, { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { usePublicProfile } from '../hooks/usePublicProfile';
import { useAuth } from '../hooks/useAuth';
import { ShareProfileModal } from '../components/profile/ShareProfileModal';
import { PublicProfileEmptyState } from '../components/profile/PublicProfileEmptyState';
import {
  MapPin,
  GraduationCap,
  Calendar,
  ExternalLink,
  Share2,
  Trophy,
  Flame,
  Layers,
  Github,
  Linkedin,
  Globe,
  AlertCircle,
  CheckCircle2,
  Code2,
} from 'lucide-react';

export const PublicProfilePage: React.FC = () => {
  const { username } = useParams<{ username: string }>();
  const { data, loading, error, notFound, refetch } = usePublicProfile(username);
  const { user, isAuthenticated } = useAuth();
  const [isShareModalOpen, setIsShareModalOpen] = useState<boolean>(false);

  const formatMetric = (val: number | string | null | undefined) =>
    val !== null && val !== undefined ? val.toLocaleString() : '—';

  const getInitials = (name: string | null | undefined) => {
    if (!name) return 'U';
    const parts = name.trim().split(/\s+/);
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return parts[0].slice(0, 2).toUpperCase();
  };

  const getPlatformBrandColor = (platformName: string) => {
    switch (platformName.toUpperCase()) {
      case 'LEETCODE':
        return '#FFA116';
      case 'CODEFORCES':
        return '#1F8ACB';
      case 'CODECHEF':
        return '#5B4638';
      case 'GEEKSFORGEEKS':
        return '#2F8D46';
      case 'HACKERRANK':
        return '#00EA64';
      default:
        return 'var(--color-primary, var(--color-primary))';
    }
  };

  const getSourceBadge = (source: string) => {
    const isReal = source.toUpperCase().includes('REAL');
    return (
      <span
        style={{
          display: 'inline-flex',
          alignItems: 'center',
          gap: '4px',
          padding: '2px 8px',
          borderRadius: '9999px',
          fontSize: '0.6875rem',
          fontWeight: 600,
          background: isReal ? 'rgba(16, 185, 129, 0.12)' : 'rgba(245, 158, 11, 0.12)',
          color: isReal ? 'var(--color-success)' : 'var(--color-warning)',
          border: `1px solid ${isReal ? 'rgba(16, 185, 129, 0.25)' : 'rgba(245, 158, 11, 0.25)'}`,
        }}
      >
        {isReal ? <CheckCircle2 size={10} /> : null}
        {isReal ? 'REAL statistics' : 'Statistics unavailable'}
      </span>
    );
  };

  if (loading) {
    return (
      <div
        style={{
          minHeight: '100vh',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          background: 'var(--color-bg-base, var(--color-bg-base))',
          color: 'var(--color-text, var(--color-text-primary))',
        }}
      >
        <div
          style={{
            width: '40px',
            height: '40px',
            border: '3px solid rgba(56, 189, 248, 0.2)',
            borderTopColor: 'var(--color-primary, var(--color-primary))',
            borderRadius: '50%',
            animation: 'spin 1s linear infinite',
            marginBottom: '1rem',
          }}
        />
        <style>{`@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }`}</style>
        <p style={{ color: 'var(--color-text-muted, var(--color-text-muted))', fontSize: '0.9375rem' }}>
          Loading developer profile...
        </p>
      </div>
    );
  }

  if (error && !notFound) {
    return (
      <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', background: 'var(--color-bg-base, var(--color-bg-base))', color: 'var(--color-text, var(--color-text-primary))', padding: '2rem', textAlign: 'center' }}>
        <AlertCircle size={32} color="var(--color-error)" />
        <h2 style={{ fontSize: '1.5rem', fontWeight: 700, margin: '1rem 0 0.5rem' }}>Could not load developer profile</h2>
        <p style={{ color: 'var(--color-text-muted, var(--color-text-muted))', marginBottom: '1.5rem' }}>{error}</p>
        <button type="button" onClick={() => refetch()} className="btn btn-primary">Try again</button>
      </div>
    );
  }

  if (notFound || !data) {
    return (
      <div
        style={{
          minHeight: '100vh',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          background: 'var(--color-bg-base, var(--color-bg-base))',
          padding: '2rem',
          textAlign: 'center',
        }}
      >
        <div
          style={{
            width: '64px',
            height: '64px',
            borderRadius: '50%',
            background: 'rgba(239, 68, 68, 0.1)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: 'var(--color-error)',
            marginBottom: '1.25rem',
          }}
        >
          <AlertCircle size={32} />
        </div>
        <h2 style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--color-text, var(--color-text-primary))', marginBottom: '0.5rem' }}>
          Developer Not Found
        </h2>
        <p style={{ color: 'var(--color-text-muted, var(--color-text-muted))', maxWidth: '420px', marginBottom: '1.5rem', lineHeight: 1.5 }}>
          No public developer profile exists with the handle{' '}
          <code style={{ background: 'rgba(255,255,255,0.08)', padding: '2px 6px', borderRadius: '4px', color: 'var(--color-primary, var(--color-primary))' }}>
            @{username}
          </code>
          .
        </p>
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <Link
            to="/"
            style={{
              padding: '0.625rem 1.25rem',
              borderRadius: '8px',
              background: 'var(--color-primary, var(--color-primary))',
              color: 'var(--color-bg-base)',
              textDecoration: 'none',
              fontWeight: 600,
              fontSize: '0.875rem',
            }}
          >
            Go Home
          </Link>
          <button
            onClick={() => refetch()}
            style={{
              padding: '0.625rem 1.25rem',
              borderRadius: '8px',
              background: 'rgba(255, 255, 255, 0.05)',
              border: '1px solid var(--color-border, var(--color-border-subtle))',
              color: 'var(--color-text, var(--color-text-primary))',
              cursor: 'pointer',
              fontSize: '0.875rem',
            }}
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  const { overview, socialLinks, analytics } = data;
  const platforms = data.platforms.filter((platform) =>
    platform.sourceStatus === 'SOURCE_PENDING' || platform.source?.toUpperCase().includes('REAL')
  );
  const isOwnProfile = isAuthenticated && (user?.email && data.displayName === user.name);

  return (
    <div
      style={{
        minHeight: '100vh',
        background: 'var(--color-bg-base, var(--color-bg-base))',
        color: 'var(--color-text, var(--color-text-primary))',
        display: 'flex',
        flexDirection: 'column',
      }}
    >
      {/* Top Navbar */}
      <header
        style={{
          borderBottom: '1px solid var(--color-border, var(--color-border-subtle))',
          background: 'rgba(22, 27, 34, 0.85)',
          backdropFilter: 'blur(8px)',
          position: 'sticky',
          top: 0,
          zIndex: 40,
        }}
      >
        <div
          style={{
            maxWidth: '1200px',
            margin: '0 auto',
            padding: '0.875rem 1.5rem',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
          }}
        >
          <Link
            to="/"
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem',
              textDecoration: 'none',
              color: 'var(--color-text, var(--color-text-primary))',
              fontWeight: 700,
              fontSize: '1.125rem',
            }}
          >
            <div
              style={{
                width: '28px',
                height: '28px',
                borderRadius: '6px',
                background: 'linear-gradient(135deg, var(--color-primary, var(--color-primary)), #0284c7)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#fff',
              }}
            >
              <Code2 size={16} />
            </div>
            KodBTW
          </Link>

          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <button
              onClick={() => setIsShareModalOpen(true)}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '0.375rem',
                padding: '0.5rem 0.875rem',
                borderRadius: '8px',
                background: 'rgba(56, 189, 248, 0.1)',
                border: '1px solid rgba(56, 189, 248, 0.3)',
                color: 'var(--color-primary, var(--color-primary))',
                fontSize: '0.8125rem',
                fontWeight: 600,
                cursor: 'pointer',
              }}
            >
              <Share2 size={14} />
              Share Profile
            </button>

            {!isAuthenticated ? (
              <Link
                to="/register"
                style={{
                  padding: '0.5rem 0.875rem',
                  borderRadius: '8px',
                  background: 'var(--color-primary, var(--color-primary))',
                  color: 'var(--color-bg-base)',
                  textDecoration: 'none',
                  fontSize: '0.8125rem',
                  fontWeight: 600,
                }}
              >
                Create Yours
              </Link>
            ) : (
              <Link
                to="/dashboard"
                style={{
                  padding: '0.5rem 0.875rem',
                  borderRadius: '8px',
                  background: 'rgba(255, 255, 255, 0.05)',
                  border: '1px solid var(--color-border, var(--color-border-subtle))',
                  color: 'var(--color-text, var(--color-text-primary))',
                  textDecoration: 'none',
                  fontSize: '0.8125rem',
                  fontWeight: 500,
                }}
              >
                Dashboard
              </Link>
            )}
          </div>
        </div>
      </header>

      {/* Own Profile Banner */}
      {isOwnProfile && (
        <div
          style={{
            background: 'rgba(56, 189, 248, 0.08)',
            borderBottom: '1px solid rgba(56, 189, 248, 0.2)',
            padding: '0.625rem 1.5rem',
            textAlign: 'center',
            fontSize: '0.8125rem',
            color: 'var(--color-primary, var(--color-primary))',
          }}
        >
          Viewing your public profile. Other developers see this exact page.{' '}
          <Link
            to="/profile"
            style={{ color: '#fff', textDecoration: 'underline', fontWeight: 600, marginLeft: '0.375rem' }}
          >
            Edit Profile
          </Link>
        </div>
      )}

      {/* Main Body */}
      <main style={{ flex: 1, maxWidth: '1200px', margin: '0 auto', width: '100%', padding: '2rem 1.5rem' }}>
        {/* Header Profile Card */}
        <section
          style={{
            background: 'var(--color-bg-card, var(--color-bg-card))',
            border: '1px solid var(--color-border, var(--color-border-subtle))',
            borderRadius: '16px',
            padding: '2rem',
            marginBottom: '1.5rem',
            boxShadow: '0 8px 24px rgba(0,0,0,0.25)',
          }}
        >
          <div
            style={{
              display: 'flex',
              flexDirection: 'row',
              flexWrap: 'wrap',
              gap: '1.5rem',
              alignItems: 'center',
              justifyContent: 'space-between',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '1.25rem', flexWrap: 'wrap' }}>
              {/* Avatar */}
              <div
                style={{
                  width: '84px',
                  height: '84px',
                  borderRadius: '50%',
                  background: data.avatarUrl
                    ? `url(${data.avatarUrl}) center/cover no-repeat`
                    : 'linear-gradient(135deg, #0284c7, var(--color-primary))',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '2rem',
                  fontWeight: 700,
                  color: '#ffffff',
                  border: '3px solid rgba(56, 189, 248, 0.3)',
                  boxShadow: '0 4px 12px rgba(0,0,0,0.3)',
                }}
              >
                {!data.avatarUrl && getInitials(data.displayName)}
              </div>

              {/* Bio & Handle */}
              <div>
                <h1
                  style={{
                    fontSize: '1.75rem',
                    fontWeight: 800,
                    color: 'var(--color-text, var(--color-text-primary))',
                    margin: '0 0 0.25rem 0',
                  }}
                >
                  {data.displayName || data.username}
                </h1>
                <div
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '0.5rem',
                    color: 'var(--color-primary, var(--color-primary))',
                    fontSize: '0.9375rem',
                    fontWeight: 600,
                    marginBottom: '0.5rem',
                  }}
                >
                  <span>@{data.username}</span>
                </div>
                {data.bio && (
                  <p
                    style={{
                      color: 'var(--color-text-muted, var(--color-text-muted))',
                      fontSize: '0.875rem',
                      lineHeight: 1.5,
                      maxWidth: '560px',
                      margin: 0,
                    }}
                  >
                    {data.bio}
                  </p>
                )}
              </div>
            </div>

            {/* Social Links & Share */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.625rem', flexWrap: 'wrap' }}>
              {socialLinks.githubUrl && (
                <a
                  href={socialLinks.githubUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '0.375rem',
                    padding: '0.5rem 0.75rem',
                    borderRadius: '8px',
                    background: 'rgba(255, 255, 255, 0.05)',
                    border: '1px solid var(--color-border, var(--color-border-subtle))',
                    color: 'var(--color-text, var(--color-text-primary))',
                    fontSize: '0.8125rem',
                    textDecoration: 'none',
                  }}
                >
                  <Github size={15} />
                  GitHub
                </a>
              )}
              {socialLinks.linkedinUrl && (
                <a
                  href={socialLinks.linkedinUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '0.375rem',
                    padding: '0.5rem 0.75rem',
                    borderRadius: '8px',
                    background: 'rgba(10, 102, 194, 0.1)',
                    border: '1px solid rgba(10, 102, 194, 0.3)',
                    color: '#70b5f9',
                    fontSize: '0.8125rem',
                    textDecoration: 'none',
                  }}
                >
                  <Linkedin size={15} />
                  LinkedIn
                </a>
              )}
              {socialLinks.portfolioUrl && (
                <a
                  href={socialLinks.portfolioUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '0.375rem',
                    padding: '0.5rem 0.75rem',
                    borderRadius: '8px',
                    background: 'rgba(255, 255, 255, 0.05)',
                    border: '1px solid var(--color-border, var(--color-border-subtle))',
                    color: 'var(--color-text, var(--color-text-primary))',
                    fontSize: '0.8125rem',
                    textDecoration: 'none',
                  }}
                >
                  <Globe size={15} />
                  Portfolio
                </a>
              )}
            </div>
          </div>

          {/* Academic & Location Chips */}
          <div
            style={{
              display: 'flex',
              flexWrap: 'wrap',
              gap: '1rem',
              marginTop: '1.25rem',
              paddingTop: '1.25rem',
              borderTop: '1px solid var(--color-border, var(--color-border-subtle))',
              fontSize: '0.8125rem',
              color: 'var(--color-text-muted, var(--color-text-muted))',
            }}
          >
            {data.college && (
              <span style={{ display: 'flex', alignItems: 'center', gap: '0.375rem' }}>
                <GraduationCap size={15} />
                {data.college}
              </span>
            )}
            {data.graduationYear && (
              <span style={{ display: 'flex', alignItems: 'center', gap: '0.375rem' }}>
                <Calendar size={15} />
                Class of {data.graduationYear}
              </span>
            )}
            {data.location && (
              <span style={{ display: 'flex', alignItems: 'center', gap: '0.375rem' }}>
                <MapPin size={15} />
                {data.location}
              </span>
            )}
          </div>
        </section>

        {/* Unified Metrics Ribbon */}
        <section
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
            gap: '1rem',
            marginBottom: '1.5rem',
          }}
        >
          {/* Total Solved Card */}
          <div
            style={{
              background: 'var(--color-bg-card, var(--color-bg-card))',
              border: '1px solid var(--color-border, var(--color-border-subtle))',
              borderRadius: '12px',
              padding: '1.25rem',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--color-text-muted, var(--color-text-muted))', fontSize: '0.8125rem', marginBottom: '0.5rem' }}>
              <Layers size={16} color="var(--color-primary, var(--color-primary))" />
              Total Platform Solves
            </div>
            <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--color-text, var(--color-text-primary))' }}>
              {formatMetric(overview.totalProblemsSolved)}
            </div>
            <div style={{ display: 'flex', gap: '0.5rem', marginTop: '0.5rem', fontSize: '0.6875rem' }}>
              <span style={{ color: 'var(--color-success)' }}>E: {overview.easySolved}</span>
              <span style={{ color: 'var(--color-warning)' }}>M: {overview.mediumSolved}</span>
              <span style={{ color: 'var(--color-error)' }}>H: {overview.hardSolved}</span>
            </div>
          </div>

          {/* Best Contest Rating Card */}
          <div
            style={{
              background: 'var(--color-bg-card, var(--color-bg-card))',
              border: '1px solid var(--color-border, var(--color-border-subtle))',
              borderRadius: '12px',
              padding: '1.25rem',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--color-text-muted, var(--color-text-muted))', fontSize: '0.8125rem', marginBottom: '0.5rem' }}>
              <Trophy size={16} color="var(--color-warning)" />
              Peak Contest Rating
            </div>
            <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--color-text, var(--color-text-primary))' }}>
              {formatMetric(overview.bestRating)}
            </div>
            <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted, var(--color-text-muted))', marginTop: '0.5rem' }}>
              {overview.bestRatingPlatform ? `on ${overview.bestRatingPlatform}` : '—'}
            </div>
          </div>

          {/* Active Streak Card */}
          <div
            style={{
              background: 'var(--color-bg-card, var(--color-bg-card))',
              border: '1px solid var(--color-border, var(--color-border-subtle))',
              borderRadius: '12px',
              padding: '1.25rem',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--color-text-muted, var(--color-text-muted))', fontSize: '0.8125rem', marginBottom: '0.5rem' }}>
              <Flame size={16} color="#f97316" />
              Current Streak
            </div>
            <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--color-text, var(--color-text-primary))' }}>
              {overview.currentStreak !== null ? `${overview.currentStreak} days` : '—'}
            </div>
            <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted, var(--color-text-muted))', marginTop: '0.5rem' }}>
              {overview.longestStreak !== null ? `Best: ${overview.longestStreak} days` : '—'}
            </div>
          </div>

          {/* Contests Attended Card */}
          <div
            style={{
              background: 'var(--color-bg-card, var(--color-bg-card))',
              border: '1px solid var(--color-border, var(--color-border-subtle))',
              borderRadius: '12px',
              padding: '1.25rem',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--color-text-muted, var(--color-text-muted))', fontSize: '0.8125rem', marginBottom: '0.5rem' }}>
              <Calendar size={16} color="#a855f7" />
              Contests Participated
            </div>
            <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--color-text, var(--color-text-primary))' }}>
              {formatMetric(overview.contestsParticipated)}
            </div>
            <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted, var(--color-text-muted))', marginTop: '0.5rem' }}>
              Sum across platforms; the same problem may be counted more than once.
            </div>
          </div>
        </section>

        {/* Connected Platforms Section */}
        <section style={{ marginBottom: '2rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
            <h2 style={{ fontSize: '1.25rem', fontWeight: 700, margin: 0 }}>
              Connected Coding Platforms ({platforms.length})
            </h2>
          </div>

          {platforms.length === 0 ? (
            <PublicProfileEmptyState />
          ) : (
            <div
              style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))',
                gap: '1rem',
              }}
            >
              {platforms.map((p) => {
                const brandColor = getPlatformBrandColor(p.platform);
                return (
                  <div
                    key={p.platform}
                    style={{
                      background: 'var(--color-bg-card, var(--color-bg-card))',
                      border: '1px solid var(--color-border, var(--color-border-subtle))',
                      borderRadius: '12px',
                      padding: '1.25rem',
                      display: 'flex',
                      flexDirection: 'column',
                      justifyContent: 'space-between',
                    }}
                  >
                    <div>
                      {/* Platform Header */}
                      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.75rem' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                          <span
                            style={{
                              width: '10px',
                              height: '10px',
                              borderRadius: '50%',
                              backgroundColor: brandColor,
                            }}
                          />
                          <span style={{ fontWeight: 700, fontSize: '0.9375rem' }}>{p.platform}</span>
                        </div>
                        {getSourceBadge(p.source)}
                      </div>

                      {/* Username link */}
                      <div style={{ marginBottom: '1rem' }}>
                        {p.profileUrl ? (
                          <a
                            href={p.profileUrl}
                            target="_blank"
                            rel="noopener noreferrer"
                            style={{
                              display: 'inline-flex',
                              alignItems: 'center',
                              gap: '0.25rem',
                              color: 'var(--color-primary, var(--color-primary))',
                              textDecoration: 'none',
                              fontSize: '0.8125rem',
                              fontWeight: 500,
                            }}
                          >
                            @{p.username}
                            <ExternalLink size={12} />
                          </a>
                        ) : (
                          <span style={{ fontSize: '0.8125rem', color: 'var(--color-text-muted, var(--color-text-muted))' }}>
                          @{p.username}
                          </span>
                        )}
                        <div style={{ marginTop: '0.35rem', color: 'var(--color-text-muted)', fontSize: '0.7rem' }}>Self-reported · unverified handle</div>
                      </div>

                      {p.sourceStatus === 'SOURCE_PENDING' ? (
                        <div style={{ padding: '0.875rem', borderRadius: '8px', marginBottom: '0.75rem', color: 'var(--color-text-muted, var(--color-text-muted))', background: 'rgba(0,0,0,0.2)', fontSize: '0.8125rem' }}>
                          Connected — live statistics currently unavailable.
                        </div>
                      ) : <>
                      {/* Stats Grid */}
                      <div
                        style={{
                          display: 'grid',
                          gridTemplateColumns: 'repeat(3, 1fr)',
                          gap: '0.5rem',
                          background: 'rgba(0,0,0,0.2)',
                          padding: '0.75rem',
                          borderRadius: '8px',
                          marginBottom: '0.75rem',
                          textAlign: 'center',
                        }}
                      >
                        <div>
                          <div style={{ fontSize: '0.6875rem', color: 'var(--color-text-muted, var(--color-text-muted))' }}>Solved</div>
                          <div style={{ fontWeight: 700, fontSize: '0.9375rem' }}>{formatMetric(p.totalSolved)}</div>
                        </div>
                        <div>
                          <div style={{ fontSize: '0.6875rem', color: 'var(--color-text-muted, var(--color-text-muted))' }}>Rating</div>
                          <div style={{ fontWeight: 700, fontSize: '0.9375rem' }}>{formatMetric(p.rating)}</div>
                        </div>
                        <div>
                          <div style={{ fontSize: '0.6875rem', color: 'var(--color-text-muted, var(--color-text-muted))' }}>Contests</div>
                          <div style={{ fontWeight: 700, fontSize: '0.9375rem' }}>{formatMetric(p.contests)}</div>
                        </div>
                      </div>

                      {/* Difficulty Sub-counts */}
                      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem', padding: '0 0.25rem' }}>
                        <span style={{ color: 'var(--color-success)' }}>Easy: {formatMetric(p.easySolved)}</span>
                        <span style={{ color: 'var(--color-warning)' }}>Medium: {formatMetric(p.mediumSolved)}</span>
                        <span style={{ color: 'var(--color-error)' }}>Hard: {formatMetric(p.hardSolved)}</span>
                      </div>
                      </>}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </section>

        {/* Difficulty Distribution Section */}
        {platforms.length > 0 && analytics?.difficulty && (
          <section
            style={{
              background: 'var(--color-bg-card, var(--color-bg-card))',
              border: '1px solid var(--color-border, var(--color-border-subtle))',
              borderRadius: '12px',
              padding: '1.5rem',
              marginBottom: '2rem',
            }}
          >
            <h3 style={{ fontSize: '1.125rem', fontWeight: 700, marginBottom: '1rem', margin: 0 }}>
              Difficulty Distribution
            </h3>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '1rem', marginTop: '1rem' }}>
              <div style={{ background: 'rgba(16, 185, 129, 0.08)', border: '1px solid rgba(16, 185, 129, 0.2)', padding: '1rem', borderRadius: '8px' }}>
                <div style={{ color: 'var(--color-success)', fontWeight: 600, fontSize: '0.8125rem' }}>Easy</div>
                <div style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--color-text, var(--color-text-primary))' }}>
                  {formatMetric(analytics.difficulty.easy?.count)}
                </div>
                <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted, var(--color-text-muted))' }}>
                  {analytics.difficulty.easy?.percentage != null ? `${analytics.difficulty.easy.percentage}%` : '—'}
                </div>
              </div>
              <div style={{ background: 'rgba(245, 158, 11, 0.08)', border: '1px solid rgba(245, 158, 11, 0.2)', padding: '1rem', borderRadius: '8px' }}>
                <div style={{ color: 'var(--color-warning)', fontWeight: 600, fontSize: '0.8125rem' }}>Medium</div>
                <div style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--color-text, var(--color-text-primary))' }}>
                  {formatMetric(analytics.difficulty.medium?.count)}
                </div>
                <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted, var(--color-text-muted))' }}>
                  {analytics.difficulty.medium?.percentage != null ? `${analytics.difficulty.medium.percentage}%` : '—'}
                </div>
              </div>
              <div style={{ background: 'rgba(239, 68, 68, 0.08)', border: '1px solid rgba(239, 68, 68, 0.2)', padding: '1rem', borderRadius: '8px' }}>
                <div style={{ color: 'var(--color-error)', fontWeight: 600, fontSize: '0.8125rem' }}>Hard</div>
                <div style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--color-text, var(--color-text-primary))' }}>
                  {formatMetric(analytics.difficulty.hard?.count)}
                </div>
                <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted, var(--color-text-muted))' }}>
                  {analytics.difficulty.hard?.percentage != null ? `${analytics.difficulty.hard.percentage}%` : '—'}
                </div>
              </div>
            </div>
          </section>
        )}
      </main>

      {/* Footer */}
      <footer
        style={{
          borderTop: '1px solid var(--color-border, var(--color-border-subtle))',
          padding: '1.5rem',
          textAlign: 'center',
          fontSize: '0.8125rem',
          color: 'var(--color-text-muted, var(--color-text-muted))',
        }}
      >
        Built with <strong style={{ color: 'var(--color-text, var(--color-text-primary))' }}>KodBTW</strong> • Unified Developer Stats & CP Portfolio
      </footer>

      {/* Share Profile Modal */}
      <ShareProfileModal
        isOpen={isShareModalOpen}
        onClose={() => setIsShareModalOpen(false)}
        username={data.username}
        displayName={data.displayName}
      />
    </div>
  );
};

export default PublicProfilePage;

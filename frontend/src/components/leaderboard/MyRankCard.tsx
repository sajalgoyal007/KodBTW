import React from 'react';
import { MyRankResponse } from '../../types/leaderboard';
import { Award, CheckCircle2, AlertTriangle, GraduationCap, Code2, Zap } from 'lucide-react';

interface MyRankCardProps {
  myRank: MyRankResponse | null;
  loading: boolean;
}

export const MyRankCard: React.FC<MyRankCardProps> = ({ myRank, loading }) => {
  if (loading) {
    return (
      <div className="card" style={{ padding: '1.5rem' }}>
        <div className="skeleton" style={{ height: '24px', width: '200px', marginBottom: '1rem' }} />
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '1rem' }}>
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="skeleton" style={{ height: '70px', borderRadius: 'var(--radius-md)' }} />
          ))}
        </div>
      </div>
    );
  }

  const isRanked = myRank?.globalRank != null;

  return (
    <div
      className="card"
      style={{
        padding: '1.5rem',
        border: '1px solid var(--color-border-subtle)',
        position: 'relative',
        overflow: 'hidden',
      }}
    >
      {/* Header row */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '1rem',
          marginBottom: '1.25rem',
          paddingBottom: '1rem',
          borderBottom: '1px solid var(--color-border-subtle)',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <div
            style={{
              width: '40px',
              height: '40px',
              borderRadius: 'var(--radius-md)',
              backgroundColor: 'rgba(249, 115, 22, 0.1)',
              border: '1px solid rgba(249, 115, 22, 0.2)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'var(--color-primary)',
            }}
          >
            <Award size={22} />
          </div>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <h2 style={{ fontSize: '1.125rem', fontWeight: 700, margin: 0 }}>
                {myRank?.displayName || 'My Standing'}
              </h2>
              {myRank?.realDataOnly && (
                <span className="badge badge-primary" style={{ fontSize: '0.75rem' }}>
                  <CheckCircle2 size={12} />
                  <span>Verified</span>
                </span>
              )}
              {myRank?.hasMockData && (
                <span
                  className="badge"
                  style={{
                    backgroundColor: 'rgba(234, 179, 8, 0.15)',
                    color: '#eab308',
                    border: '1px solid rgba(234, 179, 8, 0.3)',
                    fontSize: '0.75rem',
                  }}
                >
                  <AlertTriangle size={12} />
                  <span>Includes Unverified</span>
                </span>
              )}
            </div>
            <p style={{ fontSize: '0.8125rem', color: 'var(--color-text-secondary)', margin: '0.2rem 0 0 0' }}>
              {isRanked
                ? 'Your position across real verified platform submissions'
                : 'Connect and verify real coding platforms (LeetCode / Codeforces) to rank globally'}
            </p>
          </div>
        </div>

        {myRank?.lastSyncedAt && (
          <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)' }}>
            Last synced: {new Date(myRank.lastSyncedAt).toLocaleString()}
          </div>
        )}
      </div>

      {/* Metric Cards Grid */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
          gap: '1rem',
        }}
      >
        {/* Global Rank */}
        <div
          style={{
            backgroundColor: 'var(--color-bg-subtle)',
            borderRadius: 'var(--radius-md)',
            padding: '1rem',
            border: isRanked ? '1px solid rgba(249, 115, 22, 0.3)' : '1px solid var(--color-border-subtle)',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--color-text-secondary)', fontSize: '0.8125rem', marginBottom: '0.375rem' }}>
            <Award size={15} style={{ color: 'var(--color-primary)' }} />
            <span>Global Rank</span>
          </div>
          <div style={{ fontSize: '1.5rem', fontWeight: 800, color: isRanked ? 'var(--color-primary)' : 'var(--color-text-muted)' }}>
            {isRanked ? `#${myRank?.globalRank}` : 'Unranked'}
          </div>
        </div>

        {/* College Rank */}
        <div
          style={{
            backgroundColor: 'var(--color-bg-subtle)',
            borderRadius: 'var(--radius-md)',
            padding: '1rem',
            border: '1px solid var(--color-border-subtle)',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--color-text-secondary)', fontSize: '0.8125rem', marginBottom: '0.375rem' }}>
            <GraduationCap size={15} />
            <span>College Rank</span>
          </div>
          <div style={{ fontSize: '1.5rem', fontWeight: 800 }}>
            {myRank?.collegeRank != null ? `#${myRank.collegeRank}` : '—'}
          </div>
          {myRank?.college && (
            <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', marginTop: '0.2rem', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
              {myRank.college}
            </div>
          )}
        </div>

        {/* Weighted Difficulty Score (WDS) */}
        <div
          style={{
            backgroundColor: 'var(--color-bg-subtle)',
            borderRadius: 'var(--radius-md)',
            padding: '1rem',
            border: '1px solid var(--color-border-subtle)',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--color-text-secondary)', fontSize: '0.8125rem', marginBottom: '0.375rem' }}>
            <Zap size={15} style={{ color: '#eab308' }} />
            <span>Score (WDS)</span>
          </div>
          <div style={{ fontSize: '1.5rem', fontWeight: 800 }}>
            {myRank?.weightedScore != null ? Math.round(myRank.weightedScore).toLocaleString() : '0'}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', marginTop: '0.2rem' }}>
            easy×1 + med×3 + hard×6
          </div>
        </div>

        {/* Total Solved */}
        <div
          style={{
            backgroundColor: 'var(--color-bg-subtle)',
            borderRadius: 'var(--radius-md)',
            padding: '1rem',
            border: '1px solid var(--color-border-subtle)',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--color-text-secondary)', fontSize: '0.8125rem', marginBottom: '0.375rem' }}>
            <Code2 size={15} />
            <span>Verified Solved</span>
          </div>
          <div style={{ fontSize: '1.5rem', fontWeight: 800 }}>
            {myRank?.totalSolved != null ? myRank.totalSolved.toLocaleString() : '0'}
          </div>
          {myRank?.bestRating != null && (
            <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', marginTop: '0.2rem' }}>
              Best: {myRank.bestRating} ({myRank.bestRatingPlatform})
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

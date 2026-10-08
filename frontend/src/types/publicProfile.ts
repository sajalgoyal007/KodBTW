import { PlatformType } from './platform';
import { DifficultyAnalytics, PlatformComparison, ContestAnalytics } from './dashboard';

export interface PublicSocialLinks {
  githubUrl: string | null;
  linkedinUrl: string | null;
  portfolioUrl: string | null;
}

export interface PublicOverview {
  totalProblemsSolved: number | null;
  easySolved: number | null;
  mediumSolved: number | null;
  hardSolved: number | null;
  contestsParticipated: number | null;
  bestRating: number | null;
  bestRatingPlatform: string | null;
  currentStreak: number | null;
  longestStreak: number | null;
}

export interface PublicPlatformStat {
  platform: PlatformType;
  username: string;
  profileUrl: string | null;
  verified: boolean;
  totalSolved: number | null;
  easySolved: number | null;
  mediumSolved: number | null;
  hardSolved: number | null;
  rating: number | null;
  maxRating: number | null;
  rank: number | null;
  contests: number | null;
  currentStreak: number | null;
  longestStreak: number | null;
  source: string;
  sourceStatus: 'REAL_AVAILABLE' | 'SOURCE_PENDING';
  lastSyncedAt: string | null;
}

export interface PublicAnalytics {
  difficulty: DifficultyAnalytics;
  platformComparison: PlatformComparison[];
  contests: ContestAnalytics;
}

export interface PublicProfileData {
  username: string;
  displayName: string | null;
  bio: string | null;
  avatarUrl: string | null;
  college: string | null;
  graduationYear: number | null;
  location: string | null;
  socialLinks: PublicSocialLinks;
  overview: PublicOverview;
  platforms: PublicPlatformStat[];
  analytics: PublicAnalytics;
}

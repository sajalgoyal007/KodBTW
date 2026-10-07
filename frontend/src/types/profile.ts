export interface ProfileResponse {
  id: number;
  userId: number;
  username: string;
  displayName: string | null;
  bio: string | null;
  avatarUrl: string | null;
  college: string | null;
  graduationYear: number | null;
  location: string | null;
  githubUrl: string | null;
  linkedinUrl: string | null;
  portfolioUrl: string | null;
}

export interface ProfileRequest {
  username?: string | null;
  displayName?: string | null;
  bio?: string | null;
  avatarUrl?: string | null;
  college?: string | null;
  graduationYear?: number | null;
  location?: string | null;
  githubUrl?: string | null;
  linkedinUrl?: string | null;
  portfolioUrl?: string | null;
}

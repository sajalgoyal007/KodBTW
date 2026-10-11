export type ContestSourceState = 'AVAILABLE' | 'STALE' | 'UNAVAILABLE';

export interface ContestCalendarEvent {
  id: string;
  platform: string;
  name: string;
  startTime: string;
  durationSeconds: number;
  officialUrl: string;
  source: string;
  fetchedAt: string;
}

export interface ContestSourceStatus {
  platform: string;
  status: ContestSourceState;
  message: string | null;
  fetchedAt: string | null;
  cacheExpiresAt: string | null;
  source: string | null;
}

export interface ContestCalendarResponse {
  generatedAt: string;
  stale: boolean;
  sources: ContestSourceStatus[];
  contests: ContestCalendarEvent[];
}

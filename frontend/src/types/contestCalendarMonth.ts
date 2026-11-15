export type ContestPlatformCode =
  | 'CODEFORCES'
  | 'LEETCODE'
  | 'CODECHEF'
  | 'ATCODER'
  | 'GFG'
  | 'CODE360'
  | 'HACKERRANK'
  | 'HACKEREARTH';

export type ContestEventStatus = 'UPCOMING' | 'ONGOING' | 'COMPLETED';

export interface ContestMonthEvent {
  id: string;
  platform: ContestPlatformCode;
  platformDisplayName: string;
  title: string;
  startTimeUtc: string;
  endTimeUtc: string;
  durationSeconds: number;
  contestUrl: string;
  status: ContestEventStatus;
}

export interface ContestMonthCalendarResponse {
  year: number;
  month: number;
  timeZone: string;
  generatedAt: string;
  contests: ContestMonthEvent[];
}

export interface ContestPlatformMeta {
  code: ContestPlatformCode;
  label: string;
  accent: string;
}

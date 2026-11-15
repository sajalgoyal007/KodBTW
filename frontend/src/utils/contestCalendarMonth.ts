import {
  ContestEventStatus,
  ContestMonthCalendarResponse,
  ContestMonthEvent,
  ContestPlatformCode,
  ContestPlatformMeta,
} from '../types/contestCalendarMonth';

export const IST_TIME_ZONE = 'Asia/Kolkata';

export const CONTEST_PLATFORMS: readonly ContestPlatformMeta[] = [
  { code: 'CODEFORCES', label: 'Codeforces', accent: '#1f8acb' },
  { code: 'LEETCODE', label: 'LeetCode', accent: '#f89f1b' },
  { code: 'CODECHEF', label: 'CodeChef', accent: '#a0522d' },
  { code: 'ATCODER', label: 'AtCoder', accent: '#475569' },
  { code: 'GFG', label: 'GFG', accent: '#2f8d46' },
  { code: 'CODE360', label: 'Code360', accent: '#f97316' },
  { code: 'HACKERRANK', label: 'HackerRank', accent: '#0d9488' },
  { code: 'HACKEREARTH', label: 'HackerEarth', accent: '#6d28d9' },
];

const PLATFORM_CODES = CONTEST_PLATFORMS.map((platform) => platform.code);
const STATUSES: readonly ContestEventStatus[] = ['UPCOMING', 'ONGOING', 'COMPLETED'];
const WEEKDAY_LABELS = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
const MONTH_NAMES = [
  'January', 'February', 'March', 'April', 'May', 'June',
  'July', 'August', 'September', 'October', 'November', 'December',
];

function pad2(value: number): string {
  return String(value).padStart(2, '0');
}

function isObject(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null;
}

function isInstant(value: unknown): value is string {
  return typeof value === 'string' && Number.isFinite(Date.parse(value));
}

export function isSafeHttpUrl(value: unknown): value is string {
  if (typeof value !== 'string') return false;
  const trimmed = value.trim();
  if (!/^https?:\/\//i.test(trimmed)) return false;
  try {
    const parsed = new URL(trimmed);
    return (parsed.protocol === 'http:' || parsed.protocol === 'https:') && parsed.hostname.length > 0;
  } catch {
    return false;
  }
}

export function parseContestMonthCalendarResponse(value: unknown): ContestMonthCalendarResponse {
  if (!isObject(value)
    || typeof value.year !== 'number' || !Number.isInteger(value.year)
    || typeof value.month !== 'number' || !Number.isInteger(value.month)
    || value.month < 1 || value.month > 12
    || typeof value.timeZone !== 'string'
    || !isInstant(value.generatedAt)
    || !Array.isArray(value.contests)) {
    throw new Error('Contest calendar response is invalid.');
  }

  const contests = value.contests.map((contest): ContestMonthEvent => {
    if (!isObject(contest)
      || typeof contest.id !== 'string'
      || typeof contest.platform !== 'string'
      || !(PLATFORM_CODES as readonly string[]).includes(contest.platform)
      || typeof contest.platformDisplayName !== 'string'
      || typeof contest.title !== 'string'
      || !isInstant(contest.startTimeUtc)
      || !isInstant(contest.endTimeUtc)
      || typeof contest.durationSeconds !== 'number'
      || !Number.isFinite(contest.durationSeconds)
      || contest.durationSeconds < 0
      || typeof contest.contestUrl !== 'string'
      || !(STATUSES as readonly string[]).includes(String(contest.status))) {
      throw new Error('Contest calendar event is invalid.');
    }
    return {
      id: contest.id,
      platform: contest.platform as ContestPlatformCode,
      platformDisplayName: contest.platformDisplayName,
      title: contest.title,
      startTimeUtc: contest.startTimeUtc,
      endTimeUtc: contest.endTimeUtc,
      durationSeconds: contest.durationSeconds,
      contestUrl: isSafeHttpUrl(contest.contestUrl) ? contest.contestUrl : '',
      status: contest.status as ContestEventStatus,
    };
  });

  return {
    year: value.year,
    month: value.month,
    timeZone: value.timeZone,
    generatedAt: value.generatedAt,
    contests,
  };
}

export function getPlatformMeta(code: string): ContestPlatformMeta | undefined {
  return CONTEST_PLATFORMS.find((platform) => platform.code === code);
}

export function getPlatformLabel(code: string): string {
  return getPlatformMeta(code)?.label ?? code;
}

export function getPlatformAccent(code: string): string {
  return getPlatformMeta(code)?.accent ?? 'var(--color-border-subtle)';
}

export function toDateKey(instant: string, timeZone = IST_TIME_ZONE): string | null {
  const date = new Date(instant);
  if (!Number.isFinite(date.getTime())) return null;
  const parts = new Intl.DateTimeFormat('en-CA', {
    timeZone,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).formatToParts(date);
  const year = parts.find((part) => part.type === 'year')?.value;
  const month = parts.find((part) => part.type === 'month')?.value;
  const day = parts.find((part) => part.type === 'day')?.value;
  if (!year || !month || !day) return null;
  return `${year}-${month}-${day}`;
}

export function getCurrentMonth(timeZone = IST_TIME_ZONE, now: Date = new Date()): { year: number; month: number } {
  const parts = new Intl.DateTimeFormat('en-US', {
    timeZone,
    year: 'numeric',
    month: 'numeric',
  }).formatToParts(now);
  const year = Number(parts.find((part) => part.type === 'year')?.value);
  const month = Number(parts.find((part) => part.type === 'month')?.value);
  if (!Number.isFinite(year) || !Number.isFinite(month)) {
    return { year: now.getUTCFullYear(), month: now.getUTCMonth() + 1 };
  }
  return { year, month };
}

export function getTodayDateKey(timeZone = IST_TIME_ZONE, now: Date = new Date()): string {
  return toDateKey(now.toISOString(), timeZone) ?? '';
}

export function shiftMonth(year: number, month: number, delta: number): { year: number; month: number } {
  const index = (year * 12) + (month - 1) + delta;
  return { year: Math.floor(index / 12), month: (index % 12) + 1 };
}

export function formatMonthLabel(year: number, month: number): string {
  return `${MONTH_NAMES[month - 1] ?? ''} ${year}`.trim();
}

export function getWeekdayLabels(): readonly string[] {
  return WEEKDAY_LABELS;
}

/**
 * Returns six weeks of calendar cells (42 entries) for the given month.
 * Each non-null cell is a `YYYY-MM-DD` date key; nulls are padding.
 */
export function buildMonthWeeks(year: number, month: number): Array<Array<string | null>> {
  const firstWeekday = new Date(Date.UTC(year, month - 1, 1)).getUTCDay();
  const daysInMonth = new Date(Date.UTC(year, month, 0)).getUTCDate();
  const cells: Array<string | null> = [];
  for (let index = 0; index < firstWeekday; index += 1) cells.push(null);
  for (let day = 1; day <= daysInMonth; day += 1) {
    cells.push(`${year}-${pad2(month)}-${pad2(day)}`);
  }
  while (cells.length < 42) cells.push(null);

  const weeks: Array<Array<string | null>> = [];
  for (let index = 0; index < cells.length; index += 7) {
    weeks.push(cells.slice(index, index + 7));
  }
  return weeks;
}

export function getDayOfMonth(dateKey: string): number {
  return Number(dateKey.slice(8, 10));
}

export function groupContestsByDate(
  contests: readonly ContestMonthEvent[],
  timeZone = IST_TIME_ZONE,
): Map<string, ContestMonthEvent[]> {
  const grouped = new Map<string, ContestMonthEvent[]>();
  for (const contest of contests) {
    const key = toDateKey(contest.startTimeUtc, timeZone);
    if (!key) continue;
    const bucket = grouped.get(key);
    if (bucket) bucket.push(contest);
    else grouped.set(key, [contest]);
  }
  for (const bucket of grouped.values()) {
    bucket.sort((left, right) => Date.parse(left.startTimeUtc) - Date.parse(right.startTimeUtc));
  }
  return grouped;
}

export function filterContestsByPlatform(
  contests: readonly ContestMonthEvent[],
  platforms: readonly string[],
): ContestMonthEvent[] {
  if (platforms.length === 0) return [...contests];
  const allowed = new Set(platforms.map((platform) => platform.toUpperCase()));
  return contests.filter((contest) => allowed.has(contest.platform));
}

/** Formats as `15 Nov 2026, 8:00 PM IST`. */
export function formatContestDateTime(instant: string, timeZone = IST_TIME_ZONE): string {
  const date = new Date(instant);
  if (!Number.isFinite(date.getTime())) return 'Time unavailable';
  const datePart = new Intl.DateTimeFormat('en-GB', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    timeZone,
  }).format(date);
  const timePart = new Intl.DateTimeFormat('en-US', {
    hour: 'numeric',
    minute: '2-digit',
    hour12: true,
    timeZone,
  }).format(date);
  const zonePart = new Intl.DateTimeFormat('en-US', {
    timeZoneName: 'short',
    timeZone,
  }).formatToParts(date).find((part) => part.type === 'timeZoneName')?.value;
  const zone = timeZone === IST_TIME_ZONE ? 'IST' : (zonePart ?? timeZone);
  return `${datePart}, ${timePart} ${zone}`;
}

/** Formats a duration as `2h 00m`, `45m`, or `30m`. */
export function formatContestDuration(durationSeconds: number): string {
  if (!Number.isFinite(durationSeconds) || durationSeconds <= 0) return '0m';
  const totalMinutes = Math.round(durationSeconds / 60);
  const hours = Math.floor(totalMinutes / 60);
  const minutes = totalMinutes % 60;
  if (hours === 0) return `${minutes}m`;
  return `${hours}h ${pad2(minutes)}m`;
}

export function formatDayLabel(dateKey: string): string {
  const [year, month, day] = dateKey.split('-').map(Number);
  if (!year || !month || !day) return dateKey;
  return new Intl.DateTimeFormat('en-GB', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
    timeZone: 'UTC',
  }).format(new Date(Date.UTC(year, month - 1, day)));
}

export function formatStatusLabel(status: ContestEventStatus): string {
  if (status === 'ONGOING') return 'Live now';
  if (status === 'COMPLETED') return 'Completed';
  return 'Upcoming';
}

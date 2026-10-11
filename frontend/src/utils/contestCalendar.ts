import { ContestCalendarEvent, ContestCalendarResponse } from '../types/contestCalendar';

export type ContestPlatformFilter = 'ALL' | 'CODEFORCES';
export type ContestCalendarViewState = 'loading' | 'error' | 'unavailable' | 'stale' | 'empty' | 'contests';

function isObject(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null;
}

function isInstant(value: unknown): value is string {
  return typeof value === 'string' && Number.isFinite(Date.parse(value));
}

export function parseContestCalendarResponse(value: unknown): ContestCalendarResponse {
  if (!isObject(value) || !isInstant(value.generatedAt) || typeof value.stale !== 'boolean'
      || !Array.isArray(value.sources) || !Array.isArray(value.contests)) {
    throw new Error('Contest schedule response is invalid.');
  }

  const sources = value.sources.map((source): ContestCalendarResponse['sources'][number] => {
    if (!isObject(source)
        || typeof source.platform !== 'string'
        || !['AVAILABLE', 'STALE', 'UNAVAILABLE'].includes(String(source.status))
        || !(source.message === null || typeof source.message === 'string')
        || !(source.fetchedAt === null || isInstant(source.fetchedAt))
        || !(source.cacheExpiresAt === null || isInstant(source.cacheExpiresAt))
        || !(source.source === undefined || source.source === null || typeof source.source === 'string')) {
      throw new Error('Contest schedule source status is invalid.');
    }
    return {
      platform: source.platform,
      status: source.status as ContestCalendarResponse['sources'][number]['status'],
      message: source.message,
      fetchedAt: source.fetchedAt,
      cacheExpiresAt: source.cacheExpiresAt,
      source: typeof source.source === 'string' ? source.source : null,
    };
  });

  const contests = value.contests.map((contest): ContestCalendarEvent => {
    if (!isObject(contest)
        || typeof contest.id !== 'string'
        || typeof contest.platform !== 'string'
        || typeof contest.name !== 'string'
        || !isInstant(contest.startTime)
        || typeof contest.durationSeconds !== 'number'
        || !Number.isFinite(contest.durationSeconds)
        || typeof contest.officialUrl !== 'string'
        || typeof contest.source !== 'string'
        || !isInstant(contest.fetchedAt)) {
      throw new Error('Contest schedule event is invalid.');
    }
    return {
      id: contest.id,
      platform: contest.platform,
      name: contest.name,
      startTime: contest.startTime,
      durationSeconds: contest.durationSeconds,
      officialUrl: contest.officialUrl,
      source: contest.source,
      fetchedAt: contest.fetchedAt,
    };
  });

  return { generatedAt: value.generatedAt, stale: value.stale, sources, contests };
}

export function filterAndSortContests(
  contests: ContestCalendarEvent[],
  platform: ContestPlatformFilter = 'ALL',
): ContestCalendarEvent[] {
  return contests
    .filter((contest) => platform === 'ALL' || contest.platform === platform)
    .slice()
    .sort((left, right) => Date.parse(left.startTime) - Date.parse(right.startTime)
      || left.platform.localeCompare(right.platform)
      || left.id.localeCompare(right.id));
}

export function markContestCalendarStale(response: ContestCalendarResponse): ContestCalendarResponse {
  const sources = response.sources.map((source) => source.platform === 'CODEFORCES'
    ? {
      ...source,
      status: source.fetchedAt ? 'STALE' as const : 'UNAVAILABLE' as const,
      message: 'The latest schedule could not be refreshed.',
    }
    : source);
  return {
    ...response,
    stale: sources.some((source) => source.fetchedAt !== null),
    sources,
  };
}

export function getContestCalendarViewState(options: {
  loading: boolean;
  error: string | null;
  response: ContestCalendarResponse | null;
  visibleContestCount: number;
}): ContestCalendarViewState {
  if (options.loading && !options.response) return 'loading';
  if (options.error) return 'error';
  const sourceUnavailable = options.response?.sources.some((source) => source.status === 'UNAVAILABLE');
  if (sourceUnavailable && options.visibleContestCount === 0) return 'unavailable';
  if (options.response?.stale && options.visibleContestCount > 0) return 'stale';
  if (options.visibleContestCount === 0) return 'empty';
  return 'contests';
}

export function formatContestLocalTime(instant: string): { dateTime: string; timeZone: string } {
  const date = new Date(instant);
  if (!Number.isFinite(date.getTime())) return { dateTime: 'Time unavailable', timeZone: '' };
  const formatter = new Intl.DateTimeFormat(undefined, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
    timeZoneName: 'short',
  });
  return {
    dateTime: formatter.format(date),
    timeZone: Intl.DateTimeFormat().resolvedOptions().timeZone || 'local time',
  };
}

function formatRemaining(totalSeconds: number): string {
  const days = Math.floor(totalSeconds / 86_400);
  const hours = Math.floor((totalSeconds % 86_400) / 3_600);
  const minutes = Math.floor((totalSeconds % 3_600) / 60);
  const seconds = totalSeconds % 60;
  if (days > 0) return `${days}d ${hours}h`;
  if (hours > 0) return `${hours}h ${minutes}m`;
  if (minutes > 0) return `${minutes}m ${seconds}s`;
  return `${seconds}s`;
}

export function getContestCountdown(
  contest: Pick<ContestCalendarEvent, 'startTime' | 'durationSeconds'>,
  nowMs = Date.now(),
): { state: 'upcoming' | 'running' | 'ended'; label: string } {
  const startMs = Date.parse(contest.startTime);
  if (!Number.isFinite(startMs) || !Number.isFinite(contest.durationSeconds) || contest.durationSeconds <= 0) {
    return { state: 'ended', label: 'Time unavailable' };
  }
  const endMs = startMs + contest.durationSeconds * 1_000;
  if (nowMs < startMs) {
    return { state: 'upcoming', label: `Starts in ${formatRemaining(Math.ceil((startMs - nowMs) / 1_000))}` };
  }
  if (nowMs < endMs) {
    return { state: 'running', label: `Started · ends in ${formatRemaining(Math.ceil((endMs - nowMs) / 1_000))}` };
  }
  return { state: 'ended', label: 'Ended' };
}

export function formatContestDuration(durationSeconds: number): string {
  const totalMinutes = Math.max(0, Math.floor(durationSeconds / 60));
  const hours = Math.floor(totalMinutes / 60);
  const minutes = totalMinutes % 60;
  if (hours === 0) return `${minutes}m`;
  return minutes === 0 ? `${hours}h` : `${hours}h ${minutes}m`;
}

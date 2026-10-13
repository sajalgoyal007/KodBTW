import type { HistoryRange } from '../types/history';

export interface DatedValue {
  date: string;
  value: number | null;
}

export interface HistoryDateDomain {
  startDate: string;
  endDate: string;
}

const DAY_MS = 24 * 60 * 60 * 1000;
export const INVALID_HISTORY_DATES_MESSAGE = 'History dates are unavailable or inconsistent. Retry to load this range again.';

function dateAtUtcMidnight(date: unknown): number | null {
  if (typeof date !== 'string' || !/^\d{4}-\d{2}-\d{2}$/.test(date)) return null;
  const timestamp = Date.parse(`${date}T00:00:00Z`);
  return Number.isFinite(timestamp) && new Date(timestamp).toISOString().slice(0, 10) === date
    ? timestamp
    : null;
}

function expectedStartDate(range: HistoryRange, endDate: string): string {
  const end = new Date(`${endDate}T00:00:00Z`);
  if (range === '7d') end.setUTCDate(end.getUTCDate() - 6);
  if (range === '30d') end.setUTCDate(end.getUTCDate() - 29);
  if (range === '90d') end.setUTCDate(end.getUTCDate() - 89);
  if (range === '1y') {
    const year = end.getUTCFullYear() - 1;
    const month = end.getUTCMonth();
    const day = Math.min(end.getUTCDate(), new Date(Date.UTC(year, month + 1, 0)).getUTCDate());
    end.setUTCFullYear(year, month, day);
  }
  return end.toISOString().slice(0, 10);
}

export function isValidHistoryDateDomain(value: unknown): value is HistoryDateDomain {
  if (value === null || typeof value !== 'object') return false;
  const domain = value as Partial<HistoryDateDomain>;
  const start = dateAtUtcMidnight(domain.startDate);
  const end = dateAtUtcMidnight(domain.endDate);
  return start !== null && end !== null && end > start;
}

export function getValidatedHistoryDateDomain(
  range: HistoryRange,
  startDate: unknown,
  endDate: unknown,
): HistoryDateDomain | null {
  if (typeof startDate !== 'string' || typeof endDate !== 'string') return null;
  if (!isValidHistoryDateDomain({ startDate, endDate })) return null;
  return expectedStartDate(range, endDate) === startDate ? { startDate, endDate } : null;
}

export function isDateWithinHistoryDomain(date: unknown, domain: HistoryDateDomain): date is string {
  const value = dateAtUtcMidnight(date);
  const start = dateAtUtcMidnight(domain.startDate);
  const end = dateAtUtcMidnight(domain.endDate);
  return value !== null && start !== null && end !== null && value >= start && value <= end;
}

export function getHistoryDateMidpoint(domain: HistoryDateDomain): string | null {
  if (!isValidHistoryDateDomain(domain)) return null;
  const start = dateAtUtcMidnight(domain.startDate)!;
  const end = dateAtUtcMidnight(domain.endDate)!;
  const midpoint = new Date(start + Math.floor((end - start) / 2));
  return Number.isFinite(midpoint.getTime()) ? midpoint.toISOString().slice(0, 10) : null;
}

export function getDatePositionRatio(date: string, domain: HistoryDateDomain): number | null {
  if (!isValidHistoryDateDomain(domain)) return null;
  const value = dateAtUtcMidnight(date);
  const start = dateAtUtcMidnight(domain.startDate)!;
  const end = dateAtUtcMidnight(domain.endDate)!;
  if (value === null || value < start || value > end) return null;
  return (value - start) / (end - start);
}

export function getHistoryResponseDateError(response: unknown, selectedRange: HistoryRange): string | null {
  if (response === null || typeof response !== 'object') return INVALID_HISTORY_DATES_MESSAGE;
  const data = response as Record<string, unknown>;
  if (data.range !== selectedRange) return INVALID_HISTORY_DATES_MESSAGE;
  const domain = getValidatedHistoryDateDomain(selectedRange, data.rangeStart, data.rangeEnd);
  if (!domain) return INVALID_HISTORY_DATES_MESSAGE;
  if (!Array.isArray(data.snapshotDates) || !Array.isArray(data.overallSolved) || !Array.isArray(data.platforms)) {
    return INVALID_HISTORY_DATES_MESSAGE;
  }
  if (data.freshness === null || typeof data.freshness !== 'object') return INVALID_HISTORY_DATES_MESSAGE;
  const freshness = data.freshness as Record<string, unknown>;
  if (freshness.lastUpdatedAt !== null && typeof freshness.lastUpdatedAt !== 'string') return INVALID_HISTORY_DATES_MESSAGE;
  if (freshness.latestSnapshotDate !== null
      && !isDateWithinHistoryDomain(freshness.latestSnapshotDate, domain)) return INVALID_HISTORY_DATES_MESSAGE;
  const dates: unknown[] = [...data.snapshotDates];
  if (!data.overallSolved.every((point) => point !== null && typeof point === 'object' && 'date' in point)) {
    return INVALID_HISTORY_DATES_MESSAGE;
  }
  dates.push(...data.overallSolved.map((point) => (point as { date: unknown }).date));
  for (const series of data.platforms) {
    if (series === null || typeof series !== 'object' || !Array.isArray((series as { points?: unknown }).points)) {
      return INVALID_HISTORY_DATES_MESSAGE;
    }
    dates.push(...(series as { points: Array<{ date?: unknown }> }).points.map((point) => point?.date));
  }
  if (!dates.every((date) => isDateWithinHistoryDomain(date, domain))) return INVALID_HISTORY_DATES_MESSAGE;
  return null;
}

/** Split at unknown values and missing calendar dates; never bridge unobserved intervals. */
export function splitObservedSegments<T extends DatedValue>(points: T[]): T[][] {
  const segments: T[][] = [];
  let active: T[] = [];
  let previousDate: number | null = null;

  for (const point of points) {
    const currentDate = dateAtUtcMidnight(point.date);
    if (currentDate === null) {
      if (active.length) segments.push(active);
      active = [];
      previousDate = null;
      continue;
    }
    const missingCalendarDay = previousDate !== null && currentDate - previousDate > DAY_MS;
    if (point.value === null || missingCalendarDay) {
      if (active.length) segments.push(active);
      active = [];
    }
    if (point.value !== null) active.push(point);
    previousDate = currentDate;
  }

  if (active.length) segments.push(active);
  return segments;
}

import { apiClient } from './apiClient';
import { ContestCalendarResponse } from '../../types/contestCalendar';
import { ContestMonthCalendarResponse } from '../../types/contestCalendarMonth';
import { parseContestCalendarResponse } from '../../utils/contestCalendar';
import { parseContestMonthCalendarResponse } from '../../utils/contestCalendarMonth';

export async function getContestCalendar(limit = 50): Promise<ContestCalendarResponse> {
  const response = await apiClient<unknown>('/api/contests', { params: { limit } });
  return parseContestCalendarResponse(response);
}

export async function getContestCalendarMonth(params: {
  year: number;
  month: number;
  platforms?: readonly string[];
}): Promise<ContestMonthCalendarResponse> {
  const response = await apiClient<unknown>('/api/contests/calendar', {
    params: {
      year: params.year,
      month: params.month,
      platforms: params.platforms && params.platforms.length > 0 ? params.platforms.join(',') : undefined,
    },
    // Public endpoint: no bearer token needed, and a 401 must not clear the session.
    auth: false,
  });
  return parseContestMonthCalendarResponse(response);
}

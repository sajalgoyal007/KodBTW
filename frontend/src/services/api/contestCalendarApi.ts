import { apiClient } from './apiClient';
import { ContestCalendarResponse } from '../../types/contestCalendar';
import { parseContestCalendarResponse } from '../../utils/contestCalendar';

export async function getContestCalendar(limit = 50): Promise<ContestCalendarResponse> {
  const response = await apiClient<unknown>('/api/contests', { params: { limit } });
  return parseContestCalendarResponse(response);
}

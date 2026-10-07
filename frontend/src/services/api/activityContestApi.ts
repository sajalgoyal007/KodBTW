import { apiClient } from './apiClient';
import { DashboardActivityResponse, DashboardContestIntelligenceResponse } from '../../types/activityContest';

export function getDashboardActivity(): Promise<DashboardActivityResponse> {
  return apiClient<DashboardActivityResponse>('/api/dashboard/activity');
}

export function getDashboardContestIntelligence(): Promise<DashboardContestIntelligenceResponse> {
  return apiClient<DashboardContestIntelligenceResponse>('/api/dashboard/contests');
}

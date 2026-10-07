import { apiClient } from './apiClient';
import { DashboardInsightsResponse } from '../../types/insights';

export function getDashboardInsights(): Promise<DashboardInsightsResponse> {
  return apiClient<DashboardInsightsResponse>('/api/dashboard/insights');
}

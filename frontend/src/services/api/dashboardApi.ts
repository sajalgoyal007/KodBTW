import { apiClient } from './apiClient';
import { DashboardStatsResponse, DashboardAnalyticsResponse } from '../../types/dashboard';

export function getStats(): Promise<DashboardStatsResponse> {
  return apiClient<DashboardStatsResponse>('/api/dashboard/stats');
}

export function getAnalytics(): Promise<DashboardAnalyticsResponse> {
  return apiClient<DashboardAnalyticsResponse>('/api/dashboard/analytics');
}

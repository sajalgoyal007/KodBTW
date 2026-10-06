import { getAnalytics } from './dashboardApi';
import { DashboardAnalyticsResponse } from '../../types/dashboard';

export function fetchAnalytics(): Promise<DashboardAnalyticsResponse> {
  return getAnalytics();
}

export { getAnalytics };

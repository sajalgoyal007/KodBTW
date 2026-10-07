import { apiClient } from './apiClient';
import { DashboardHistoryResponse, HistoryRange } from '../../types/history';

export function getDashboardHistory(range: HistoryRange): Promise<DashboardHistoryResponse> {
  return apiClient<DashboardHistoryResponse>('/api/dashboard/history', { params: { range } });
}

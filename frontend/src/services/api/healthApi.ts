import { apiClient } from './apiClient';

interface HealthResponse {
  status?: string;
}

export async function checkBackendReachability(): Promise<void> {
  const response = await apiClient<HealthResponse>('/api/health', {
    method: 'GET',
    timeoutMs: 75_000,
    timeoutMessage: 'The connection check timed out. You can retry it.',
    auth: false,
    cache: 'no-store',
  });

  if (response.status !== 'UP') {
    throw new Error('Backend did not report a reachable status');
  }
}

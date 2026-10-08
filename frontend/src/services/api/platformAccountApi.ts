import { apiClient } from './apiClient';
import {
  PlatformAccountResponse,
  PlatformAccountRequest,
} from '../../types/platform';
import { PlatformStats } from '../../types/dashboard';

export interface PlatformSyncStatus {
  status: 'NEVER_SYNCED' | 'RUNNING' | 'SUCCEEDED' | 'FAILED';
  lastAttemptAt: string | null;
  lastSuccessAt: string | null;
  lastFailureAt: string | null;
  failureCategory: string | null;
  failureMessage: string | null;
  fresh: boolean;
}

export interface PlatformSyncResponse {
  syncStatus: PlatformSyncStatus;
  currentStats: PlatformStats;
  cooldownApplied: boolean;
}

export function getAll(): Promise<PlatformAccountResponse[]> {
  return apiClient<PlatformAccountResponse[]>('/api/platform-accounts');
}

export function sync(id: number): Promise<PlatformSyncResponse> {
  return apiClient<PlatformSyncResponse>(`/api/platform-accounts/${id}/sync`, { method: 'POST' });
}

export function getById(id: number): Promise<PlatformAccountResponse> {
  return apiClient<PlatformAccountResponse>(`/api/platform-accounts/${id}`);
}

export function create(data: PlatformAccountRequest): Promise<PlatformAccountResponse> {
  return apiClient<PlatformAccountResponse>('/api/platform-accounts', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

export function update(
  id: number,
  data: PlatformAccountRequest
): Promise<PlatformAccountResponse> {
  return apiClient<PlatformAccountResponse>(`/api/platform-accounts/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  });
}

export function deleteAccount(id: number): Promise<void> {
  return apiClient<void>(`/api/platform-accounts/${id}`, {
    method: 'DELETE',
  });
}

export function getStats(id: number): Promise<PlatformStats> {
  return apiClient<PlatformStats>(`/api/platform-accounts/${id}/stats`);
}

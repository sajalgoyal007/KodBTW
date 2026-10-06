import { apiClient } from './apiClient';
import { ProfileResponse, ProfileRequest } from '../../types/profile';

export function getProfile(): Promise<ProfileResponse> {
  return apiClient<ProfileResponse>('/api/profile');
}

export function updateProfile(data: ProfileRequest): Promise<ProfileResponse> {
  return apiClient<ProfileResponse>('/api/profile', {
    method: 'PUT',
    body: JSON.stringify(data),
  });
}

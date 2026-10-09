import { apiClient } from './apiClient';
import { LoginRequest, LoginResponse, RegisterRequest, User } from '../../types/auth';

const AUTH_REQUEST_TIMEOUT_MS = 75_000;

export const authApi = {
  login: (data: LoginRequest): Promise<LoginResponse> => {
    return apiClient<LoginResponse>('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify(data),
      timeoutMs: AUTH_REQUEST_TIMEOUT_MS,
      timeoutMessage: 'The server is taking longer than expected to wake up. Please try signing in again.',
    });
  },

  register: (data: RegisterRequest): Promise<User> => {
    return apiClient<User>('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify(data),
      timeoutMs: AUTH_REQUEST_TIMEOUT_MS,
      timeoutMessage: 'The server is taking longer than expected. If your account may have been created, try signing in before submitting registration again.',
    });
  },

  getMe: (): Promise<User> => {
    return apiClient<User>('/api/auth/me', {
      method: 'GET',
    });
  },
};

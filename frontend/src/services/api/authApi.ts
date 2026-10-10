import { apiClient } from './apiClient';
import { LoginRequest, LoginResponse, RegisterRequest, User } from '../../types/auth';
import { AUTH_TIMEOUT_MESSAGES } from './authTimeoutMessages';

const AUTH_REQUEST_TIMEOUT_MS = 75_000;

export const authApi = {
  login: (
    data: LoginRequest,
    timeoutMessage: string = AUTH_TIMEOUT_MESSAGES.login,
  ): Promise<LoginResponse> => {
    return apiClient<LoginResponse>('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify(data),
      timeoutMs: AUTH_REQUEST_TIMEOUT_MS,
      timeoutMessage,
    });
  },

  register: (data: RegisterRequest): Promise<User> => {
    return apiClient<User>('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify(data),
      timeoutMs: AUTH_REQUEST_TIMEOUT_MS,
      timeoutMessage: AUTH_TIMEOUT_MESSAGES.registration,
    });
  },

  getMe: (): Promise<User> => {
    return apiClient<User>('/api/auth/me', {
      method: 'GET',
    });
  },
};

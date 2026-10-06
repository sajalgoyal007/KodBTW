import { apiClient } from './apiClient';
import { LoginRequest, LoginResponse, RegisterRequest, User } from '../../types/auth';

export const authApi = {
  login: (data: LoginRequest): Promise<LoginResponse> => {
    return apiClient<LoginResponse>('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },

  register: (data: RegisterRequest): Promise<User> => {
    return apiClient<User>('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },

  getMe: (): Promise<User> => {
    return apiClient<User>('/api/auth/me', {
      method: 'GET',
    });
  },
};

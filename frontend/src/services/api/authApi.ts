import { apiClient } from './apiClient';
import { GoogleAuthChallengeResponse, GoogleIdentityStatusResponse, GoogleLoginRequest, LoginRequest, LoginResponse, RegisterRequest, User } from '../../types/auth';
import { AUTH_TIMEOUT_MESSAGES } from './authTimeoutMessages';

const AUTH_REQUEST_TIMEOUT_MS = 75_000;

function authRequestHeaders(): Record<string, string> {
  return {
    'Content-Type': 'application/json',
    'X-Request-ID': globalThis.crypto.randomUUID(),
  };
}

export const authApi = {
  login: (
    data: LoginRequest,
    timeoutMessage: string = AUTH_TIMEOUT_MESSAGES.login,
  ): Promise<LoginResponse> => {
    return apiClient<LoginResponse>('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify(data),
      headers: authRequestHeaders(),
      auth: false,
      timeoutMs: AUTH_REQUEST_TIMEOUT_MS,
      timeoutMessage,
    });
  },

  register: (data: RegisterRequest): Promise<User> => {
    return apiClient<User>('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify(data),
      headers: authRequestHeaders(),
      auth: false,
      timeoutMs: AUTH_REQUEST_TIMEOUT_MS,
      timeoutMessage: AUTH_TIMEOUT_MESSAGES.registration,
    });
  },

  googleLogin: (data: GoogleLoginRequest): Promise<LoginResponse> => {
    return apiClient<LoginResponse>('/api/auth/google', {
      method: 'POST',
      body: JSON.stringify(data),
      headers: authRequestHeaders(),
      auth: false,
      timeoutMs: AUTH_REQUEST_TIMEOUT_MS,
      timeoutMessage: AUTH_TIMEOUT_MESSAGES.login,
    });
  },

  createGoogleChallenge: (): Promise<GoogleAuthChallengeResponse> => {
    return apiClient<GoogleAuthChallengeResponse>('/api/auth/google/challenge', {
      method: 'POST',
      headers: authRequestHeaders(),
      auth: false,
      timeoutMessage: 'Google sign-in could not be prepared. Please try again.',
    });
  },

  createGoogleLinkChallenge: (currentPassword: string): Promise<GoogleAuthChallengeResponse> => {
    return apiClient<GoogleAuthChallengeResponse>('/api/auth/google/link/challenge', {
      method: 'POST',
      body: JSON.stringify({ currentPassword }),
      headers: authRequestHeaders(),
      timeoutMessage: 'Google connection could not be prepared. Check your password and try again.',
    });
  },

  linkGoogleIdentity: (data: GoogleLoginRequest): Promise<GoogleIdentityStatusResponse> => {
    return apiClient<GoogleIdentityStatusResponse>('/api/auth/google/link', {
      method: 'POST',
      body: JSON.stringify(data),
      headers: authRequestHeaders(),
      timeoutMs: AUTH_REQUEST_TIMEOUT_MS,
      timeoutMessage: 'Google connection timed out. Please try again.',
    });
  },

  getGoogleIdentityStatus: (signal?: AbortSignal): Promise<GoogleIdentityStatusResponse> => {
    return apiClient<GoogleIdentityStatusResponse>('/api/auth/google/identity', {
      method: 'GET',
      signal,
    });
  },

  getMe: (signal?: AbortSignal): Promise<User> => {
    return apiClient<User>('/api/auth/me', {
      method: 'GET',
      signal,
    });
  },
};

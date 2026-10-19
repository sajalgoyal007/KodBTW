export interface User {
  id: number;
  name: string;
  email: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: User;
}

export interface GoogleLoginRequest {
  credential: string;
  challengeId: string;
}

export interface GoogleAuthChallengeResponse {
  challengeId: string;
  nonce: string;
  expiresAt: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}

export interface ApiErrorResponse {
  error?: string;
  code?: string;
  message?: string;
  details?: Record<string, string>;
  status?: number;
}

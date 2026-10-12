import { AUTH_TIMEOUT_MESSAGES } from './authTimeoutMessages';

export class RegistrationAutoLoginError extends Error {
  constructor(readonly originalError: unknown) {
    super('Automatic sign-in failed after registration');
    this.name = 'RegistrationAutoLoginError';
  }
}

export async function runPostRegistrationLogin(login: () => Promise<void>): Promise<void> {
  try {
    await login();
  } catch (error) {
    throw new RegistrationAutoLoginError(error);
  }
}

export function formatRegistrationAutoLoginFailure(): string {
  return 'Your account was created, but automatic sign-in failed. Please sign in from the login page.';
}

export function formatAuthApiError(status: number, flow: 'login' | 'registration'): string {
  if (status === 0) return 'Unable to reach KodBTW. Check your connection and try again.';
  if (status === 408) {
    return flow === 'login' ? AUTH_TIMEOUT_MESSAGES.login : AUTH_TIMEOUT_MESSAGES.registration;
  }
  if (status === 401) return 'Invalid email or password.';
  if (status === 400) return 'Please check the information you entered.';
  if (status === 409 && flow === 'registration') return 'An account with this email already exists.';
  if (status === 429) return 'Too many requests. Please wait a moment and try again.';
  if (status >= 500) return 'KodBTW could not complete the request. Please try again later.';
  return 'The request could not be completed. Please try again.';
}

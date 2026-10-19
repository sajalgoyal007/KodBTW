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

export function formatGoogleAuthError(status: number, code?: string): string {
  switch (code) {
    case 'GOOGLE_ACCOUNT_REQUIRES_EXISTING_SIGN_IN':
      return 'An account already exists for this email. Sign in with your existing method, then connect Google from your profile.';
    case 'GOOGLE_AUTH_NOT_CONFIGURED':
      return 'Google sign-in is not configured on the server. Please use email and password.';
    case 'GOOGLE_VERIFICATION_UNAVAILABLE':
      return 'Google could not verify your sign-in right now. Please try again.';
    case 'INVALID_GOOGLE_CREDENTIAL':
      return 'Google sign-in could not be verified. Please try again.';
    case 'GOOGLE_IDENTITY_CONFLICT':
      return 'Another sign-in request updated this account. Please try again.';
    default:
      return formatAuthApiError(status, 'login');
  }
}

export function formatGoogleLinkError(status: number, code?: string): string {
  if (code === 'GOOGLE_LINK_REAUTHENTICATION_REQUIRED') {
    return 'That password could not be verified. Check it and try again.';
  }
  if (code === 'GOOGLE_IDENTITY_CONFLICT') {
    return 'This Google account is already connected elsewhere, or this KodBTW account already has a different Google account.';
  }
  if (code === 'INVALID_GOOGLE_CREDENTIAL') return 'Google could not verify this account. Please try again.';
  return formatAuthApiError(status, 'login');
}

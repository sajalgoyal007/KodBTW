import type { PlatformType } from '../types/platform';

export interface PlatformAccountFormValues {
  username: string;
  profileUrl: string;
}

export type PlatformAccountFieldErrors = Partial<Record<'username' | 'profileUrl', string>>;

export async function runSingleSubmission(
  lock: { current: boolean },
  action: () => Promise<void>,
): Promise<boolean> {
  if (lock.current) return false;
  lock.current = true;
  try {
    await action();
    return true;
  } finally {
    lock.current = false;
  }
}

export function validatePlatformAccountForm(
  values: PlatformAccountFormValues,
  platform?: PlatformType,
): PlatformAccountFieldErrors {
  const errors: PlatformAccountFieldErrors = {};
  const username = values.username.trim();
  const profileUrl = values.profileUrl.trim();

  if (!username) {
    errors.username = 'Username is required.';
  } else if (username.length > 100) {
    errors.username = 'Username must not exceed 100 characters.';
  }

  if (profileUrl) {
    try {
      const parsed = new URL(profileUrl);
      if ((parsed.protocol !== 'http:' && parsed.protocol !== 'https:') || !parsed.hostname) {
        errors.profileUrl = 'Enter a valid HTTP or HTTPS profile URL.';
      }
    } catch {
      errors.profileUrl = 'Enter a valid HTTP or HTTPS profile URL.';
    }
  } else if (platform === 'ALGOMASTER') {
    errors.profileUrl = 'AlgoMaster is link-only; a public profile URL is required.';
  }

  return errors;
}

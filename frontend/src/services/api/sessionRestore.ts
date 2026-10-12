import type { User } from '../../types/auth';

export type SessionRestoreResult =
  | { status: 'anonymous' }
  | { status: 'authenticated'; user: User }
  | { status: 'unavailable' }
  | { status: 'cancelled' };

function isAbortError(error: unknown): boolean {
  return typeof error === 'object'
    && error !== null
    && 'name' in error
    && error.name === 'AbortError';
}

function isUnauthorizedError(error: unknown): boolean {
  return typeof error === 'object'
    && error !== null
    && 'status' in error
    && error.status === 401;
}

/** Validate a saved session without treating temporary API failures as token revocation. */
export async function restoreSession(
  token: string | null,
  loadCurrentUser: (signal?: AbortSignal) => Promise<User>,
  clearStoredToken: () => void,
  signal?: AbortSignal,
): Promise<SessionRestoreResult> {
  if (!token) return { status: 'anonymous' };
  if (signal?.aborted) return { status: 'cancelled' };

  try {
    const user = await loadCurrentUser(signal);
    return signal?.aborted ? { status: 'cancelled' } : { status: 'authenticated', user };
  } catch (error) {
    if (signal?.aborted || isAbortError(error)) return { status: 'cancelled' };
    if (isUnauthorizedError(error)) {
      clearStoredToken();
      return { status: 'anonymous' };
    }
    return { status: 'unavailable' };
  }
}

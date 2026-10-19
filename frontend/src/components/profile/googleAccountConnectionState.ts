export type GoogleAccountConnectionView = 'loading' | 'error' | 'connected' | 'disconnected';

export function getGoogleAccountConnectionView(
  checkingStatus: boolean,
  statusError: string | null,
  connected: boolean | null,
): GoogleAccountConnectionView {
  if (checkingStatus || connected === null && !statusError) return 'loading';
  if (statusError) return 'error';
  return connected ? 'connected' : 'disconnected';
}

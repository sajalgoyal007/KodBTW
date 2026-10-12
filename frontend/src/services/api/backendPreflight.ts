export type BackendPreflightStatus = 'checking' | 'reachable' | 'stale' | 'failed';

const REACHABILITY_CACHE_MS = 5 * 60_000;

export function createBackendPreflight(
  checkHealth: () => Promise<unknown>,
  options: { now?: () => number; cacheTtlMs?: number } = {},
) {
  const now = options.now ?? Date.now;
  const cacheTtlMs = options.cacheTtlMs ?? REACHABILITY_CACHE_MS;
  let status: BackendPreflightStatus = 'checking';
  let inFlight: Promise<boolean> | null = null;
  let started = false;
  let lastSuccessAt: number | null = null;
  const listeners = new Set<(status: BackendPreflightStatus) => void>();

  const hasFreshReachability = () => status === 'reachable'
    && lastSuccessAt !== null
    && now() - lastSuccessAt < cacheTtlMs;

  const getFreshnessRemaining = () => lastSuccessAt === null
    ? 0
    : Math.max(0, cacheTtlMs - (now() - lastSuccessAt));

  const expireIfStale = () => {
    if (status === 'reachable' && !hasFreshReachability()) setStatus('stale');
  };

  const setStatus = (nextStatus: BackendPreflightStatus) => {
    status = nextStatus;
    listeners.forEach((listener) => listener(status));
  };

  const check = (): Promise<boolean> => {
    if (inFlight) return inFlight;
    if (hasFreshReachability()) return Promise.resolve(true);

    setStatus('checking');
    inFlight = Promise.resolve()
      .then(checkHealth)
      .then(
        () => {
          lastSuccessAt = now();
          setStatus('reachable');
          return true;
        },
        () => {
          lastSuccessAt = null;
          setStatus('failed');
          return false;
        },
      )
      .finally(() => {
        inFlight = null;
      });

    return inFlight;
  };

  return {
    getStatus: () => status,
    getFreshnessRemaining,
    expireIfStale,
    subscribe(listener: (status: BackendPreflightStatus) => void) {
      listeners.add(listener);
      return () => {
        listeners.delete(listener);
      };
    },
    start(): Promise<boolean> {
      if (inFlight) return inFlight;
      if (started && status === 'failed') return Promise.resolve(false);
      started = true;
      return check();
    },
    check,
    canSubmit: hasFreshReachability,
    async submitIfReady(submit: () => Promise<void>): Promise<boolean> {
      if (!hasFreshReachability()) {
        // A submit after the cache expires is user-initiated: refresh reachability
        // now, but require a separate explicit submit before sending credentials.
        if (status === 'reachable' && !inFlight) void check();
        return false;
      }
      await submit();
      return true;
    },
  };
}

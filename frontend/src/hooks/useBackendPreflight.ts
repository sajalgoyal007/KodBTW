import { useEffect, useState } from 'react';
import { checkBackendReachability } from '../services/api/healthApi';
import { createBackendPreflight } from '../services/api/backendPreflight';

const backendPreflight = createBackendPreflight(checkBackendReachability);

export function useBackendPreflight() {
  const [status, setStatus] = useState(backendPreflight.getStatus());

  useEffect(() => {
    let mounted = true;
    let freshnessTimer: number | undefined;
    const clearFreshnessTimer = () => {
      if (freshnessTimer !== undefined) {
        window.clearTimeout(freshnessTimer);
        freshnessTimer = undefined;
      }
    };
    const scheduleFreshnessExpiry = () => {
      clearFreshnessTimer();
      if (backendPreflight.getStatus() !== 'reachable') return;
      freshnessTimer = window.setTimeout(() => {
        backendPreflight.expireIfStale();
      }, backendPreflight.getFreshnessRemaining());
    };

    const unsubscribe = backendPreflight.subscribe((nextStatus) => {
      if (!mounted) return;
      setStatus(nextStatus);
      if (nextStatus === 'reachable') scheduleFreshnessExpiry();
      else clearFreshnessTimer();
    });

    void backendPreflight.start().then(() => {
      if (mounted) scheduleFreshnessExpiry();
    });
    return () => {
      mounted = false;
      clearFreshnessTimer();
      unsubscribe();
    };
  }, []);

  return {
    status,
    submitIfReady: (submit: () => Promise<void>) => backendPreflight.submitIfReady(submit),
    retry: () => backendPreflight.check(),
  };
}

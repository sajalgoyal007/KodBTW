import { useCallback, useEffect, useState } from 'react';
import { DashboardActivityResponse, DashboardContestIntelligenceResponse } from '../types/activityContest';
import { getDashboardActivity, getDashboardContestIntelligence } from '../services/api/activityContestApi';

export function useActivityContestIntelligence() {
  const [activity, setActivity] = useState<DashboardActivityResponse | null>(null);
  const [contests, setContests] = useState<DashboardContestIntelligenceResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [retryCount, setRetryCount] = useState(0);
  const refetch = useCallback(() => setRetryCount((count) => count + 1), []);

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError(null);
    Promise.all([getDashboardActivity(), getDashboardContestIntelligence()])
      .then(([activityResponse, contestResponse]) => {
        if (active) {
          setActivity(activityResponse);
          setContests(contestResponse);
        }
      })
      .catch((err: unknown) => {
        if (active) setError(err instanceof Error ? err.message : 'Unable to load activity and contest intelligence.');
      })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [retryCount]);

  return { activity, contests, loading, error, refetch };
}

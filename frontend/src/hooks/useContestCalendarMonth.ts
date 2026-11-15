import { useCallback, useEffect, useRef, useState } from 'react';
import { getContestCalendarMonth } from '../services/api/contestCalendarApi';
import { ContestMonthCalendarResponse } from '../types/contestCalendarMonth';

export function useContestCalendarMonth(year: number, month: number) {
  const [response, setResponse] = useState<ContestMonthCalendarResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [reloadToken, setReloadToken] = useState(0);
  const requestId = useRef(0);

  useEffect(() => {
    const currentRequest = requestId.current + 1;
    requestId.current = currentRequest;
    setLoading(true);
    setError(null);

    getContestCalendarMonth({ year, month })
      .then((result) => {
        if (requestId.current !== currentRequest) return;
        setResponse(result);
      })
      .catch(() => {
        if (requestId.current !== currentRequest) return;
        setError('The contest calendar could not be loaded. Please try again.');
      })
      .finally(() => {
        if (requestId.current !== currentRequest) return;
        setLoading(false);
      });
  }, [year, month, reloadToken]);

  const refetch = useCallback(() => setReloadToken((token) => token + 1), []);

  return { response, loading, error, refetch };
}

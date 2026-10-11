import { useCallback, useEffect, useRef, useState } from 'react';
import { getContestCalendar } from '../services/api/contestCalendarApi';
import { ContestCalendarResponse } from '../types/contestCalendar';
import { markContestCalendarStale } from '../utils/contestCalendar';

export function useContestCalendar() {
  const [response, setResponse] = useState<ContestCalendarResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const mounted = useRef(false);
  const requestInFlight = useRef(false);

  const refetch = useCallback(async () => {
    if (requestInFlight.current) return;
    requestInFlight.current = true;
    if (mounted.current) {
      setLoading(true);
      setError(null);
    }
    try {
      const result = await getContestCalendar();
      if (mounted.current) setResponse(result);
    } catch {
      if (mounted.current) {
        setError('The contest schedule could not be loaded. Please retry.');
        setResponse((current) => current ? markContestCalendarStale(current) : null);
      }
    } finally {
      requestInFlight.current = false;
      if (mounted.current) setLoading(false);
    }
  }, []);

  useEffect(() => {
    mounted.current = true;
    void refetch();
    return () => { mounted.current = false; };
  }, [refetch]);

  return { response, loading, error, refetch };
}

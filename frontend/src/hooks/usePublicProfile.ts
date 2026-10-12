import { useState, useEffect, useCallback, useRef } from 'react';
import { PublicProfileData } from '../types/publicProfile';
import { publicProfileService } from '../services/publicProfileService';
import { ApiError } from '../services/api/apiClient';
import { createLatestRequestTracker } from '../utils/requestCoordination';

interface UsePublicProfileResult {
  data: PublicProfileData | null;
  loading: boolean;
  error: string | null;
  notFound: boolean;
  refetch: () => Promise<void>;
}

export function usePublicProfile(username: string | undefined): UsePublicProfileResult {
  const [data, setData] = useState<PublicProfileData | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [notFound, setNotFound] = useState<boolean>(false);
  const requestTrackerRef = useRef(createLatestRequestTracker());

  const fetchProfile = useCallback(async () => {
    const requestVersion = requestTrackerRef.current.begin();
    if (!username) {
      setData(null);
      setError(null);
      setLoading(false);
      setNotFound(true);
      return;
    }

    setLoading(true);
    setError(null);
    setNotFound(false);
    try {
      const result = await publicProfileService.getPublicProfile(username);
      if (!requestTrackerRef.current.isCurrent(requestVersion)) return;
      setData(result);
    } catch (err: unknown) {
      if (!requestTrackerRef.current.isCurrent(requestVersion)) return;
      if (err instanceof ApiError && err.status === 404) {
        setNotFound(true);
      } else {
        setError(err instanceof ApiError ? err.message : 'Failed to load developer profile. Please try again.');
      }
      setData(null);
    } finally {
      if (requestTrackerRef.current.isCurrent(requestVersion)) setLoading(false);
    }
  }, [username]);

  useEffect(() => {
    fetchProfile();
    return () => requestTrackerRef.current.invalidate();
  }, [fetchProfile]);

  return {
    data,
    loading,
    error,
    notFound,
    refetch: fetchProfile,
  };
}

export default usePublicProfile;

import { useState, useEffect, useCallback } from 'react';
import { PublicProfileData } from '../types/publicProfile';
import { publicProfileService } from '../services/publicProfileService';
import { ApiError } from '../services/api/apiClient';

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

  const fetchProfile = useCallback(async () => {
    if (!username) {
      setLoading(false);
      setNotFound(true);
      return;
    }

    setLoading(true);
    setError(null);
    setNotFound(false);
    try {
      const result = await publicProfileService.getPublicProfile(username);
      setData(result);
    } catch (err: any) {
      if (err instanceof ApiError && err.status === 404) {
        setNotFound(true);
      } else {
        setError(err?.message || 'Failed to load developer profile');
      }
      setData(null);
    } finally {
      setLoading(false);
    }
  }, [username]);

  useEffect(() => {
    fetchProfile();
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

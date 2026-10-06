import { useState, useEffect, useCallback } from 'react';
import { ProfileResponse, ProfileRequest } from '../types/profile';
import { getProfile, updateProfile } from '../services/api/profileApi';
import { ApiError } from '../services/api/apiClient';

interface UseProfileResult {
  profile: ProfileResponse | null;
  loading: boolean;
  saving: boolean;
  error: string | null;
  isInitialSetup: boolean;
  fetchProfile: () => Promise<void>;
  saveProfile: (data: ProfileRequest) => Promise<ProfileResponse>;
}

export function useProfile(): UseProfileResult {
  const [profile, setProfile] = useState<ProfileResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [saving, setSaving] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [isInitialSetup, setIsInitialSetup] = useState<boolean>(false);

  const fetchProfile = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getProfile();
      setProfile(data);
      setIsInitialSetup(false);
    } catch (err: any) {
      if (err instanceof ApiError && err.status === 404) {
        // 404 is treated as uninitialized profile / setup state, not an error
        setProfile(null);
        setIsInitialSetup(true);
      } else {
        setError(err?.message || 'Failed to fetch profile.');
      }
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchProfile();
  }, [fetchProfile]);

  const saveProfile = async (data: ProfileRequest): Promise<ProfileResponse> => {
    setSaving(true);
    setError(null);
    try {
      const updated = await updateProfile(data);
      setProfile(updated);
      setIsInitialSetup(false);
      return updated;
    } catch (err: any) {
      throw err;
    } finally {
      setSaving(false);
    }
  };

  return {
    profile,
    loading,
    saving,
    error,
    isInitialSetup,
    fetchProfile,
    saveProfile,
  };
}

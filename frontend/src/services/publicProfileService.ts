import { apiClient } from './api/apiClient';
import { PublicProfileData } from '../types/publicProfile';

export const publicProfileService = {
  getPublicProfile: (username: string): Promise<PublicProfileData> => {
    return apiClient<PublicProfileData>(`/api/public/profiles/${encodeURIComponent(username)}`);
  },
};

export default publicProfileService;

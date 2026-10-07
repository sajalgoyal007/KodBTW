import { apiClient } from './apiClient';
import {
  LeaderboardQueryParams,
  LeaderboardResponse,
  MyRankResponse,
} from '../../types/leaderboard';

export async function getLeaderboard(
  params: LeaderboardQueryParams = {}
): Promise<LeaderboardResponse> {
  const { college, ...rest } = params;
  if (college && college.trim().length > 0) {
    return apiClient<LeaderboardResponse>('/api/leaderboard/college', {
      params: {
        college: college.trim(),
        ...rest,
      },
    });
  }
  return apiClient<LeaderboardResponse>('/api/leaderboard', {
    params: rest,
  });
}

export async function getMyRank(): Promise<MyRankResponse> {
  return apiClient<MyRankResponse>('/api/leaderboard/me');
}

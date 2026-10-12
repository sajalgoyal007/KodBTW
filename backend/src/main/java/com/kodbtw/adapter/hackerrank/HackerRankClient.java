package com.kodbtw.adapter.hackerrank;

import com.kodbtw.adapter.hackerrank.dto.HackerRankStatsResponse;

public interface HackerRankClient {
    HackerRankStatsResponse fetchStats(String username);
}

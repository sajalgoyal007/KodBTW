package com.kodbtw.adapter.hackerrank;

import com.kodbtw.adapter.hackerrank.dto.HackerRankPublicStats;

public interface HackerRankClient {

    HackerRankPublicStats fetchStats(String username);
}

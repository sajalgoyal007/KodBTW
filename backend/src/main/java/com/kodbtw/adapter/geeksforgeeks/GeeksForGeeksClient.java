package com.kodbtw.adapter.geeksforgeeks;

import com.kodbtw.adapter.geeksforgeeks.dto.GeeksForGeeksStatsResponse;

public interface GeeksForGeeksClient {
    GeeksForGeeksStatsResponse fetchStats(String username);
}

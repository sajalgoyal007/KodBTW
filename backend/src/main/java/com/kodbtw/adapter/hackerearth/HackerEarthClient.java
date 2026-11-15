package com.kodbtw.adapter.hackerearth;

import com.kodbtw.adapter.hackerearth.dto.HackerEarthStats;

public interface HackerEarthClient {

    HackerEarthStats fetchStats(String username);
}

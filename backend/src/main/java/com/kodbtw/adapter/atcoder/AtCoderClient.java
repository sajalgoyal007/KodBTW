package com.kodbtw.adapter.atcoder;

import com.kodbtw.adapter.atcoder.dto.AtCoderStats;

public interface AtCoderClient {

    AtCoderStats fetchStats(String username);
}

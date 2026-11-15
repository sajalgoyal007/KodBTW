package com.kodbtw.adapter.code360;

import com.kodbtw.adapter.code360.dto.Code360Stats;

public interface Code360Client {

    Code360Stats fetchStats(String username);
}

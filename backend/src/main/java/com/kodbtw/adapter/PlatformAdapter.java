package com.kodbtw.adapter;

import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;

public interface PlatformAdapter {

    Platform getPlatform();

    PlatformStats fetchStats(PlatformAccount account);
}

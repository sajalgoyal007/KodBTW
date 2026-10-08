package com.kodbtw.adapter.impl;

import com.kodbtw.adapter.PlatformAdapter;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;
import com.kodbtw.exception.PlatformSyncUnavailableException;
import org.springframework.stereotype.Component;

@Component
public class GeeksForGeeksAdapter implements PlatformAdapter {

    @Override
    public Platform getPlatform() {
        return Platform.GEEKSFORGEEKS;
    }

    @Override
    public PlatformStats fetchStats(PlatformAccount account) {
        throw new PlatformSyncUnavailableException("GeeksforGeeks live sync unavailable");
    }
}

package com.kodbtw.adapter.impl;

import com.kodbtw.adapter.PlatformAdapter;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;
import com.kodbtw.exception.PlatformSyncUnavailableException;
import org.springframework.stereotype.Component;

@Component
public class CodeChefAdapter implements PlatformAdapter {

    @Override
    public Platform getPlatform() {
        return Platform.CODECHEF;
    }

    @Override
    public PlatformStats fetchStats(PlatformAccount account) {
        throw new PlatformSyncUnavailableException("CodeChef live sync unavailable");
    }
}

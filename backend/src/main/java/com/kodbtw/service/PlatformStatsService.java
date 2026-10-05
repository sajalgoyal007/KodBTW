package com.kodbtw.service;

import com.kodbtw.adapter.PlatformAdapter;
import com.kodbtw.adapter.PlatformAdapterRegistry;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.PlatformAccount;
import com.kodbtw.exception.ResourceNotFoundException;
import com.kodbtw.repository.PlatformAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlatformStatsService {

    private final PlatformAccountRepository platformAccountRepository;
    private final PlatformAdapterRegistry platformAdapterRegistry;

    public PlatformStatsService(PlatformAccountRepository platformAccountRepository,
                                PlatformAdapterRegistry platformAdapterRegistry) {
        this.platformAccountRepository = platformAccountRepository;
        this.platformAdapterRegistry = platformAdapterRegistry;
    }

    @Transactional(readOnly = true)
    public PlatformStats getStats(Long userId, Long accountId) {
        PlatformAccount account = platformAccountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Platform account not found"));

        return fetchStats(account);
    }

    @Transactional(readOnly = true)
    public PlatformStats fetchStats(PlatformAccount account) {
        PlatformAdapter adapter = platformAdapterRegistry.getAdapter(account.getPlatform());
        return adapter.fetchStats(account);
    }
}

package com.kodbtw.service;

import com.kodbtw.adapter.PlatformAdapter;
import com.kodbtw.adapter.PlatformAdapterRegistry;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;
import com.kodbtw.exception.ResourceNotFoundException;
import com.kodbtw.repository.PlatformAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatformStatsServiceTest {

    @Mock
    private PlatformAccountRepository platformAccountRepository;

    @Mock
    private PlatformAdapter platformAdapter;

    private PlatformAdapterRegistry platformAdapterRegistry;
    private PlatformStatsService platformStatsService;

    @BeforeEach
    void setUp() {
        org.mockito.Mockito.lenient().when(platformAdapter.getPlatform()).thenReturn(Platform.LEETCODE);
        platformAdapterRegistry = new PlatformAdapterRegistry(java.util.List.of(platformAdapter));
        platformStatsService = new PlatformStatsService(platformAccountRepository, platformAdapterRegistry);
    }

    @Test
    void getStatsShouldDelegateToCorrectAdapterWhenAccountFound() {
        Long userId = 1L;
        Long accountId = 10L;

        PlatformAccount account = new PlatformAccount();
        account.setId(accountId);
        account.setPlatform(Platform.LEETCODE);
        account.setUsername("testuser");

        PlatformStats expectedStats = PlatformStats.builder()
                .platform(Platform.LEETCODE)
                .username("testuser")
                .totalProblemsSolved(350)
                .source("MOCK")
                .build();

        when(platformAccountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(account));
        when(platformAdapter.fetchStats(account)).thenReturn(expectedStats);

        PlatformStats result = platformStatsService.getStats(userId, accountId);

        assertNotNull(result);
        assertEquals(Platform.LEETCODE, result.getPlatform());
        assertEquals("testuser", result.getUsername());
        assertEquals(350, result.getTotalProblemsSolved());
        verify(platformAdapter).fetchStats(account);
    }

    @Test
    void getStatsShouldThrowResourceNotFoundWhenAccountNotOwnedOrNotFound() {
        Long userId = 1L;
        Long accountId = 99L;

        when(platformAccountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> platformStatsService.getStats(userId, accountId));
    }
}

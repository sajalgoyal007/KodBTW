package com.kodbtw.adapter;

import com.kodbtw.adapter.codechef.CodeChefClient;
import com.kodbtw.adapter.codechef.dto.CodeChefApiResponse;
import com.kodbtw.adapter.impl.CodeChefAdapter;
import com.kodbtw.dto.PlatformStats;
import com.kodbtw.entity.Platform;
import com.kodbtw.entity.PlatformAccount;
import com.kodbtw.exception.PlatformApiException;
import com.kodbtw.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodeChefAdapterTest {

    @Mock
    private CodeChefClient client;

    private CodeChefAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CodeChefAdapter(client);
    }

    @Test
    void shouldReturnCodeChefPlatform() {
        assertEquals(Platform.CODECHEF, adapter.getPlatform());
    }

    @Test
    void shouldMapThirdPartyProfileWithoutInventingMissingMetrics() {
        String username = "sajalgoyal2007";
        PlatformAccount account = new PlatformAccount();
        account.setUsername(username);
        account.setPlatform(Platform.CODECHEF);

        CodeChefApiResponse.Profile profile = new CodeChefApiResponse.Profile(
                "https://www.codechef.com/users/" + username,
                "Test User",
                1247,
                1519,
                null,
                "India",
                76970,
                73803,
                "1★",
                656
        );

        when(client.fetchProfile(username)).thenReturn(
                new CodeChefApiResponse(true, 200, username, profile, "OK", "CodeChef")
        );

        PlatformStats stats = adapter.fetchStats(account);

        assertEquals(Platform.CODECHEF, stats.getPlatform());
        assertEquals(username, stats.getUsername());
        assertEquals("https://www.codechef.com/users/" + username, stats.getProfileUrl());
        assertEquals(656, stats.getTotalProblemsSolved());
        assertEquals(1247, stats.getRating());
        assertEquals(1519, stats.getMaxRating());
        assertEquals(76970, stats.getRank());
        assertNull(stats.getEasySolved());
        assertNull(stats.getMediumSolved());
        assertNull(stats.getHardSolved());
        assertNull(stats.getContestsParticipated());
        assertNotNull(stats.getLastSyncedAt());
        assertEquals("CODECHEF_THIRD_PARTY", stats.getSource());
    }

    @Test
    void shouldPropagateResourceNotFoundWhenProfileDoesNotExist() {
        PlatformAccount account = new PlatformAccount();
        account.setUsername("missinguser");
        account.setPlatform(Platform.CODECHEF);

        when(client.fetchProfile("missinguser"))
                .thenThrow(new ResourceNotFoundException("CodeChef profile not found"));

        assertThrows(ResourceNotFoundException.class, () -> adapter.fetchStats(account));
    }

    @Test
    void shouldPropagateUpstreamFailureWithoutMockFallback() {
        PlatformAccount account = new PlatformAccount();
        account.setUsername("sajalgoyal2007");
        account.setPlatform(Platform.CODECHEF);

        when(client.fetchProfile("sajalgoyal2007"))
                .thenThrow(new PlatformApiException("CodeChef provider unavailable"));

        PlatformApiException exception = assertThrows(
                PlatformApiException.class,
                () -> adapter.fetchStats(account)
        );

        assertEquals("CodeChef provider unavailable", exception.getMessage());
    }
}

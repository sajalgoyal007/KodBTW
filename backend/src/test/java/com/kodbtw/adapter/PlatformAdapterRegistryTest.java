package com.kodbtw.adapter;

import com.kodbtw.adapter.impl.CodeChefAdapter;
import com.kodbtw.adapter.codechef.CodeChefClient;
import com.kodbtw.adapter.impl.CodeforcesAdapter;
import com.kodbtw.adapter.impl.GeeksForGeeksAdapter;
import com.kodbtw.adapter.impl.HackerRankAdapter;
import com.kodbtw.adapter.impl.LeetCodeAdapter;
import com.kodbtw.entity.Platform;
import com.kodbtw.exception.UnsupportedPlatformException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlatformAdapterRegistryTest {

    private PlatformAdapterRegistry registry;

    @BeforeEach
    void setUp() {
        List<PlatformAdapter> adapters = List.of(
                new LeetCodeAdapter(username -> null),
                new CodeChefAdapter((CodeChefClient) handle -> null),
                new CodeforcesAdapter(handle -> null),
                new GeeksForGeeksAdapter(),
                new HackerRankAdapter()
        );
        registry = new PlatformAdapterRegistry(adapters);
    }

    @Test
    void shouldReturnCorrectAdapterForEachPlatform() {
        assertInstanceOf(LeetCodeAdapter.class, registry.getAdapter(Platform.LEETCODE));
        assertInstanceOf(CodeChefAdapter.class, registry.getAdapter(Platform.CODECHEF));
        assertInstanceOf(CodeforcesAdapter.class, registry.getAdapter(Platform.CODEFORCES));
        assertInstanceOf(GeeksForGeeksAdapter.class, registry.getAdapter(Platform.GEEKSFORGEEKS));
        assertInstanceOf(HackerRankAdapter.class, registry.getAdapter(Platform.HACKERRANK));
    }

    @Test
    void shouldThrowExceptionWhenPlatformIsNull() {
        assertThrows(UnsupportedPlatformException.class, () -> registry.getAdapter(null));
    }

    @Test
    void shouldThrowExceptionWhenPlatformNotRegistered() {
        PlatformAdapterRegistry emptyRegistry = new PlatformAdapterRegistry(List.of());
        assertThrows(UnsupportedPlatformException.class, () -> emptyRegistry.getAdapter(Platform.LEETCODE));
    }
}

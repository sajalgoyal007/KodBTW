package com.kodbtw.adapter;

import com.kodbtw.adapter.atcoder.AtCoderClient;
import com.kodbtw.adapter.code360.Code360Client;
import com.kodbtw.adapter.codechef.CodeChefClient;
import com.kodbtw.adapter.geeksforgeeks.GeeksForGeeksClient;
import com.kodbtw.adapter.hackerearth.HackerEarthClient;
import com.kodbtw.adapter.hackerrank.HackerRankClient;
import com.kodbtw.adapter.impl.AlgoMasterAdapter;
import com.kodbtw.adapter.impl.AtCoderAdapter;
import com.kodbtw.adapter.impl.Code360Adapter;
import com.kodbtw.adapter.impl.CodeChefAdapter;
import com.kodbtw.adapter.impl.CodeforcesAdapter;
import com.kodbtw.adapter.impl.GeeksForGeeksAdapter;
import com.kodbtw.adapter.impl.HackerEarthAdapter;
import com.kodbtw.adapter.impl.HackerRankAdapter;
import com.kodbtw.adapter.impl.InterviewBitAdapter;
import com.kodbtw.adapter.impl.LeetCodeAdapter;
import com.kodbtw.adapter.interviewbit.InterviewBitClient;
import com.kodbtw.entity.Platform;
import com.kodbtw.exception.UnsupportedPlatformException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlatformAdapterRegistryTest {

    private PlatformAdapterRegistry registry;

    @BeforeEach
    void setUp() {
        List<PlatformAdapter> adapters = List.of(
                new LeetCodeAdapter(username -> null),
                new CodeChefAdapter((CodeChefClient) handle -> null),
                new CodeforcesAdapter(handle -> null),
                new AtCoderAdapter((AtCoderClient) username -> null),
                new GeeksForGeeksAdapter((GeeksForGeeksClient) username -> null),
                new Code360Adapter((Code360Client) username -> null),
                new HackerRankAdapter((HackerRankClient) username -> null),
                new InterviewBitAdapter((InterviewBitClient) username -> null),
                new HackerEarthAdapter((HackerEarthClient) username -> null),
                new AlgoMasterAdapter()
        );
        registry = new PlatformAdapterRegistry(adapters);
    }

    @Test
    void shouldReturnCorrectAdapterForEachPlatform() {
        assertInstanceOf(LeetCodeAdapter.class, registry.getAdapter(Platform.LEETCODE));
        assertInstanceOf(CodeChefAdapter.class, registry.getAdapter(Platform.CODECHEF));
        assertInstanceOf(CodeforcesAdapter.class, registry.getAdapter(Platform.CODEFORCES));
        assertInstanceOf(AtCoderAdapter.class, registry.getAdapter(Platform.ATCODER));
        assertInstanceOf(GeeksForGeeksAdapter.class, registry.getAdapter(Platform.GEEKSFORGEEKS));
        assertInstanceOf(Code360Adapter.class, registry.getAdapter(Platform.CODE360));
        assertInstanceOf(HackerRankAdapter.class, registry.getAdapter(Platform.HACKERRANK));
        assertInstanceOf(InterviewBitAdapter.class, registry.getAdapter(Platform.INTERVIEWBIT));
        assertInstanceOf(HackerEarthAdapter.class, registry.getAdapter(Platform.HACKEREARTH));
        assertInstanceOf(AlgoMasterAdapter.class, registry.getAdapter(Platform.ALGOMASTER));
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

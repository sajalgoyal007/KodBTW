package com.kodbtw.service;

import java.util.Locale;
import java.util.Set;

/** Shared allowlist for snapshot sources treated as real user statistics. */
public final class PlatformStatsSourcePolicy {
    private static final Set<String> ELIGIBLE_SOURCES = Set.of(
            "LEETCODE_REAL",
            "CODEFORCES_REAL",
            "CODECHEF_REAL",
            "CODECHEF_THIRD_PARTY"
    );

    private PlatformStatsSourcePolicy() {}

    public static boolean isEligibleRealSource(String source) {
        return source != null && ELIGIBLE_SOURCES.contains(source.trim().toUpperCase(Locale.ROOT));
    }
}

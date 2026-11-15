package com.kodbtw.contest;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * The eight competitive-programming platforms supported by Phase 1 of the
 * multi-platform contest calendar.
 *
 * <p>{@code clistResource} is the resource name used by the clist.by API when
 * requesting contests for that platform.</p>
 */
public enum ContestPlatform {

    CODEFORCES("Codeforces", "codeforces.com"),
    LEETCODE("LeetCode", "leetcode.com"),
    CODECHEF("CodeChef", "codechef.com"),
    ATCODER("AtCoder", "atcoder.jp"),
    GFG("GeeksforGeeks", "geeksforgeeks.org"),
    CODE360("Code360", "codingninjas.com"),
    HACKERRANK("HackerRank", "hackerrank.com"),
    HACKEREARTH("HackerEarth", "hackerearth.com");

    private final String displayName;
    private final String clistResource;

    ContestPlatform(String displayName, String clistResource) {
        this.displayName = displayName;
        this.clistResource = clistResource;
    }

    public String displayName() {
        return displayName;
    }

    public String clistResource() {
        return clistResource;
    }

    /** Resolves a stored/API platform code, accepting legacy aliases. */
    public static Optional<ContestPlatform> fromCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        if ("GEEKSFORGEEKS".equals(normalized)) {
            normalized = "GFG";
        } else if ("CODESTUDIO".equals(normalized)) {
            normalized = "CODE360";
        }
        final String resolved = normalized;
        return Arrays.stream(values())
                .filter(platform -> platform.name().equals(resolved))
                .findFirst();
    }

    /**
     * Resolves a clist.by resource name (for example {@code codeforces.com} or
     * {@code codingninjas.com}) to a supported platform.
     */
    public static Optional<ContestPlatform> fromResource(String resourceName) {
        if (resourceName == null || resourceName.isBlank()) {
            return Optional.empty();
        }
        String resource = resourceName.trim().toLowerCase(Locale.ROOT);
        if (resource.contains("codeforces")) {
            return Optional.of(CODEFORCES);
        }
        if (resource.contains("leetcode")) {
            return Optional.of(LEETCODE);
        }
        if (resource.contains("codechef")) {
            return Optional.of(CODECHEF);
        }
        if (resource.contains("atcoder")) {
            return Optional.of(ATCODER);
        }
        if (resource.contains("geeksforgeeks") || resource.contains("gfg")) {
            return Optional.of(GFG);
        }
        if (resource.contains("codingninjas") || resource.contains("codingninja")
                || resource.contains("code360") || resource.contains("naukri")) {
            return Optional.of(CODE360);
        }
        if (resource.contains("hackerearth")) {
            return Optional.of(HACKEREARTH);
        }
        if (resource.contains("hackerrank")) {
            return Optional.of(HACKERRANK);
        }
        return Optional.empty();
    }
}

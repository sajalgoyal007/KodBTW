package com.kodbtw.entity;

public enum Platform {
    LEETCODE,
    CODECHEF,
    CODEFORCES,
    ATCODER,
    GEEKSFORGEEKS,
    CODE360,
    HACKERRANK,
    INTERVIEWBIT,
    HACKEREARTH,
    ALGOMASTER;

    /**
     * Whether the platform has a stats adapter that performs a live fetch.
     *
     * <p>AlgoMaster is deliberately link-only: it is a learning roadmap platform without a
     * public user-stats API, so a saved profile URL is the only supported integration.
     */
    public boolean hasLiveStatsSource() {
        return this == LEETCODE
                || this == CODECHEF
                || this == CODEFORCES
                || this == ATCODER
                || this == GEEKSFORGEEKS
                || this == CODE360
                || this == HACKERRANK
                || this == INTERVIEWBIT
                || this == HACKEREARTH;
    }

    /** Link-only platforms save a profile URL but never fetch statistics. */
    public boolean isLinkOnly() {
        return this == ALGOMASTER;
    }
}

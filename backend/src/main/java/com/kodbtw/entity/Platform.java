package com.kodbtw.entity;

public enum Platform {
    LEETCODE,
    CODECHEF,
    CODEFORCES,
    GEEKSFORGEEKS,
    HACKERRANK;

    public boolean hasLiveStatsSource() {
        return this == LEETCODE || this == CODEFORCES;
    }
}

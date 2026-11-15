package com.kodbtw.entity;

/** Current availability/lifecycle of the statistics source for a connected platform. */
public enum PlatformSourceStatus {
    REAL_AVAILABLE,
    SOURCE_PENDING,
    LINK_ONLY,
    SYNCING,
    SYNCED,
    SYNC_FAILED
}

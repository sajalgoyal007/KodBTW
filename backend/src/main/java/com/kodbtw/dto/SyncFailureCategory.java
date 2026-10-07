package com.kodbtw.dto;

public enum SyncFailureCategory {
    TIMEOUT,
    RATE_LIMITED,
    ACCOUNT_NOT_FOUND,
    UPSTREAM_UNAVAILABLE,
    UNKNOWN
}

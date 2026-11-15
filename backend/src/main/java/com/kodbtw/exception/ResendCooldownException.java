package com.kodbtw.exception;

public class ResendCooldownException extends RuntimeException {

    private final long retryAfterSeconds;

    public ResendCooldownException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}

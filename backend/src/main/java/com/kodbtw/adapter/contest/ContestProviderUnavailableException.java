package com.kodbtw.adapter.contest;

public class ContestProviderUnavailableException extends RuntimeException {
    public ContestProviderUnavailableException() {
        super("Contest provider is unavailable");
    }

    public ContestProviderUnavailableException(Throwable cause) {
        super("Contest provider is unavailable", cause);
    }
}

package com.kodbtw.exception;

public class PlatformApiException extends RuntimeException {

    public PlatformApiException(String message) {
        super(message);
    }

    public PlatformApiException(String message, Throwable cause) {
        super(message, cause);
    }
}

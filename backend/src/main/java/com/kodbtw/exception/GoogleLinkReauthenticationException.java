package com.kodbtw.exception;

public class GoogleLinkReauthenticationException extends RuntimeException {
    public GoogleLinkReauthenticationException() {
        super("Please verify your current password before connecting Google.");
    }
}

package com.kodbtw.exception;

public class GoogleIdentityVerificationException extends RuntimeException {
    public GoogleIdentityVerificationException() { super("Google identity verification is temporarily unavailable"); }
}

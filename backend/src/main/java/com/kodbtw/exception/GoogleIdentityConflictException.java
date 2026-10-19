package com.kodbtw.exception;

public class GoogleIdentityConflictException extends RuntimeException {
    public GoogleIdentityConflictException() { super("This Google sign-in conflicted with another account update. Please retry."); }
}

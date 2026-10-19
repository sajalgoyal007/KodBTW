package com.kodbtw.exception;

public class GoogleAccountExistsException extends RuntimeException {
    public GoogleAccountExistsException() { super("An account already exists for this email. Sign in using your existing method; account linking is not available yet."); }
}

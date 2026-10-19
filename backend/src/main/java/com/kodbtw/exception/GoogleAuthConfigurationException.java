package com.kodbtw.exception;

public class GoogleAuthConfigurationException extends RuntimeException {
    public GoogleAuthConfigurationException() { super("Google sign-in is not configured"); }
}

package com.kodbtw.exception;

public class InvalidGoogleCredentialException extends RuntimeException {
    public InvalidGoogleCredentialException() { super("Google sign-in could not be verified"); }
}

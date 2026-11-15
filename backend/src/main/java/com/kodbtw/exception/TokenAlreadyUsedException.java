package com.kodbtw.exception;

public class TokenAlreadyUsedException extends RuntimeException {

    public TokenAlreadyUsedException(String message) {
        super(message);
    }
}

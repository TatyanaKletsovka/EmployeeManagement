package com.syberry.bakery.exception;

public class EmailException extends RuntimeException {
    public EmailException(String message) {
        super(message);
    }
    public EmailException(Throwable throwable, String message) {
        super(message, throwable);
    }
}

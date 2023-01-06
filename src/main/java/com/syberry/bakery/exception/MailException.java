package com.syberry.bakery.exception;

public class MailException extends RuntimeException {
    public MailException(String message) {
        super(message);
    }
    public MailException(Throwable throwable, String message) {
        super(message, throwable);
    }
}

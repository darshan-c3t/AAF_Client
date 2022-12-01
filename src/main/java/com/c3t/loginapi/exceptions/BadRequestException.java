package com.c3t.loginapi.exceptions;

public class BadRequestException extends RuntimeException{
    private static final long serialVersionID= 1l;

    public BadRequestException() {}

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }

}

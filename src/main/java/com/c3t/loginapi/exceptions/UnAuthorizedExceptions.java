package com.c3t.loginapi.exceptions;

public class UnAuthorizedExceptions extends RuntimeException{
    private static final long serialVersionID= 1l;

    public UnAuthorizedExceptions() {}

    public UnAuthorizedExceptions(String message) {
        super(message);
    }

    public UnAuthorizedExceptions(String message, Throwable cause) {
        super(message, cause);
    }

}

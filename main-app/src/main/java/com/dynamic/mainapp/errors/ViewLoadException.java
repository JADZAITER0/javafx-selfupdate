package com.example.selfupdate.testjavafxmvci.errors;

public class ViewLoadException extends RuntimeException {
    public ViewLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
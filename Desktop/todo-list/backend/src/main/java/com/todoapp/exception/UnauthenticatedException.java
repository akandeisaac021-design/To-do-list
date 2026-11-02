package com.todoapp.exception;

public class UnauthenticatedException extends RuntimeException {
    public UnauthenticatedException() {
        super("Not authenticated");
    }
}

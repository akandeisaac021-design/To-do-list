package com.todoapp.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        // Deliberately generic - doesn't reveal whether the username exists,
        // so login can't be used to enumerate registered accounts.
        super("Invalid username or password");
    }
}

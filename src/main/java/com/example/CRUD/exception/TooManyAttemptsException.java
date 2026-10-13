package com.example.CRUD.exception;

public class TooManyAttemptsException extends RuntimeException {
    public TooManyAttemptsException() {
        super("Too many failed login attempts. Try again later.");
    }
}
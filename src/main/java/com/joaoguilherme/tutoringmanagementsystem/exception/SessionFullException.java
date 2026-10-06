package com.joaoguilherme.tutoringmanagementsystem.exception;

public class SessionFullException extends RuntimeException {
    public SessionFullException(String message) {
        super(message);
    }
}

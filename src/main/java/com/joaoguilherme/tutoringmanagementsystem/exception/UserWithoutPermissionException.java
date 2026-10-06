package com.joaoguilherme.tutoringmanagementsystem.exception;

public class UserWithoutPermissionException extends RuntimeException {
    public UserWithoutPermissionException(String message) {
        super(message);
    }
}

package com.flavor.forge.Exception.CustomExceptions;

public class DatabaseCRUDException extends RuntimeException {
    public DatabaseCRUDException(String message) {
        super(message);
    }
    public DatabaseCRUDException(String message, Throwable cause) {
        super(message, cause);
    }
}
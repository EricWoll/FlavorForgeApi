package com.flavor.forge.Exception.CustomExceptions;

public class AnimalNotFoundException extends RuntimeException{

    public AnimalNotFoundException(String message) {
        super(message);
    }
    public AnimalNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

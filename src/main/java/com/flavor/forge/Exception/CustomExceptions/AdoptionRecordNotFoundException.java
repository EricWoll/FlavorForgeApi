package com.flavor.forge.Exception.CustomExceptions;

public class AdoptionRecordNotFoundException extends RuntimeException {
    public AdoptionRecordNotFoundException(String message) {
        super(message);
    }
    public AdoptionRecordNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

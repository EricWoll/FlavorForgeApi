package com.flavor.forge.Exception.CustomExceptions;

public class AdoptionRecordExistsException extends RuntimeException {
    public AdoptionRecordExistsException(String message) {
        super(message);
    }
    public AdoptionRecordExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}

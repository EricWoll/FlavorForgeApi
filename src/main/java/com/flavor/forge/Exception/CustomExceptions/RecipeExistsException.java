package com.flavor.forge.Exception.CustomExceptions;

public class RecipeExistsException extends RuntimeException{
    public RecipeExistsException(String message) {
        super(message);
    }
    public RecipeExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.flavor.forge.Exception.CustomExceptions;

public class RecipeNotFoundException extends RuntimeException{
    public RecipeNotFoundException(String message) {
        super(message);
    }
    public RecipeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

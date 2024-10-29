package com.flavor.forge.Exception.CustomExceptions;

public class RecipeEmptyException extends RuntimeException{
    public RecipeEmptyException(String message) {
        super(message);
    }
    public RecipeEmptyException(String message, Throwable cause) {
        super(message, cause);
    }
}

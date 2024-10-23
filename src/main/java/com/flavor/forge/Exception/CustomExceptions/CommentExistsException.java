package com.flavor.forge.Exception.CustomExceptions;

public class CommentExistsException extends RuntimeException{
    public CommentExistsException(String message) {
        super(message);
    }
    public CommentExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}

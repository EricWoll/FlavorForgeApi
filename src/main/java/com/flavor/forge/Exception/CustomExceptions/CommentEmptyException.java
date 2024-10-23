package com.flavor.forge.Exception.CustomExceptions;

public class CommentEmptyException extends RuntimeException{
    public CommentEmptyException(String message) {
        super(message);
    }
    public CommentEmptyException(String message, Throwable cause) {
        super(message, cause);
    }
}

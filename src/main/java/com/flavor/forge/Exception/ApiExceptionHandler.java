package com.flavor.forge.Exception;

import com.flavor.forge.Exception.CustomExceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(value = {
            CommentExistsException.class,
            CommentNotFoundException.class,
            CommentEmptyException.class,
            RecipeEmptyException.class,
            RecipeExistsException.class,
            RecipeNotFoundException.class,
            UserExistsException.class,
            UserNotFoundException.class
    })
    public ResponseEntity<Object> handleApiExceptions(Exception e) {
        HttpStatus badRequest = HttpStatus.BAD_REQUEST;

        ApiException apiException = new ApiException(
                e.getMessage(),
                badRequest,
                ZonedDateTime.now(ZoneId.of("Z"))
        );

        return new ResponseEntity<>(apiException, badRequest);
    }
}

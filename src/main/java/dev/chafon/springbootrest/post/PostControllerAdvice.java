package dev.chafon.springbootrest.post;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class PostControllerAdvice {

    @ExceptionHandler(PostNotFoundException.class)
    ErrorResponse handlePostNotFoundException(PostNotFoundException ex) {
        return ErrorResponse.builder(ex, HttpStatus.NOT_FOUND, ex.getMessage()).build();
    }

}

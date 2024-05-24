package dev.chafon.springbootrest.user;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class UserControllerExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    ErrorResponse handleUserNotFoundException(UserNotFoundException ex) {
       return ErrorResponse.builder(ex, HttpStatus.NOT_FOUND, ex.getMessage() ).build();
    }
}

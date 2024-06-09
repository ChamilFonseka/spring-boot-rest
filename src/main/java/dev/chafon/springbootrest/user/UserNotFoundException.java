package dev.chafon.springbootrest.user;

import static dev.chafon.springbootrest.Constants.USER_NOT_FOUND_EXCEPTION_MESSAGE;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Integer userId) {
        super(USER_NOT_FOUND_EXCEPTION_MESSAGE + userId);
    }
}

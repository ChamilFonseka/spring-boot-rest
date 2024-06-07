package dev.chafon.springbootrest.user;

import static dev.chafon.springbootrest.Constants.USER_ALREADY_EXISTS_EXCEPTION_MESSAGE;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String  username) {
        super(USER_ALREADY_EXISTS_EXCEPTION_MESSAGE + username);
    }
}

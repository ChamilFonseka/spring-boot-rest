package dev.chafon.springbootrest.post;

import static dev.chafon.springbootrest.Constants.POST_NOT_FOUND_EXCEPTION_MESSAGE;

public class PostNotFoundException extends RuntimeException {
    public PostNotFoundException(Integer id) {
        super(POST_NOT_FOUND_EXCEPTION_MESSAGE + id);
    }
}

package dev.chafon.springbootrest.user;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Integer userId) {
        super("User not found with the id: " + userId);
    }
}

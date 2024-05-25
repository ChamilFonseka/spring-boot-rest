package dev.chafon.springbootrest.user;

import org.springframework.stereotype.Service;

import java.util.List;

import static dev.chafon.springbootrest.user.Constants.USER_ALREADY_EXISTS_EXCEPTION_MESSAGE;
import static dev.chafon.springbootrest.user.Constants.USER_NOT_FOUND_EXCEPTION_MESSAGE;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public List<User> getUsers() {
        return repository.findAll();
    }

    public User getUser(Integer id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(USER_NOT_FOUND_EXCEPTION_MESSAGE + id));
    }

    public User createUser(User user) {
        repository.findByUsername(user.username())
                .ifPresent(existingUser -> {
                    throw new UserAlreadyExistsException(USER_ALREADY_EXISTS_EXCEPTION_MESSAGE
                            + existingUser.username());
                });

        return repository.save(user);
    }

    public void updateUser(User userToUpdate) {
    }

    public void deleteUser(Integer id) {
    }
}

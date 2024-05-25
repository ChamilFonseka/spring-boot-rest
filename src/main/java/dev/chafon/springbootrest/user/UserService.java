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
        if(repository.existsByUsername(user.username())) {
            throw new UserAlreadyExistsException(USER_ALREADY_EXISTS_EXCEPTION_MESSAGE + user.username());
        }
        return repository.save(user);
    }

    public void updateUser(Integer id, User user) {
        //If it is required to return the updated user, use the following code:
        /*
        repository.findById(id)
                .map(existingUser -> {
                    new User(existingUser.id(), user.name(), existingUser.username(), user.email());
                    return repository.save(existingUser);
                })
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_EXCEPTION_MESSAGE + id));
        */

        repository.findById(id)
                .ifPresentOrElse(
                        existingUser -> repository.save(new User(
                                existingUser.id(),
                                user.name(),
                                existingUser.username(),
                                user.email()
                        )),
                        () -> {
                            throw new UserNotFoundException(USER_NOT_FOUND_EXCEPTION_MESSAGE + id);
                        });
    }

    public void deleteUser(Integer id) {
        if(!repository.existsById(id)) {
            throw new UserNotFoundException(USER_NOT_FOUND_EXCEPTION_MESSAGE + id);
        }
        repository.deleteById(id);
    }
}

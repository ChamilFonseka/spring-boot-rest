package dev.chafon.springbootrest.user;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User getUser(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(id));
    }

    public User createUser(User user) {
        if (userRepository.existsByUsername(user.username())) {
            throw new UserAlreadyExistsException(user.username());
        }
        return userRepository.save(user);
    }

    public void updateUser(Integer id, User user) {
        //If it is required to return the updated user, use the following code:
        /*
        return userRepository.findById(id)
                .map(existingUser -> {
                    new User(existingUser.id(), user.name(), existingUser.username(), user.email());
                    return userRepository.save(existingUser);
                })
                .orElseThrow(() -> new UserNotFoundException(id));
        */

        userRepository.findById(id)
                .ifPresentOrElse(
                        existingUser -> userRepository.save(
                                new User(
                                        existingUser.id(),
                                        user.name(),
                                        existingUser.username(),
                                        user.email()
                                )),
                        () -> {
                            throw new UserNotFoundException(id);
                        });
    }

    public void deleteUser(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }
}

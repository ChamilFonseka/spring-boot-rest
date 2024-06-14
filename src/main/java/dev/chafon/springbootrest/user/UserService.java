package dev.chafon.springbootrest.user;

import dev.chafon.springbootrest.post.Post;
import dev.chafon.springbootrest.post.PostService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PostService postService;

    public UserService(UserRepository userRepository, PostService postService) {
        this.userRepository = userRepository;
        this.postService = postService;
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
        validateUser(id);
        userRepository.deleteById(id);
    }

    public List<Post> getUserPosts(Integer id) {
        validateUser(id);
        return postService.getPostsByUser(id);
    }

    private void validateUser(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
    }
}

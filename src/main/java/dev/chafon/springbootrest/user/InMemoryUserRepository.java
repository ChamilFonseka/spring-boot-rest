package dev.chafon.springbootrest.user;

import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static dev.chafon.springbootrest.user.Constants.*;

@Repository
@Validated
public class InMemoryUserRepository implements UserRepository {

    private final Map<Integer, User> userMap;

    public InMemoryUserRepository() {
        this.userMap = new ConcurrentHashMap<>();
    }

    @Override
    public List<User> findAll() {
        return userMap.values()
                .stream()
                .toList();
    }

    @Override
    public Optional<User> findById(Integer id) {
        Objects.requireNonNull(id, ID_CANNOT_BE_NULL);
        return Optional.ofNullable(userMap.get(id));
    }

    @Override
    public User save(User user) {
        Objects.requireNonNull(user, USER_CANNOT_BE_NULL);
        User userToSave = new User(userMap.size() + 1, user.name(), user.username(), user.email());
        userMap.put(userToSave.id(), userToSave);
        return userToSave;
    }

    @Override
    public boolean existsByUsername(String username) {
        Objects.requireNonNull(username, USERNAME_CANNOT_BE_NULL);
        return userMap.values()
                .stream()
                .anyMatch(user -> user.username().equals(username));
    }

    @Override
    public void deleteById(Integer id) {
        Objects.requireNonNull(id, ID_CANNOT_BE_NULL);
        userMap.remove(id);
    }

    @Override
    public boolean existsById(Integer id) {
        Objects.requireNonNull(id, ID_CANNOT_BE_NULL);
        return userMap.values()
                .stream()
                .anyMatch(user -> user.id().equals(id));
    }
}

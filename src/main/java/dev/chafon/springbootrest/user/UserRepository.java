package dev.chafon.springbootrest.user;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    List<User> findAll();
    Optional<User> findById(Integer id);
    User save(User userToCreate);
    boolean existsByUsername(String username);
    void deleteById(Integer id);
    boolean existsById(Integer id);
}

package dev.chafon.springbootrest.user;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class InMemoryUserRepository implements UserRepository {
    @Override
    public List<User> findAll() {
        return null;
    }

    @Override
    public Optional<User> findById(Integer id) {
        return Optional.empty();
    }

    @Override
    public User save(User userToCreate) {
        return null;
    }

    @Override
    public boolean existsByUsername(String username) {
        return false;
    }

    @Override
    public void deleteById(Integer id) {

    }

    @Override
    public boolean existsById(Integer id) {
        return false;
    }
}

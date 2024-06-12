package dev.chafon.springbootrest.post;

import java.util.List;
import java.util.Optional;

public interface PostRepository {
    List<Post> findAll();
    Optional<Post> findById(Integer id);
    Post save(Post post);
    void deleteById(Integer id);
    boolean existsById(Integer id);
    List<Post> findByUserId(Integer userId);
    Optional<Post> findByUserIdAndId(Integer userId, Integer id);
}

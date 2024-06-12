package dev.chafon.springbootrest.post;

import dev.chafon.springbootrest.user.User;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static dev.chafon.springbootrest.Constants.*;

@Repository
public class InMemoryPostRepository implements PostRepository {

    private final Map<Integer, Post> postMap;

    public InMemoryPostRepository() {
        this.postMap = new ConcurrentHashMap<>();
    }

    @Override
    public List<Post> findAll() {
        return postMap.values()
                .stream()
                .toList();
    }

    @Override
    public Optional<Post> findById(Integer id) {
        Objects.requireNonNull(id, ID_CANNOT_BE_NULL);
        return Optional.ofNullable(postMap.get(id));
    }

    @Override
    public Post save(Post post) {
        Objects.requireNonNull(post, POST_CANNOT_BE_NULL);
        Post postToSave;
        if (post.id() != null) {
            Post existingPost = postMap.get(post.id());
            if(existingPost == null) {
                throw new IllegalArgumentException(POST_WITH_ID_DOES_NOT_EXIST + " : " + post.id());
            }
            postToSave = post;
        } else {
            postToSave = new Post(postMap.size() + 1, post.userId(), post.title(), post.body());
        }
        postMap.put(postToSave.id(), postToSave);
        return postToSave;
    }

    @Override
    public void deleteById(Integer id) {
        Objects.requireNonNull(id, ID_CANNOT_BE_NULL);
        postMap.remove(id);
    }

    @Override
    public boolean existsById(Integer id) {
        Objects.requireNonNull(id, ID_CANNOT_BE_NULL);
        return postMap.values()
                .stream()
                .anyMatch(user -> user.id().equals(id));
    }

    @Override
    public List<Post> findByUserId(Integer userId) {
        return postMap.values()
                .stream()
                .filter(post -> post.userId().equals(userId))
                .toList();
    }

    @Override
    public Optional<Post> findByIdAndUserId(Integer id, Integer userId) {
        return postMap.values()
                .stream()
                .filter(post -> post.userId().equals(userId))
                .filter(post -> post.id().equals(id))
                .findFirst();
    }
}

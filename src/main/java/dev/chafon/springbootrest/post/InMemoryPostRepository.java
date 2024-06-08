package dev.chafon.springbootrest.post;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
}

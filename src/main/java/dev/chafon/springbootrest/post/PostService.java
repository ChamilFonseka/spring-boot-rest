package dev.chafon.springbootrest.post;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {

    private final PostRepository repository;

    public PostService(PostRepository repository) {
        this.repository = repository;
    }

    public List<Post> getPosts() {
        return repository.findAll();
    }
}

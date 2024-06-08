package dev.chafon.springbootrest.post;

import java.util.List;

public interface PostRepository {
    List<Post> findAll();
}

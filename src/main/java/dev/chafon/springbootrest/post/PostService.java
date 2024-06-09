package dev.chafon.springbootrest.post;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public List<Post> getPosts() {
        return postRepository.findAll();
    }

    public Post getPost(Integer id) {
        return postRepository.findById(id)
                .orElseThrow(() ->
                        new PostNotFoundException(id));
    }

    public Post createPost(Post post) {
        return postRepository.save(post);
    }

    public void updatePost(Integer id, Post postToUpdate) {
        postRepository.findById(id)
                .ifPresentOrElse(
                        exisitingPost -> {
                            postRepository.save(
                                    new Post(
                                            exisitingPost.id(),
                                            postToUpdate.userId(),
                                            postToUpdate.title(),
                                            postToUpdate.body()
                                    )
                            );
                        },
                        () -> {
                            throw new PostNotFoundException(id);
                        });
    }

    public void deletePost(Integer id) {
        if(!postRepository.existsById(id)) {
            throw new PostNotFoundException(id);
        }
        postRepository.deleteById(id);
    }
}

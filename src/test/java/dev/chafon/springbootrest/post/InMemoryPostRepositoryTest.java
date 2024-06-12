package dev.chafon.springbootrest.post;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static dev.chafon.springbootrest.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class InMemoryPostRepositoryTest {

    private InMemoryPostRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryPostRepository();
    }

    @Test
    void findAll_shouldReturnEmptyListWhenNoPosts() {
        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void findAll_shouldReturnPostsWhenPostsExist() {
        Post post1 = repository.save(
                new Post(null, 1, "Java", "Body"));
        Post post2 = repository.save(
                new Post(null, 1, "Spring", "Body"));

        List<Post> posts = repository.findAll();

        assertThat(posts).hasSize(2);
        assertThat(posts).containsExactlyInAnyOrder(post1, post2);
    }

    @Test
    void findById_shouldReturnPostWhenPostExists() {
        Post post = saveAPost();

        Optional<Post> postExpected = repository.findById(1);
        assertThat(postExpected).isPresent();
        assertThat(postExpected.get()).isEqualTo(post);
    }

    @Test
    void findById_shouldReturnEmptyOptionalWhenPostDoesNotExist() {
        Optional<Post> postExpected = repository.findById(1);
        assertThat(postExpected).isEmpty();
    }

    @Test
    void findById_shouldThrowNullPointerExceptionWhenIdIsNull() {
        assertThatThrownBy(() -> repository.findById(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage(ID_CANNOT_BE_NULL);
    }

    @Test
    void save_shouldPersistPost() {
        Post post = new Post(null, 1, "Title", "Body");

        Post savedPost = repository.save(post);
        assertThat(savedPost.id()).isNotNull();

        Optional<Post> postExpected = repository.findById(savedPost.id());
        assertThat(postExpected).isPresent();
        assertThat(postExpected.get()).isEqualTo(savedPost);
    }

    @Test
    void save_shouldThrowNullPointerExceptionWhenPostIsNull() {
        assertThatThrownBy(() -> repository.save(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage(POST_CANNOT_BE_NULL);
    }

    @Test
    void save_shouldThrowIllegalArgumentExceptionWhenPostIdNotExists() {
        Post post = new Post(1, 1, "Title", "Body");

        assertThatThrownBy(() -> repository.save(post))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(POST_WITH_ID_DOES_NOT_EXIST + " : " + post.id());

    }

    @Test
    void deleteById_shouldDeletePost() {
        Post post = saveAPost();
        repository.deleteById(post.id());

        Optional<Post> postExpected = repository.findById(post.id());
        assertThat(postExpected).isEmpty();
    }

    @Test
    void deleteById_shouldThrowNullPointerExceptionWhenIdIsNull() {
        assertThatThrownBy(() -> repository.deleteById(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage(ID_CANNOT_BE_NULL);
    }

    @Test
    void existsById_shouldReturnTrueWhenPostExists() {
        Post post = saveAPost();

        boolean exists = repository.existsById(post.id());
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_shouldReturnFalseWhenPostDoesNotExist() {
        boolean exists = repository.existsById(1);
        assertThat(exists).isFalse();
    }

    @Test
    void existsById_shouldThrowNullPointerExceptionWhenIdIsNull() {
        assertThatThrownBy(() -> repository.existsById(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage(ID_CANNOT_BE_NULL);
    }

    @Test
    void findByUserId_shouldReturnPostsForUser() {
        Integer user1Id = 1;
        Integer user2Id = 2;
        Post post1 = repository.save(new Post(null, user1Id, "Post 1", "Post content"));
        Post post2 = repository.save(new Post(null, user1Id, "Post 2", "Post content"));
        Post post3 = repository.save(new Post(null, user2Id, "Post 3", "Post content"));

        List<Post> user1Posts = repository.findByUserId(user1Id);
        assertThat(user1Posts).hasSize(2);

        assertThat(user1Posts.getFirst().userId()).isEqualTo(post1.userId());
        assertThat(user1Posts.getFirst().title()).isEqualTo(post1.title());
        assertThat(user1Posts.getFirst().body()).isEqualTo(post1.body());

        assertThat(user1Posts.get(1).userId()).isEqualTo(post2.userId());
        assertThat(user1Posts.get(1).title()).isEqualTo(post2.title());
        assertThat(user1Posts.get(1).body()).isEqualTo(post2.body());

        List<Post> user2Posts = repository.findByUserId(user2Id);
        assertThat(user2Posts).hasSize(1);
        assertThat(user2Posts.getFirst().userId()).isEqualTo(post3.userId());
        assertThat(user2Posts.getFirst().title()).isEqualTo(post3.title());
        assertThat(user2Posts.getFirst().body()).isEqualTo(post3.body());
    }

    @Test
    void findByIdAndUserId_shouldReturnPost() {
        Post post = saveAPost();
        Optional<Post> postExpected = repository.findByIdAndUserId(post.id(), post.userId());
        assertThat(postExpected).isPresent();
        assertThat(postExpected.get()).isEqualTo(post);
    }

    @Test
    void findByIdAndUserId_shouldReturnEmptyOptionalWhenPostDoesNotExist() {
        Optional<Post> postExpected = repository.findByIdAndUserId(1, 1);
        assertThat(postExpected).isEmpty();
    }

    private Post saveAPost() {
        return repository.save(new Post(null, 1, "Title", "Body"));
    }
}

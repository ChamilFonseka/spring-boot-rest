package dev.chafon.springbootrest.post;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static dev.chafon.springbootrest.Constants.POST_NOT_FOUND_EXCEPTION_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    @Captor
    private ArgumentCaptor<Post> argumentCaptor;

    @Test
    void shouldReturnAllPosts() {
        Post post1 = new Post(1, 1, "Java Post", "Java post content");
        Post post2 = new Post(2, 1, "Spring Post", "Spring post content");
        given(postRepository.findAll())
                .willReturn(List.of(post1, post2));

        List<Post> posts = postService.getPosts();

        assertThat(posts).hasSize(2);

        assertThat(posts.getFirst().id()).isEqualTo(post1.id());
        assertThat(posts.getFirst().userId()).isEqualTo(post1.userId());
        assertThat(posts.getFirst().title()).isEqualTo(post1.title());
        assertThat(posts.getFirst().body()).isEqualTo(post1.body());

        assertThat(posts.get(1).id()).isEqualTo(post2.id());
        assertThat(posts.get(1).userId()).isEqualTo(post2.userId());
        assertThat(posts.get(1).title()).isEqualTo(post2.title());
        assertThat(posts.get(1).body()).isEqualTo(post2.body());

        verify(postRepository).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoPosts() {
        given(postRepository.findAll())
                .willReturn(List.of());

        List<Post> posts = postService.getPosts();

        assertThat(posts).isEmpty();

        verify(postRepository).findAll();
    }

    @Test
    void shouldReturnPostById() {
        Post post = new Post(1, 1, "Java post", "Java post content");
        given(postRepository.findById(1))
                .willReturn(Optional.of(post));

        Post foundPost = postService.getPost(post.id());

        assertThat(foundPost).isEqualTo(post);

        verify(postRepository).findById(post.id());
    }

    @Test
    void shouldThrowPostNotFoundExceptionWhenPostWithGivenIdDoesNotExist() {
        Integer postId = 1;

        assertThatThrownBy(() -> postService.getPost(postId))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessageContaining(POST_NOT_FOUND_EXCEPTION_MESSAGE + postId);

        verify(postRepository).findById(postId);
    }

    @Test
    void shouldCreatePostAndReturnIt() {
        Post postToCreate = new Post(null, 567, "Java post", "Java post content");

        Integer expextedId = 123;
        given(postRepository.save(postToCreate))
                .willReturn(
                        new Post(expextedId,
                                postToCreate.userId(),
                                postToCreate.title(),
                                postToCreate.body()));

        Post postCreated = postService.createPost(postToCreate);

        assertThat(postCreated).isNotNull();
        assertThat(postCreated.id()).isEqualTo(expextedId);

        verify(postRepository).save(argumentCaptor.capture());

        Post capturedPost = argumentCaptor.getValue();
        assertThat(capturedPost.id()).isNull();
        assertThat(capturedPost.userId()).isEqualTo(postToCreate.userId());
        assertThat(capturedPost.title()).isEqualTo(postToCreate.title());
        assertThat(capturedPost.body()).isEqualTo(postToCreate.body());
    }

    @Test
    void shouldUpdatePost() {
        Post postToUpdate = new Post(123, 567, "Updated title", "Updated post content");

        given(postRepository.findById(postToUpdate.id()))
                .willReturn(Optional.of(
                        new Post(postToUpdate.id(),
                                postToUpdate.userId(),
                                "Existing title",
                                "Existing post content")));

        postService.updatePost(postToUpdate.id(), postToUpdate);

        verify(postRepository).findById(postToUpdate.id());
        verify(postRepository).save(argumentCaptor.capture());

        Post capturedPost = argumentCaptor.getValue();
        assertThat(capturedPost.id()).isEqualTo(postToUpdate.id());
        assertThat(capturedPost.userId()).isEqualTo(postToUpdate.userId());
        assertThat(capturedPost.title()).isEqualTo(postToUpdate.title());
        assertThat(capturedPost.body()).isEqualTo(postToUpdate.body());
    }

    @Test
    void shouldThrowPostNotFoundExceptionWhenUpdatingPostNotExist() {
        Post postToUpdate = new Post(123, 567, "Updated title", "Updated post content");

        given(postRepository.findById(postToUpdate.id()))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> postService.updatePost(postToUpdate.id(), postToUpdate))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessageContaining(POST_NOT_FOUND_EXCEPTION_MESSAGE + postToUpdate.id());

        verify(postRepository).findById(postToUpdate.id());
        verify(postRepository, never()).save(postToUpdate);
    }

    @Test
    void shouldDeletePost() {
        Integer idToDelete = 123;

        given(postRepository.existsById(idToDelete))
                .willReturn(true);

        postService.deletePost(idToDelete);

        verify(postRepository).existsById(idToDelete);
        verify(postRepository).deleteById(idToDelete);
    }

    @Test
    void shouldThrowPostNotFoundExceptionWhenDeletingPostNotExist() {
        Integer idToDelete = 123;

        given(postRepository.existsById(idToDelete))
                .willReturn(false);

        assertThatThrownBy(() -> postService.deletePost(idToDelete))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessageContaining(POST_NOT_FOUND_EXCEPTION_MESSAGE + idToDelete);

        verify(postRepository).existsById(idToDelete);
        verify(postRepository, never()).deleteById(idToDelete);
    }

    @Test
    void shouldReturnPostsByUserId() {
        Integer userId = 123;
        Post post1 = new Post(1, userId, "Post 1", "Post content");
        Post post2 = new Post(2, userId, "Post 2", "Post content");
        given(postRepository.findByUserId(userId))
                .willReturn(List.of(post1, post2));

        List<Post> posts = postService.getPostsByUser(userId);

        assertThat(posts).hasSize(2);

        assertThat(posts.getFirst().id()).isEqualTo(post1.id());
        assertThat(posts.getFirst().userId()).isEqualTo(post1.userId());
        assertThat(posts.getFirst().title()).isEqualTo(post1.title());
        assertThat(posts.getFirst().body()).isEqualTo(post1.body());

        assertThat(posts.get(1).id()).isEqualTo(post2.id());
        assertThat(posts.get(1).userId()).isEqualTo(post2.userId());
        assertThat(posts.get(1).title()).isEqualTo(post2.title());
        assertThat(posts.get(1).body()).isEqualTo(post2.body());

        verify(postRepository).findByUserId(userId);
    }

    @Test
    void shouldReturnPostByUserAndId() {
        Integer userId = 123;
        Integer postId = 456;
        Post post = new Post(postId, userId, "Post 1", "Post content");
        given(postRepository.findByUserIdAndId(userId, postId))
                .willReturn(Optional.of(post));

        Post foundPost = postService.getPostByUserAndId(userId, postId);

        assertThat(foundPost).isEqualTo(post);

        verify(postRepository).findByUserIdAndId(userId, postId);
    }

    @Test
    void shouldThrowPostNotFoundExceptionWhenPostByUserAndIdNotExist() {
        Integer userId = 123;
        Integer postId = 456;
        given(postRepository.findByUserIdAndId(userId, postId))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPostByUserAndId(userId, postId))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessageContaining(POST_NOT_FOUND_EXCEPTION_MESSAGE + postId);
    }
}

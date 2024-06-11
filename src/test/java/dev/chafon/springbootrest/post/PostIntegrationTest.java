package dev.chafon.springbootrest.post;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import dev.chafon.springbootrest.Constants;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Optional;

import static dev.chafon.springbootrest.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class PostIntegrationTest {

    public static final String BASE_URL = "/api/v1/posts";
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PostRepository postRepository;

    @Test
    void shouldReturnPosts() {
        Post post1 = postRepository.save(new Post(null, 123, "Title 1", "Content 1"));
        Post post2 = postRepository.save(new Post(null, 456, "Title 2", "Content 2"));

        ResponseEntity<String> response = restTemplate.getForEntity(BASE_URL, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());

        int postCount = documentContext.read("$.length()");
        assertThat(postCount).isEqualTo(2);

        JSONArray ids = documentContext.read("$..id");
        assertThat(ids).containsExactlyInAnyOrder(post1.id(), post2.id());

        JSONArray userIds = documentContext.read("$..userId");
        assertThat(userIds).containsExactlyInAnyOrder(post1.userId(), post2.userId());

        JSONArray titles = documentContext.read("$..title");
        assertThat(titles).containsExactlyInAnyOrder(post1.title(), post2.title());

        JSONArray contents = documentContext.read("$..body");
        assertThat(contents).containsExactlyInAnyOrder(post1.body(), post2.body());

        Integer id1 = documentContext.read("[0].id");
        assertThat(id1).isEqualTo(post1.id());

        Integer id2 = documentContext.read("[1].id");
        assertThat(id2).isEqualTo(post2.id());

        Integer userId1 = documentContext.read("[0].userId");
        assertThat(userId1).isEqualTo(post1.userId());

        Integer UserId2 = documentContext.read("[1].userId");
        assertThat(UserId2).isEqualTo(post2.userId());

        String  title1 = documentContext.read("[0].title");
        assertThat(title1).isEqualTo(post1.title());

        String title2 = documentContext.read("[1].title");
        assertThat(title2).isEqualTo(post2.title());

        String body1 = documentContext.read("[0].body");
        assertThat(body1).isEqualTo(post1.body());

        String body2 = documentContext.read("[1].body");
        assertThat(body2).isEqualTo(post2.body());
    }

    @Test
    @DirtiesContext
    void shouldReturnAnEmptyPostList() throws Exception {
        ResponseEntity<String> response = restTemplate.getForEntity(BASE_URL, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());

        int postCount = documentContext.read("$.length()");
        assertThat(postCount).isEqualTo(0);
    }

    @Test
    @DirtiesContext
    void shouldReturnThePost() throws Exception {
        Post post = postRepository.save(
                new Post(null, 123, "Title 1", "Content 1"));

        ResponseEntity<String> response = restTemplate.getForEntity(BASE_URL + "/" + post.id(), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());

        Integer id = documentContext.read("$.id");
        assertThat(id).isEqualTo(post.id());

        String title = documentContext.read("$.title");
        assertThat(title).isEqualTo(post.title());

        String body = documentContext.read("$.body");
        assertThat(body).isEqualTo(post.body());
    }

    @Test
    void shouldReturnStatusNotFoundWhenPostDoesNotExist() throws Exception {
        ResponseEntity<String> response = restTemplate.getForEntity(BASE_URL + "/99", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        DocumentContext documentContext = JsonPath.parse(response.getBody());

        String message = documentContext.read("$.detail");
        assertThat(message).isEqualTo(Constants.POST_NOT_FOUND_EXCEPTION_MESSAGE + 99);
    }

    @Test
    @DirtiesContext
    void shouldCreatePost() throws Exception {
        Post post = new Post(null, 123, "Title 1", "Content 1");

        ResponseEntity<Void> postCreatedResponse = restTemplate.postForEntity(BASE_URL, post, Void.class);
        assertThat(postCreatedResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(postCreatedResponse.getHeaders().getLocation()).isNotNull();

        ResponseEntity<String> postGetResponse = restTemplate.getForEntity(postCreatedResponse.getHeaders().getLocation(), String.class);

        DocumentContext documentContext = JsonPath.parse(postGetResponse.getBody());

        Integer id = documentContext.read("$.id");
        assertThat(id).isNotNull();

        Integer userId = documentContext.read("$.userId");
        assertThat(userId).isEqualTo(post.userId());

        String title = documentContext.read("$.title");
        assertThat(title).isEqualTo(post.title());

        String body = documentContext.read("$.body");
        assertThat(body).isEqualTo(post.body());
    }

    @Test
    void shouldReturnStatusBadRequestWhenPostIsInvalid() throws Exception {
        Post post = new Post(null, null, null, null);

        ResponseEntity<String> postCreatedResponse = restTemplate.postForEntity(BASE_URL, post, String.class);
        assertThat(postCreatedResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        DocumentContext documentContext = JsonPath.parse(postCreatedResponse.getBody());

        String errorMessage = documentContext.read("$.detail");
        assertThat(errorMessage).contains(POST_USER_CANNOT_BE_NULL);
        assertThat(errorMessage).contains(POST_TITLE_CANNOT_BE_BLANK);
        assertThat(errorMessage).contains(POST_BODY_CANNOT_BE_BLANK);
    }

    @Test
    @DirtiesContext
    void shouldUpdatePost() {
        Post post = postRepository.save(
                new Post(null, 123, "Old title", "Old content"));

        Post postToUpdate = new Post(post.id(), post.userId(), "New title", "New content");

        ResponseEntity<String> response = restTemplate
                .exchange(BASE_URL + "/" + postToUpdate.id(),
                        HttpMethod.PUT,
                        new HttpEntity<>(postToUpdate),
                        String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        Optional<Post> updatedPost = postRepository.findById(postToUpdate.id());
        assertThat(updatedPost).isPresent();
        assertThat(updatedPost.get().title()).isEqualTo(postToUpdate.title());
        assertThat(updatedPost.get().body()).isEqualTo(postToUpdate.body());
    }

    @Test
    void shouldReturnStatusNotFoundWhenUpdatingPostDoesNotExist() {
        Post postToUpdate = new Post(99, 123, "New title", "New content");

        ResponseEntity<String> response = restTemplate
                .exchange(BASE_URL + "/" + postToUpdate.id(),
                        HttpMethod.PUT,
                        new HttpEntity<>(postToUpdate),
                        String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        DocumentContext documentContext = JsonPath.parse(response.getBody());

        String errorMessage = documentContext.read("$.detail");
        assertThat(errorMessage).isEqualTo(Constants.POST_NOT_FOUND_EXCEPTION_MESSAGE + 99);
    }

    @Test
    @DirtiesContext
    void shouldDeletePost() {
        Post post = postRepository.save(
                new Post(null, 123, "Post title", "Post content"));

        ResponseEntity<Void> response = restTemplate
                .exchange(BASE_URL + "/" + post.id(),
                        HttpMethod.DELETE,
                        null,
                        Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        Optional<Post> deletedPost = postRepository.findById(post.id());
        assertThat(deletedPost).isNotPresent();
    }

    @Test
    void shouldReturnStatusNotFoundWhenDeletingPostDoesNotExist() {
        ResponseEntity<String> response = restTemplate
                .exchange(BASE_URL + "/99",
                        HttpMethod.DELETE,
                        null,
                        String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        DocumentContext documentContext = JsonPath.parse(response.getBody());

        String errorMessage = documentContext.read("$.detail");
        assertThat(errorMessage).isEqualTo(Constants.POST_NOT_FOUND_EXCEPTION_MESSAGE + 99);
    }
}

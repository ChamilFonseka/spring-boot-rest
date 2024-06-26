package dev.chafon.springbootrest.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import dev.chafon.springbootrest.Constants;
import dev.chafon.springbootrest.post.Post;
import dev.chafon.springbootrest.post.PostRepository;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Optional;

import static dev.chafon.springbootrest.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class UserIntegrationTest {

    public static final String BASE_URL = "/api/v1/users";
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DirtiesContext
    void shouldReturnUsers() throws Exception {
        User john = userRepository.save(
                new User(null, "John Doe", "johnD", "john.doe@mail.com"));
        User jean = userRepository.save(
                new User(null, "Jane Gray", "janeG", "jane.gray@mail.com"));

        ResponseEntity<String> response = restTemplate.getForEntity(BASE_URL, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        //Can use DocumentContext
        DocumentContext documentContext = JsonPath.parse(response.getBody());

        int userCount = documentContext.read("$.length()");
        assertThat(userCount).isEqualTo(2);

        JSONArray ids = documentContext.read("$..id");
        assertThat(ids).containsExactlyInAnyOrder(john.id(), jean.id());

        JSONArray names = documentContext.read("$..name");
        assertThat(names).containsExactlyInAnyOrder(john.name(), jean.name());

        int id1 = documentContext.read("[0].id");
        assertThat(id1).isEqualTo(john.id());

        int id2 = documentContext.read("[1].id");
        assertThat(id2).isEqualTo(jean.id());

        String name1 = documentContext.read("[0].name");
        assertThat(name1).isEqualTo(john.name());

        String name2 = documentContext.read("[1].name");
        assertThat(name2).isEqualTo(jean.name());

        //Or ObjectMapper
        String expected = objectMapper.writeValueAsString(List.of(john, jean));
        JSONAssert.assertEquals(expected, response.getBody(), true);
    }

    @Test
    void shouldReturnAnEmptyUserList() {
        ResponseEntity<String> response = restTemplate.getForEntity(BASE_URL, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        //Can use DocumentContext
        DocumentContext documentContext = JsonPath.parse(response.getBody());

        int userCount = documentContext.read("$.length()");
        assertThat(userCount).isEqualTo(0);

        //Or
        assertThat(response.getBody()).isEqualTo("[]");
    }

    @Test
    @DirtiesContext
    void shouldReturnTheUser() throws Exception {
        User john = userRepository.save(
                new User(null, "John Doe", "johnD", "john.doe@mail.com"));

        ResponseEntity<String> response = restTemplate.getForEntity(BASE_URL + "/" + john.id(), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        //Can use DocumentContext
        DocumentContext documentContext = JsonPath.parse(response.getBody());

        int id = documentContext.read("$.id");
        assertThat(id).isEqualTo(john.id());

        String name = documentContext.read("$.name");
        assertThat(name).isEqualTo(john.name());

        //Or ObjectMapper
        String expected = objectMapper.writeValueAsString(john);
        JSONAssert.assertEquals(expected, response.getBody(), true);
    }

    @Test
    @DirtiesContext
    void shouldReturnStatusNotFoundWhenUserDoesNotExist() throws Exception {
        int userId = 99;
        ResponseEntity<String> response = restTemplate.getForEntity(BASE_URL + "/" + userId, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        //Can use DocumentContext
        DocumentContext documentContext = JsonPath.parse(response.getBody());

        String message = documentContext.read("$.detail");
        assertThat(message).isEqualTo(Constants.USER_NOT_FOUND_EXCEPTION_MESSAGE + userId);

        //Or
        String expected = "{\"type\":\"about:blank\",\"title\":\"Not Found\",\"status\":404,\"detail\":\"User not found with the id: 99\",\"instance\":\"/api/v1/users/99\"}";
        JSONAssert.assertEquals(expected, response.getBody(), true);
    }

    @Test
    void shouldCreateUser() {
        User john = new User(null, "John Doe", "johnD", "john.doe@mail.com");

        ResponseEntity<Void> userCreatedResponse = restTemplate.postForEntity(BASE_URL, john, Void.class);
        assertThat(userCreatedResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(userCreatedResponse.getHeaders().getLocation()).isNotNull();

        ResponseEntity<String> userGetResponse = restTemplate.getForEntity(userCreatedResponse.getHeaders().getLocation(), String.class);

        DocumentContext documentContext = JsonPath.parse(userGetResponse.getBody());

        int id = documentContext.read("$.id");
        assertThat(id).isNotNull();

        String name = documentContext.read("$.name");
        assertThat(name).isEqualTo(john.name());

        String username = documentContext.read("$.username");
        assertThat(username).isEqualTo(john.username());

        String email = documentContext.read("$.email");
        assertThat(email).isEqualTo(john.email());
    }

    @Test
    void shouldReturnStatusBadRequestWhenUserIsInvalid() {
        User john = new User(null, null, null, null);

        ResponseEntity<String> response = restTemplate.postForEntity(BASE_URL, john, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        DocumentContext documentContext = JsonPath.parse(response.getBody());

        String errorMessage = documentContext.read("$.detail");
        assertThat(errorMessage).contains(NAME_CANNOT_BE_BLANK);
        assertThat(errorMessage).contains(USERNAME_CANNOT_BE_BLANK);
        assertThat(errorMessage).contains(EMAIL_CANNOT_BE_BLANK);
    }

    @Test
    @DirtiesContext
    void shouldReturnStatusConflictWhenUserWithSameUserNameAlreadyExist() {
        User john = new User(null, "John Doe", "johnD", "john.doe@mail.com");
        userRepository.save(john);

        User anotherJohn = new User(null, "John Dean", "johnD", "john.dean@mail.com");

        ResponseEntity<String> response = restTemplate.postForEntity(BASE_URL, anotherJohn, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        DocumentContext documentContext = JsonPath.parse(response.getBody());

        String errorMessage = documentContext.read("$.detail");
        assertThat(errorMessage).isEqualTo(USER_ALREADY_EXISTS_EXCEPTION_MESSAGE + anotherJohn.username());
    }

    @Test
    @DirtiesContext
    void shouldUpdateTheUser() {
        User user = userRepository.save(new User(null, "John", "johnD", "john.doe@mail.com"));
        User userToUpdate = new User(user.id(), "John Doe", "johnD", "new.john.doe@mail.com");

        ResponseEntity<String> response = restTemplate
                .exchange(BASE_URL + "/" + userToUpdate.id(),
                        HttpMethod.PUT,
                        new HttpEntity<>(userToUpdate),
                        String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        Optional<User> updatedUser = userRepository.findById(userToUpdate.id());
        assertThat(updatedUser).isPresent();
        assertThat(updatedUser.get().name()).isEqualTo(userToUpdate.name());
        assertThat(updatedUser.get().username()).isEqualTo(userToUpdate.username());
        assertThat(updatedUser.get().email()).isEqualTo(userToUpdate.email());
    }

    @Test
    void shouldReturnStatusNotFoundWhenUpdatingUserDoesNotExist() {
        User userToUpdate = new User(99, "John Doe", "johnD", "new.john.doe@mail.com");

        ResponseEntity<String> response = restTemplate
                .exchange(BASE_URL + "/" + userToUpdate.id(),
                        HttpMethod.PUT,
                        new HttpEntity<>(userToUpdate),
                        String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        DocumentContext documentContext = JsonPath.parse(response.getBody());

        String errorMessage = documentContext.read("$.detail");
        assertThat(errorMessage).isEqualTo(USER_NOT_FOUND_EXCEPTION_MESSAGE + userToUpdate.id());
    }

    @Test
    void shouldDeleteTheUser() {
        User user = userRepository.save(new User(null, "John", "johnD", "john.doe@mail.com"));

        ResponseEntity<Void> response = restTemplate.exchange(BASE_URL + "/" + user.id(),
                HttpMethod.DELETE,
                null,
                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        Optional<User> deletedUser = userRepository.findById(user.id());
        assertThat(deletedUser).isNotPresent();
    }

    @Test
    void shouldReturnStatusNotFoundWhenDeletingUserDoesNotExist() {
        ResponseEntity<String> response = restTemplate.exchange(BASE_URL + "/99",
                HttpMethod.DELETE,
                null,
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        DocumentContext documentContext = JsonPath.parse(response.getBody());

        String errorMessage = documentContext.read("$.detail");
        assertThat(errorMessage).isEqualTo(USER_NOT_FOUND_EXCEPTION_MESSAGE + 99);
    }

    @Test
    @DirtiesContext
    void shouldReturnUserPosts() {
        User user = userRepository.save(new User(null, "John", "johnD", "john.doe@mail.com"));
        Post post1 = postRepository.save(new Post(null, user.id(), "Post 1", "Post 1 content"));
        Post post2 = postRepository.save(new Post(null, user.id(), "Post 2", "Post 2 content"));

        ResponseEntity<String> response = restTemplate.getForEntity(
                BASE_URL + "/" + user.id() + "/posts", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());

        int postCount = documentContext.read("$.length()");
        assertThat(postCount).isEqualTo(2);

        JSONArray ids = documentContext.read("$..userId");
        assertThat(ids).containsExactlyInAnyOrder(user.id(), user.id());

        JSONArray titles = documentContext.read("$..title");
        assertThat(titles).containsExactlyInAnyOrder(post1.title(), post2.title());

        JSONArray bodies = documentContext.read("$..body");
        assertThat(bodies).containsExactlyInAnyOrder(post1.body(), post2.body());
    }

    @Test
    @DirtiesContext
    void shouldReturnAnEmptyPostList() {
        User user = userRepository.save(new User(null, "John", "johnD", "john.doe@mail.com"));

        ResponseEntity<String> response = restTemplate.getForEntity(
                BASE_URL + "/" + user.id() + "/posts", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());

        int postCount = documentContext.read("$.length()");
        assertThat(postCount).isEqualTo(0);
    }

    @Test
    void shouldReturnStatusNotFoundWhenUserForPostsDoesNotExist() {
        int userId = 99;
        ResponseEntity<String> response = restTemplate.getForEntity(
                BASE_URL + "/" + userId + "/posts", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        DocumentContext documentContext = JsonPath.parse(response.getBody());

        String errorMessage = documentContext.read("$.detail");

        assertThat(errorMessage).isEqualTo(USER_NOT_FOUND_EXCEPTION_MESSAGE + userId);
    }

    @Test
    @DirtiesContext
    void shouldReturnUserPost() {
        User user = userRepository.save(new User(null, "John", "johnD", "john.doe@mail.com"));
        Post post = postRepository.save(new Post(null, user.id(), "Post 1", "Post 1 content"));

        ResponseEntity<String> response = restTemplate.getForEntity(
                BASE_URL + "/" + user.id() + "/posts/" + post.id(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());

        int id = documentContext.read("$.id");
        assertThat(id).isEqualTo(post.id());

        int userId = documentContext.read("$.userId");
        assertThat(userId).isEqualTo(post.userId());

        String title = documentContext.read("$.title");
        assertThat(title).isEqualTo(post.title());

        String body = documentContext.read("$.body");
        assertThat(body).isEqualTo(post.body());
    }

    @Test
    void shouldReturnStatusNotFoundWhenUserForPostDoesNotExist() {
        int userId = 99;

        ResponseEntity<String> response = restTemplate.getForEntity(
                BASE_URL + "/" + userId + "/posts/1", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        DocumentContext documentContext = JsonPath.parse(response.getBody());

        String errorMessage = documentContext.read("$.detail");

        assertThat(errorMessage).isEqualTo(USER_NOT_FOUND_EXCEPTION_MESSAGE + userId);
    }

    @Test
    @DirtiesContext
    void shouldReturnStatusNotFoundWhenPostForUserDoesNotExist() {
        int postId = 99;
        User user = userRepository.save(new User(null, "John", "johnD", "john.doe@mail.com"));

        ResponseEntity<String> response = restTemplate.getForEntity(
                BASE_URL + "/" + user.id() + "/posts/" + postId, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        DocumentContext documentContext = JsonPath.parse(response.getBody());

        String errorMessage = documentContext.read("$.detail");

        assertThat(errorMessage).isEqualTo(POST_NOT_FOUND_EXCEPTION_MESSAGE + postId);
    }
}
